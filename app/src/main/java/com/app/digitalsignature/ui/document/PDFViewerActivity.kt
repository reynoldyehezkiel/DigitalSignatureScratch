package com.app.digitalsignature.ui.document

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.R
import com.app.digitalsignature.ui.signature.SignatureActivity
import kotlinx.android.synthetic.main.activity_pdfviewer.*

class PDFViewerActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu

    companion object{
        const val ALERT_DIALOG_CLOSE = 10
    }

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
    }

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