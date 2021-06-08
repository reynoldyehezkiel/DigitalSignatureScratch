package com.app.digitalsignature

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.app.digitalsignature.databinding.ActivityMainBinding
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.models.FileData

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var pickFilesFactory: IPickFilesFactory

    companion object {
        private const val PICK_PDF_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // add file
        binding.appBarMain.fab.setOnClickListener { view ->
            setListener()
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_document, R.id.nav_signature,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun setListener() {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(pdfIntent,"Select PDF"),
            PICK_PDF_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, pickFilesCallback)

        if (requestCode == PICK_PDF_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPDF = data.data

            val intent = Intent(this@MainActivity, PDFViewerActivity::class.java)
            intent.putExtra("ViewType", "storage")
            intent.putExtra("FileUri", selectedPDF.toString())
//            intent.putExtra("SignatureBitmap", signatureBitmap)
            startActivity(intent)
        }
    }

    private val pickFilesCallback = object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Toast.makeText(this@MainActivity, "Action canceled", Toast.LENGTH_SHORT).show()
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            when (errorModel.errorStatus) {
                ErrorStatus.DATA_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.FILE_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.PICK_ERROR -> showToastMessage(errorModel.errorMessage)
            }
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {

        }
    }

    private fun showToastMessage(@StringRes message: Int) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}