package com.app.digitalsignature

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.app.digitalsignature.db.DatabaseContract
import com.app.digitalsignature.db.DatabaseContract.DocumentColumns.Companion.DATE
import com.app.digitalsignature.db.DocumentHelper
import com.app.digitalsignature.entity.Document
import kotlinx.android.synthetic.main.activity_test.*
import java.text.SimpleDateFormat
import java.util.*

class DatabaseTest : AppCompatActivity(), View.OnClickListener {
    private var isEdit = false
    private var document: Document? = null
    private var position: Int = 0
    private lateinit var documentHelper: DocumentHelper

    companion object {
        const val EXTRA_DOCUMENT = "extra_document"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        documentHelper = DocumentHelper.getInstance(applicationContext)

        document = intent.getParcelableExtra(EXTRA_DOCUMENT)
        if(document != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            document = Document()
        }

        val actionBarTitle: String
        val btnTitle: String
        if (isEdit) {
            actionBarTitle = "Ubah (Tes Database)"
            btnTitle = "Update"
            document?.let {
                edt_filename.setText(it.fileName)
                edt_size.setText(it.size)
            }
        } else {
            actionBarTitle = "Tambah (Tes Database)"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_submit.text = btnTitle
        btn_submit.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit) {
            val fileName = edt_filename.text.toString().trim()
            val size = edt_size.text.toString().trim()

            if (fileName.isEmpty()) {
                edt_filename.error = "Field can not be blank"
                return
            }

            document?.fileName = fileName
            document?.size = size

            val intent = Intent()
            intent.putExtra(EXTRA_DOCUMENT, document)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()

            values.put(DatabaseContract.DocumentColumns.FILENAME, fileName)
            values.put(DatabaseContract.DocumentColumns.SIZE, size)

            if (isEdit) {
                val result = documentHelper.update(document?.id.toString(), values).toLong()
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this@DatabaseTest, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                }
            } else {
                document?.date = getCurrentDate()
                values.put(DATE, getCurrentDate())
                val result = documentHelper.insert(values)

                if (result > 0) {
                    document?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast.makeText(this@DatabaseTest, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Document"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result =
                        documentHelper.deleteById(document?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@DatabaseTest,
                            "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}