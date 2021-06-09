package com.app.digitalsignature

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.databinding.ActivityPdfviewerBinding

class PDFViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfviewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            if (!TextUtils.isEmpty(viewType) || viewType != null) {
                if (viewType.equals("storage")) {
                    val selectedPDF = Uri.parse((intent.getStringExtra("FileUri")))
                    binding.pdfView.fromUri(selectedPDF)
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
}