package com.app.digitalsignature.ui.signature

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.digitalsignature.R
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_signature.*
import java.io.*

class SignatureActivity : AppCompatActivity() {

    private val folderName = "Digital Signature"
    private val directory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/$folderName/" + ".nomedia"

    private lateinit var mMenu: Menu
    private lateinit var saveSignature: MenuItem
    private lateinit var clearSignature: MenuItem

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Draw Signature"

        //draw signature
        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {}

            // enable icon button when start signing
            override fun onSigned() {
                saveSignature.isEnabled = true
                saveSignature.icon.alpha = 255
                clearSignature.isEnabled = true
                clearSignature.icon.alpha = 255
            }

            // disable icon button when clear the signature
            override fun onClear() {
                saveSignature.isEnabled = false
                saveSignature.icon.alpha = 130
                clearSignature.isEnabled = false
                clearSignature.icon.alpha = 130
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.signature_menu, menu)
        this.mMenu = menu
        saveSignature = menu.findItem(R.id.action_save)
        saveSignature.isEnabled = false
        saveSignature.icon.alpha = 130
        clearSignature = menu.findItem(R.id.action_clear)
        clearSignature.isEnabled = false
        clearSignature.icon.alpha = 130

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            // clear signature
            R.id.action_clear -> {
                signaturePad.clear()
            }
            // save signature
            R.id.action_save -> {
                saveSignature()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@SignatureActivity, "Cannot write images to the Storage Media",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveSignature(){
//        val signatureBitmap = signaturePad.transparentSignatureBitmap
        val signatureBitmap = signaturePad.getTransparentSignatureBitmap(true)

        if (addJpgSignatureToGallery(signatureBitmap)) {
//                deleteFile()
            Toast.makeText(
                this@SignatureActivity, "The signature is saved successfully",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Toast.makeText(
                this@SignatureActivity, "Unable to save Signature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createAlbumStorageDir(): File {
        val file = File(directory)
        if (!file.mkdirs()) {
            Log.e(folderName, "Directory not created")
        }
        return file
    }

    //Image Signature
    @Throws(IOException::class)
    fun saveBitmapToJPG(bitmap: Bitmap, photo: File?) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
        stream.close()
    }

    private fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(
                createAlbumStorageDir(), String.format(
                    "Signature_%d.jpg",
                    System.currentTimeMillis()
                )
            )
            saveBitmapToJPG(signature, photo)
            scanMediaFile(photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        this@SignatureActivity.sendBroadcast(mediaScanIntent)
    }

//    private fun deleteFile(){
//        val contentUri = "$directory$fileName.jpg"
//        val file = File(contentUri)
//        file.delete()
//        if (file.exists()) {
//            file.canonicalFile.delete()
//            if (file.exists()) {
//                applicationContext.deleteFile(file.name)
//            }
//        }
//    }
}