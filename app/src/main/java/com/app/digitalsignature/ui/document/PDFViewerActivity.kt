package com.app.digitalsignature.ui.document

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import io.github.hyuwah.draggableviewlib.DraggableView
import io.github.hyuwah.draggableviewlib.setupDraggable
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.File


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu

    private val fileName = "Signature_test3"
    private val folderName = "Digital Signature"
    private val directory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/$folderName/" + ".nomedia/"
    private val filePath = File("$directory$fileName.png")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            if (!TextUtils.isEmpty(viewType) || viewType != null) {
                if (viewType.equals("storage")) {
                    val selectedPDF = Uri.parse((intent.getStringExtra("FileUri")))
                    pdfView.fromUri(selectedPDF)
                        .password(null)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
//                        .onDraw { canvas, pageWidth, pageHeight, displayedPage -> }
//                        .onDrawAll { canvas, pageWidth, pageHeight, displayedPage -> }
//                        .onPageChange { page, pageCount -> }
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

        signatureImage.setupDraggable()
            .setAnimated(true)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .build()

        val myBitmap = BitmapFactory.decodeFile(filePath.absolutePath)
        signatureImage.setImageBitmap(myBitmap)

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode === 1) {
//            if (resultCode === RESULT_OK) {
//                val signatureFile : String = intent.getStringExtra("signatureFile").toString()
//                val myBitmap = BitmapFactory.decodeFile(filePath.absolutePath)
//                signatureImage.setImageBitmap(myBitmap)
//            }
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.document_menu, menu)
        this.mMenu = menu
        val saveItem: MenuItem = mMenu.findItem(R.id.action_save)
        saveItem.icon.alpha = 130
        val signItem: MenuItem = mMenu.findItem(R.id.action_sign)
        signItem.icon.alpha = 255
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_sign -> {
                val intent = Intent(this, SignatureActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}