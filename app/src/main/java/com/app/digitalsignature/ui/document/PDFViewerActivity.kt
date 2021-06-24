package com.app.digitalsignature.ui.document

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import com.shockwave.pdfium.PdfiumCore
import io.github.hyuwah.draggableviewlib.*
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu
    private lateinit var selectedPDF: Uri
    private lateinit var signatureBitmap: Bitmap
    private var isSigned: Boolean = false

    private var signatureDragListener = object : DraggableListener {
        @SuppressLint("SetTextI18n")
        override fun onPositionChanged(view: View) {
            xPosition.text = "X: " + signatureImage.x.toString()
            yPosition.text = "Y: " + signatureImage.y.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        viewPDF()

        signatureImage.setupDraggable()
            .setAnimated(true)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .setListener(signatureDragListener)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val signatureFile = data?.getStringExtra("signatureFileName").toString()
                val filePath = File(signatureFile)
                signatureBitmap = BitmapFactory.decodeFile(filePath.absolutePath)
                signatureImage.setImageBitmap(signatureBitmap)

                invokeMenuButton(true)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.document_menu, menu)
        this.mMenu = menu
        val saveItem: MenuItem = mMenu.findItem(R.id.action_save_document)
        saveItem.icon.alpha = 130
        val signItem: MenuItem = mMenu.findItem(R.id.action_sign_document)
        signItem.icon.alpha = 255
        return true
    }

    private fun invokeMenuButton(disableButtonFlag: Boolean) {
        val saveItem: MenuItem = mMenu.findItem(R.id.action_save_document)
        saveItem.isEnabled = disableButtonFlag
        isSigned = disableButtonFlag
        if (disableButtonFlag) {
            saveItem.icon.alpha = 255
        } else {
            saveItem.icon.alpha = 130
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_sign_document -> {
                val intent = Intent(this, SignatureActivity::class.java)
                startActivityForResult(intent, 1)
                return true
            }
            R.id.action_save_document -> {
                savePDF()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isSigned) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Save Document")
                .setMessage("Want to save your changes to PDF document?")
                .setPositiveButton(
                    "Save"
                ) { dialog, which -> savePDF() }
                .setNegativeButton(
                    "Exit"
                ) { dialog, which -> finish() }.show()
        } else {
            finish()
        }
    }

    private fun viewPDF(){
        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            if (!TextUtils.isEmpty(viewType) || viewType != null) {
                if (viewType.equals("storage")) {
                    selectedPDF = Uri.parse((intent.getStringExtra("FileUri")))
                    pdfView.fromUri(selectedPDF)
                        .password(null)
                        .defaultPage(0)
                        .pages(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
//                        .onDraw { canvas, pageWidth, pageHeight, displayedPage -> }
                        .onPageError { page, t ->
                            Toast.makeText(
                                this,
                                "Error while opening the page$page",
                                Toast.LENGTH_SHORT
                            ).show()

                        }.onTap { false }
                        .enableAnnotationRendering(true)
                        .load()
                }
            }
        }
    }

    private fun getBitmapFromPDF(pdfUri: Uri?, context: Context): Bitmap? {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(context)
        try {
            val fd: ParcelFileDescriptor? =
                pdfUri?.let { context.contentResolver.openFileDescriptor(it, "r") }
            val pdfDocument = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)

            val width = pdfiumCore.getPageWidth(pdfDocument, pageNumber)
            val height = pdfiumCore.getPageHeight(pdfDocument, pageNumber)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNumber, 0, 0,width, height)
            pdfiumCore.closeDocument(pdfDocument)
            return bitmap
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    private fun combineTwoBitmap(pdfBitmap: Bitmap, signatureBitmap: Bitmap): Bitmap? {
        val bitmap = Bitmap.createBitmap(pdfBitmap.width, pdfBitmap.height, pdfBitmap.config)
        val canvas = Canvas(bitmap)

        canvas.drawBitmap(pdfBitmap, Matrix(), null)
        val left = signatureImage.x
        val top = signatureImage.y
        canvas.drawBitmap(signatureBitmap, left, top, null)

        pdfBitmap.recycle()
        signatureBitmap.recycle()

        return bitmap
    }

    private fun savePDF() {
        val pdfBitmap = getBitmapFromPDF(selectedPDF, this)
        val signedPDF = pdfBitmap?.let { combineTwoBitmap(it,signatureBitmap) }

        val fileName = "Result"
        val folderName = "Digital Signature/SavedPDF"
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/$folderName/"

        val pdf = File(
            createFolderStorageDir(directory, folderName),
            "$fileName.jpg"
        )
        saveBitmapToJPG(signedPDF, pdf)
    }

    private fun createFolderStorageDir(directory: String, folderName: String): File {
        val file = File(directory)
        if (!file.mkdirs()) {
            Log.e(folderName, "Directory not created")
        }
        return file
    }

    @Throws(IOException::class)
    fun saveBitmapToJPG(bitmap: Bitmap?, photo: File?) {
        val newBitmap = bitmap?.let { Bitmap.createBitmap(it.width, bitmap.height, Bitmap.Config.ARGB_8888) }

        val canvas = newBitmap?.let { Canvas(it) }
        canvas?.drawColor(Color.WHITE)
        if (bitmap != null) {
            canvas?.drawBitmap(bitmap, 0f, 0f, null)
        }

        val stream: OutputStream = FileOutputStream(photo)
        newBitmap?.compress(Bitmap.CompressFormat.PNG, 80, stream)
        stream.close()
    }

}