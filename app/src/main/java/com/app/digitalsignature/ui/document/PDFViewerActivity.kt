package com.app.digitalsignature.ui.document

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import com.benzveen.pdfdigitalsignature.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfWriter
import com.shockwave.pdfium.PdfiumCore
import io.github.hyuwah.draggableviewlib.DraggableView
import io.github.hyuwah.draggableviewlib.setupDraggable
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import kotlinx.android.synthetic.main.tool_list_document.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu
    private lateinit var selectedPdf: Uri
    private lateinit var signatureBitmap: Bitmap
    private lateinit var pdfBitmap: Bitmap
    private lateinit var signedPdf: Bitmap
    private lateinit var signatureFile: String
    private lateinit var signaturePath: File
    private lateinit var mBottomSheetDialog: BottomSheetDialog

    private val savedFile = "signedPdf"
    private val savedFolder = "Digital Signature"
    private val savedPath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .toString() + "/$savedFolder/"

    private var isSigned: Boolean = false
    private var layoutWidth = 0
    private var layoutHeight = 0
    private var pdfWidth = 0
    private var pdfHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        val layout = findViewById<View>(R.id.pdfView) as RelativeLayout
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @SuppressLint("SetTextI18n")
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                layoutWidth = layout.measuredWidth
                layoutHeight = layout.measuredHeight

