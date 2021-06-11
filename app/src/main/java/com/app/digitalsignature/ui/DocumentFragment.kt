package com.app.digitalsignature.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.digitalsignature.PDFViewerActivity
import com.app.digitalsignature.R
import com.app.digitalsignature.DatabaseTest
import com.app.digitalsignature.adapter.DocumentAdapter
import com.app.digitalsignature.databinding.FragmentDocumentBinding
import com.app.digitalsignature.db.DocumentHelper
import com.app.digitalsignature.entity.Document
import com.app.digitalsignature.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.models.FileData
import kotlinx.android.synthetic.main.fragment_document.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DocumentFragment : Fragment(R.layout.fragment_document) {

    private lateinit var binding: FragmentDocumentBinding
    private var pickFilesFactory: IPickFilesFactory? = null

    private lateinit var adapter: DocumentAdapter
    private lateinit var documentHelper: DocumentHelper

    companion object {
        private const val PICK_PDF_CODE = 1000
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setListener()

        rv_documents.layoutManager = LinearLayoutManager(activity)
        rv_documents.setHasFixedSize(true)
        adapter = DocumentAdapter(this)
        rv_documents.adapter = adapter

        binding.addFile.setOnClickListener {
            val intent = Intent(activity, DatabaseTest::class.java)
            startActivityForResult(intent, DatabaseTest.REQUEST_ADD)
        }

        documentHelper = DocumentHelper.getInstance(requireActivity().application)
        documentHelper.open()

        if (savedInstanceState == null) {
            loadDocumentsAsync()
        } else {
            val list =
                savedInstanceState.getParcelableArrayList<Document>(EXTRA_STATE)
            if (list != null) {
                adapter.listDocuments = list
            }
        }
    }

    private fun loadDocumentsAsync() {
        GlobalScope.launch(Dispatchers.Main) {
//            progressbar.visibility = View.VISIBLE
            val deferredDocuments = async(Dispatchers.IO) {
                val cursor = documentHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
//            progressbar.visibility = View.INVISIBLE
            val notes = deferredDocuments.await()
            if (notes.size > 0) {
                adapter.listDocuments = notes
            } else {
                adapter.listDocuments = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        documentHelper.close()
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_documents, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener(){
        binding.addFile.setOnClickListener {
            val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
            pdfIntent.type = "application/pdf"
            pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(
                Intent.createChooser(pdfIntent,"Select PDF"),
                PICK_PDF_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, pickFilesCallback)

        if (requestCode == PICK_PDF_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPDF = data.data

            val intent = Intent(activity, PDFViewerActivity::class.java)
            intent.putExtra("ViewType", "storage")
            intent.putExtra("FileUri", selectedPDF.toString())
            startActivity(intent)
        }
    }

    private val pickFilesCallback = object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Toast.makeText(activity, "Action canceled", Toast.LENGTH_SHORT).show()
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            when (errorModel.errorStatus) {
                ErrorStatus.DATA_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.FILE_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.PICK_ERROR -> showToastMessage(errorModel.errorMessage)
            }
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {}
    }

    private fun showToastMessage(@StringRes message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}