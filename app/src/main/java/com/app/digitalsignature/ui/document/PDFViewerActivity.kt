package com.app.digitalsignature.ui.document

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo.Builder
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import com.shockwave.pdfium.PdfiumCore
import io.github.hyuwah.draggableviewlib.*
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu
    private lateinit var selectedPDF: Uri
    private lateinit var signatureBitmap: Bitmap
    private lateinit var pdfBitmap: Bitmap
    private var isSigned: Boolean = false
    private lateinit var signedPDF: Bitmap

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

        viewPdf()

        signatureImage.setupDraggable()
            .setAnimated(true)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .setListener(signatureDragListener)
            .build()
    }

    @SuppressLint("SetTextI18n")
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
                savePdf()
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
                    selectedPDF = Uri.parse((intent.getStringExtra("FileUri")))
                    pdfView.fromUri(selectedPDF)
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
        pdfBitmap = convertPdfToBitmap(selectedPDF, this)!!
        signedPDF = combineTwoBitmap(pdfBitmap, signatureBitmap)

        val fileName = "SignedPDF"
        val folderName = "Digital Signature"
        val filePath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/$folderName/"

        val pdf = File(
            createFolderStorageDir(filePath, folderName),
            "$fileName.jpg"
        )

        /*
            method 1
            convert bitmap to jpg,
            then convert jpg to pdf
        */
        convertBitmapToJpg(signedPDF, pdf)
        convertJpgToPdf(filePath,fileName)
//        deleteFile(filePath,fileName)

        /*
            method 2
            convert bitmap to pdf
        */
//        createFolderStorageDir(filePath, folderName)
//        convertBitmapToPdf(filePath, signedPDF, fileName)
    }

    private fun convertBitmapToJpg(bitmap: Bitmap, photo: File?) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
        stream.close()
    }

    private fun convertJpgToPdf(directory: String, fileName: String) {
        val document = Document()

        // Change pdf filename
        PdfWriter.getInstance(
            document,
            FileOutputStream("$directory/$fileName.pdf")
        )

        document.open()

        val image: Image = Image.getInstance("$directory/$fileName.jpg")

//        val scaler: Float = (document.pageSize.width - document.leftMargin()
//                - document.rightMargin() - 0) / image.width * 100

//        image.scalePercent(scaler)
//        image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP

        document.add(image)
        document.close()
    }

    private fun deleteFile(directory: String, fileName: String){
        val contentUri = "$directory$fileName.jpg"
        val file = File(contentUri)
        file.delete()
        if (file.exists()) {
            file.canonicalFile.delete()
            if (file.exists()) {
                applicationContext.deleteFile(file.name)
            }
        }
    }

    private fun convertPdfToBitmap(pdfUri: Uri?, context: Context): Bitmap? {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(context)
        try {
            val fd: ParcelFileDescriptor? =
                pdfUri?.let { context.contentResolver.openFileDescriptor(it, "r") }
            val pdfDocument = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)

            val width = pdfiumCore.getPageWidth(pdfDocument, pageNumber)/3
            val height = pdfiumCore.getPageHeight(pdfDocument, pageNumber)/3

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNumber, 0, 0, width, height)
            pdfiumCore.closeDocument(pdfDocument)

            return bitmap
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    private fun convertBitmapToPdf(filePath: String, signedPDF: Bitmap?, fileName: String) {
        val pdfDocument = PdfDocument()
        val pi = Builder(signedPDF!!.width, signedPDF.height, 1).create()

        val page: Page = pdfDocument.startPage(pi)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawPaint(paint)

        val bitmap = Bitmap.createScaledBitmap(signedPDF, signedPDF.width, signedPDF.height, true)
        paint.color = Color.BLACK
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        pdfDocument.finishPage(page)

        val pdfFile = File(filePath, "$fileName.pdf")
        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        pdfDocument.close()
    }

    private fun combineTwoBitmap(pdfBitmap: Bitmap, signatureBitmap: Bitmap): Bitmap {
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

    private fun createFolderStorageDir(directory: String, folderName: String): File {
        val file = File(directory)
        if (!file.mkdirs()) {
            Log.e(folderName, "Directory not created")
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

}