//                tvLayoutWidth.text = "Layout Width: $layoutWidth"
//                tvLayoutHeight.text = "Layout Height: $layoutHeight"
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        viewPdf()
        getPdfSize()

        signatureImage.setupDraggable()
            .setAnimated(true)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .build()
    }

    private fun getPdfSize() {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(this)
        val fd: ParcelFileDescriptor? =
            selectedPdf.let { this.contentResolver.openFileDescriptor(it, "r") }
        val pdfDocument = pdfiumCore.newDocument(fd)
        pdfiumCore.openPage(pdfDocument, pageNumber)

        pdfWidth = pdfiumCore.getPageWidth(pdfDocument, pageNumber)
        pdfHeight = pdfiumCore.getPageHeight(pdfDocument, pageNumber)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                signatureFile = data?.getStringExtra("signatureFileName").toString()
                signaturePath = File(signatureFile)
                signatureBitmap = BitmapFactory.decodeFile(signaturePath.absolutePath)
                // resize the bitmap
                signatureBitmap = Bitmap.createScaledBitmap(
                    signatureBitmap,
                    signatureBitmap.width / 4,
                    signatureBitmap.height / 4,
                    false
                )
                signatureImage.setImageBitmap(signatureBitmap)
//                pdfView.fromUri(selectedPdf)
//                    .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
//                        canvas.drawBitmap(signatureBitmap, 500F, 1000F, null)
//                    }
//                    .load()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_sign_document -> {
                val intent = Intent(this, SignatureActivity::class.java)
                intent.putExtra("pdfWidth", pdfWidth)
                intent.putExtra("pdfHeight", pdfHeight)
                startActivityForResult(intent, 1)
                return true
            }
            R.id.action_save_document -> {
                savePdf()
//                showBottomSheetDialog()
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
                ) { _, _ -> savePdf() }
                .setNegativeButton(
                    "Exit"
                ) { _, _ -> finish() }.show()
        } else {
            finish()
        }
    }

    private fun viewPdf() {
        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            if (!TextUtils.isEmpty(viewType) || viewType != null) {
                if (viewType.equals("storage")) {
                    selectedPdf = Uri.parse((intent.getStringExtra("FileUri")))
                    pdfView.fromUri(selectedPdf)
                        .password(null)
                        .defaultPage(0)
                        .pages(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
//                        .onDraw { canvas, pageWidth, pageHeight, displayedPage -> }
                        .onPageError { page, _ ->
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

    private fun savePdf() {
        pdfBitmap = convertPdfToBitmap(this)!!
        signedPdf = combineTwoBitmap()

        val file = File(createFolderStorageDir(), "$savedFile.jpg")
        convertBitmapToJpg(file) //saved in Documents/Digital Signature
        convertJpgToPdf()        //saved in Documents/Digital Signature
//        deleteFile()
    }

    private fun convertPdfToBitmap(context: Context): Bitmap? {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(context)
        try {
            val fd: ParcelFileDescriptor? =
                selectedPdf.let { context.contentResolver.openFileDescriptor(it, "r") }
            val pdfDocument = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)

            val width = pdfiumCore.getPageWidth(pdfDocument, pageNumber)
            val height = pdfiumCore.getPageHeight(pdfDocument, pageNumber)

            pdfWidth = width
            pdfHeight = height

//            tvPdfWidth.text = pdfWidth.toString()
//            tvPdfHeight.text = pdfHeight.toString()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNumber, 0, 0, width, height)
            pdfiumCore.closeDocument(pdfDocument)

            return bitmap
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    private fun combineTwoBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(pdfBitmap.width, pdfBitmap.height, pdfBitmap.config)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(pdfBitmap, 0F, 0F, null)

        val left = signatureImage.x
        val top = signatureImage.y

        // Scale signature bitmap size
        signatureBitmap = Bitmap.createScaledBitmap(
            signatureBitmap,
            signatureBitmap.width * 3,
            signatureBitmap.height * 3,
            false
        )

        canvas.drawBitmap(signatureBitmap, left, top, null)
//        canvas.drawBitmap(signatureBitmap, 500F, 1000F, null)

        pdfBitmap.recycle()
        signatureBitmap.recycle()

        return bitmap
    }

    private fun convertBitmapToJpg(photo: File?) {
        val newBitmap =
            Bitmap.createBitmap(signedPdf.width, signedPdf.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.parseColor("#ffffff"))
        canvas.drawBitmap(signedPdf, 0f, 0f, null)

        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
    }

    private fun convertJpgToPdf() {
        val document = Document()

        // Initialize pdf filename
        PdfWriter.getInstance(document, FileOutputStream("$savedPath/$savedFile.pdf"))

        document.open()

        val image: Image = Image.getInstance("$savedPath/$savedFile.jpg")
        image.setAbsolutePosition(0F, 0F)
        image.borderWidth = 0F
        image.scaleAbsolute(PageSize.A4)

        document.add(image)
        document.close()
    }

//    private fun showBottomSheetDialog(){
//        val view = layoutInflater.inflate(R.layout.tool_list_document, null)
//
//        view.lyt_email.setOnClickListener {
//            mBottomSheetDialog.dismiss()
//            val contentUri = FileProvider.getUriForFile(
//                applicationContext, applicationContext.packageName + ".provider", currentFile
//            )
//            val target = Intent(Intent.ACTION_SEND)
//            target.type = "text/plain"
//            target.putExtra(Intent.EXTRA_STREAM, contentUri)
//            target.putExtra(Intent.EXTRA_SUBJECT, "Subject")
//            startActivity(Intent.createChooser(target, "Send via Email..."))
//        }
//
//        view.lyt_share.setOnClickListener {
//            mBottomSheetDialog.dismiss()
//            val contentUri = FileProvider.getUriForFile(
//                applicationContext, applicationContext.packageName + ".provider", currentFile
//            )
//            val target = ShareCompat.IntentBuilder.from(this@PDFViewerActivity)
//                .setStream(contentUri).intent
//            target.data = contentUri
//            target.type = "application/pdf"
//            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            if (target.resolveActivity(packageManager) != null) {
//                startActivity(target)
//            }
//        }
//
//        view.lyt_download.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//            startActivityForResult(
//                intent,
//                PDFViewerActivity.RQS_OPEN_DOCUMENT_TREE
//            )
//            selectedFile = currentFile
//        }
//    }

    private fun createFolderStorageDir(): File {
        val file = File(savedPath)
        if (!file.mkdirs()) {
            Log.e(savedFolder, "Directory not created")
        }
        return file
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

    private fun deleteFile() {
        val contentUri = "$savedPath$savedFile.jpg"
        val file = File(contentUri)
        file.delete()
        if (file.exists()) {
            file.canonicalFile.delete()
            if (file.exists()) {
                applicationContext.deleteFile(file.name)
            }
        }
    }
}
