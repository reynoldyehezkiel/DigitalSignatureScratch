package com.app.digitalsignature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_signature.*

class SignatureActivity : AppCompatActivity() {

//    private lateinit var binding: ActivitySignatureBinding
//    private val folderName = "Digital Signature"
//    private val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//        .toString() + "/$folderName/"

//    companion object {
//        private const val REQUEST_EXTERNAL_STORAGE = 1
//
//        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        fun verifyStoragePermissions(activity: SignatureActivity?) {
//            val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
//            }
//        }
//    }

//    private var fileName = intent.getStringExtra(FILENAME)
//    private val folderName = "Digital Signature"
//    private val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//        .toString() + "/$folderName/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        btnClear.isEnabled = false
        btnSave.isEnabled = false

        //draw signature
        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {}

            override fun onSigned() {
                btnSave.isEnabled = true
                btnClear.isEnabled = true
            }

            override fun onClear() {
                btnSave.isEnabled = false
                btnClear.isEnabled = false
            }
        })

        //clear signature
        btnClear.setOnClickListener {
            signaturePad.clear()
        }

        //next to File Picker activity
        btnSave.setOnClickListener {
//            val signatureBitmap = signaturePad.signatureBitmap
//
//            if (addJpgSignatureToGallery(signatureBitmap)) {
//                deleteFile()
//                Toast.makeText(this@SignatureActivity, "The signature is saved in Documents folder",
//                    Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this@SignatureActivity, "Unable to save Signature",
//                    Toast.LENGTH_SHORT).show()
//            }
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
//            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this@SignatureActivity, "Cannot write images to the Storage Media",
//                    Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun createAlbumStorageDir(): File {
//        val file = File(directory)
//        if (!file.mkdirs()) {
//            Log.e(folderName, "Directory not created")
//        }
//        return file
//    }
//
//    //Image Signature
//    @Throws(IOException::class)
//    fun saveBitmapToJPG(bitmap: Bitmap, photo: File?) {
//        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(newBitmap)
//        val stream: OutputStream = FileOutputStream(photo)
//
//        canvas.drawColor(Color.WHITE)
//        canvas.drawBitmap(bitmap,  0f, 0f, null)
//
//        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
//
//        stream.close()
//    }
//
//    private fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
//        var result = false
//        try {
//            val photo = File(createAlbumStorageDir(), String.format("Signature_%d.jpg",
//                System.currentTimeMillis()))
//            saveBitmapToJPG(signature, photo)
//            scanMediaFile(photo)
//            result = true
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return result
//    }
//
//    private fun scanMediaFile(photo: File) {
//        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//        val contentUri = Uri.fromFile(photo)
//        mediaScanIntent.data = contentUri
//        this@SignatureActivity.sendBroadcast(mediaScanIntent)
//    }
//
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