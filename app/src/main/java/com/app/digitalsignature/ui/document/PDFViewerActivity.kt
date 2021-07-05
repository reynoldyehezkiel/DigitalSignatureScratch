package com.app.digitalsignature.ui.document

import android.app.AlertDialog
import android.content.*
import android.graphics.*
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.shockwave.pdfium.PdfiumCore
import io.github.hyuwah.draggableviewlib.*
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.*


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu
    private lateinit var selectedPdf: Uri
    private lateinit var signatureBitmap: Bitmap
    private lateinit var pdfBitmap: Bitmap
    private lateinit var signedPdf: Bitmap
    private lateinit var signatureFile: String
    private lateinit var signaturePath: File

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

//        val layout = findViewById<View>(R.id.pdfView) as RelativeLayout
//        val vto = layout.viewTreeObserver
//        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
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

        viewPdf()

        signatureImage.setupDraggable()
            .setAnimated(true)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .build()
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
//                signatureImage.setImageBitmap(signatureBitmap)
                pdfView.fromUri(selectedPdf)
                    .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                        canvas.drawBitmap(signatureBitmap, 0F, 0F, null)
                    }
                    .load()

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

        // Change pdf filename
        PdfWriter.getInstance(document, FileOutputStream("$savedPath/$savedFile.pdf"))

        document.open()

        val image: Image = Image.getInstance("$savedPath/$savedFile.jpg")
        image.setAbsolutePosition(0F, 0F)
        image.borderWidth = 0F
        image.scaleAbsolute(PageSize.A4)

        document.add(image)
        document.close()
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

            tvPdfWidth.text = pdfWidth.toString()
            tvPdfHeight.text = pdfHeight.toString()

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
        canvas.drawBitmap(pdfBitmap, Matrix(), null)

//        val left = signatureImage.x * (pdfWidth / layoutWidth)
//        val top = signatureImage.y * (pdfHeight / layoutHeight)
        signatureBitmap = Bitmap.createScaledBitmap(
            signatureBitmap,
            signatureBitmap.width * 3,
            signatureBitmap.height * 3,
            false
        )
        canvas.drawBitmap(signatureBitmap, 0F, 0F, null)

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
