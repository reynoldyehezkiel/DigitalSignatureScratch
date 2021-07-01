package com.app.digitalsignature.ui.document

//import com.itextpdf.text.*
//import com.itextpdf.text.pdf.*
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
import com.benzveen.pdfdigitalsignature.DigitalSignatureActivity
import com.benzveen.pdfdigitalsignature.Document.PDSViewPager
import com.benzveen.pdfdigitalsignature.PDF.PDSPDFDocument
import com.benzveen.pdfdigitalsignature.imageviewer.PDSPageAdapter
import com.shockwave.pdfium.PdfiumCore
import io.github.hyuwah.draggableviewlib.*
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.*


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu
    private lateinit var selectedPdf: Uri
    private lateinit var signatureBitmap: Bitmap
    private lateinit var pdfBitmap: Bitmap
    private lateinit var signedPDF: Bitmap
    private lateinit var signatureFile: String
    private lateinit var signaturePath: File
    private lateinit var doc: PDSPDFDocument
    private lateinit var pdfView: PDSViewPager
    private val savedFile = "SignedPDF"
    private val savedFolder = "Digital Signature"
    private val savedPath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .toString() + "/$savedFolder/"
    private var isSigned: Boolean = false
    private var layoutWidth = 0
    private var layoutHeight = 0
    private var pdfWidth = 0
    private var pdfHeight = 0
    private lateinit var mCtx: DigitalSignatureActivity

    companion object{
        private const val READ_REQUEST_CODE = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        pdfView = viewPager

//        val layout = findViewById<View>(R.id.pdfView) as RelativeLayout
//        val vto = layout.viewTreeObserver
//        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            @SuppressLint("SetTextI18n")
//            override fun onGlobalLayout() {
//                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                layoutWidth = layout.measuredWidth
//                layoutHeight = layout.measuredHeight
//
//                tvLayoutWidth.text = "Layout Width: $layoutWidth"
//                tvLayoutHeight.text = "Layout Height: $layoutHeight"
//            }
//        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

//        viewPdf()

        val message = intent.getStringExtra("ActivityAction")
        if (message == "FileSearch") {
            performFileSearch()
        }

//        signatureImage.setupDraggable()
//            .setAnimated(true)
//            .setStickyMode(DraggableView.Mode.NON_STICKY)
//            .build()
    }

    @SuppressLint("SetTextI18n")
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
//                signatureImage.setImageBitmap(signatureBitmap)

                invokeMenuButton(true)
            }
        }

        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedPdf = data.data!!
                    DigitalSignatureActivity().openPDFViewer(selectedPdf)
                }
            } else {
                finish()
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

    private fun openPDFViewer(selectedPdf: Uri) {
        try {
            val document = PDSPDFDocument(this, selectedPdf)
            document.open()
            this.doc = document
            val imageAdapter = PDSPageAdapter(supportFragmentManager, document)
            pdfView.adapter = imageAdapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@PDFViewerActivity,
                "Cannot open PDF, either PDF is corrupted or password protected",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/jpeg"
        val mimetypes = arrayOf("application/pdf")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    private fun viewPdf() {
        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            if (!TextUtils.isEmpty(viewType) || viewType != null) {
                if (viewType.equals("storage")) {
//                    pdfView.fromUri(selectedPdf)
//                        .password(null)
//                        .defaultPage(0)
//                        .pages(0)
//                        .enableSwipe(true)
//                        .swipeHorizontal(false)
//                        .enableDoubletap(true)
////                        .onDraw { canvas, pageWidth, pageHeight, displayedPage -> }
//                        .onPageError { page, _ ->
//                            Toast.makeText(
//                                this,
//                                "Error while opening the page$page",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                        }.onTap { false }
//                        .enableAnnotationRendering(true)
//                        .load()
                }
            }
        }
    }

    private fun savePdf() {
        pdfBitmap = convertPdfToBitmap(this)!!
        signedPDF = combineTwoBitmap()

        /*
            method 1
            convert bitmap to jpg,
            then convert jpg to pdf
        */
        val file = File(createFolderStorageDir(), "$savedFile.jpg")
        convertBitmapToJpg(file)
//        convertJpgToPdf()
//        deleteFile()

        /*
            method 2
            convert bitmap to pdf
        */
//        createFolderStorageDir()
//        convertBitmapToPdf()
    }

    private fun convertBitmapToJpg(photo: File?) {
        val newBitmap =
            Bitmap.createBitmap(signedPDF.width, signedPDF.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.parseColor("#ffffff"))
        canvas.drawBitmap(signedPDF, 0f, 0f, null)

        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
    }

//    private fun convertJpgToPdf() {
//        val document = Document()
//
//        // Change pdf filename
//        PdfWriter.getInstance(document, FileOutputStream("$savedPath/$savedFile.pdf"))
//
//        document.open()
//
//        val image: Image = Image.getInstance("$savedPath/$savedFile.jpg")
//
//        val scaler: Float =
//            (document.pageSize.width - document.leftMargin() - document.rightMargin() - 0) / image.width * 100
//        image.scalePercent(scaler)
//
//        document.add(image)
//        document.close()
//    }

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

    private fun convertBitmapToPdf() {
        val pdfDocument = PdfDocument()
        val pi = Builder(signedPDF.width, signedPDF.height, 1).create()

        val page: Page = pdfDocument.startPage(pi)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawPaint(paint)

        val bitmap = Bitmap.createScaledBitmap(signedPDF, signedPDF.width, signedPDF.height, true)
        paint.color = Color.BLACK
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        pdfDocument.finishPage(page)

        val pdfFile = File(savedPath, "$savedFile.pdf")
        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        pdfDocument.close()
    }

    private fun combineTwoBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(pdfBitmap.width, pdfBitmap.height, pdfBitmap.config)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(pdfBitmap, Matrix(), null)

//        val left = signatureImage.x * (pdfWidth/layoutWidth)
//        val top = signatureImage.y * (pdfHeight/layoutHeight)
        signatureBitmap = Bitmap.createScaledBitmap(
            signatureBitmap,
            signatureBitmap.width * 3,
            signatureBitmap.height * 3,
            false
        )
//        canvas.drawBitmap(signatureBitmap, left, top, null)

        pdfBitmap.recycle()
        signatureBitmap.recycle()

        return bitmap
    }

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
