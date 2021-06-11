package com.app.digitalsignature

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pdfviewer.*

class PDFViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                        .onDraw { canvas, pageWidth, pageHeight, displayedPage ->

                        }.onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->

                        }.onPageChange { page, pageCount ->

                        }.onPageError { page, t ->
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.document_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}