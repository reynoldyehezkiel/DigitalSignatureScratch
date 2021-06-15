package com.app.digitalsignature.ui.signature

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.app.digitalsignature.R

class SignatureViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature_view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Signatures"

        val mFragmentManager = supportFragmentManager
        val mHomeFragment = SignatureFragment()
        val fragment = mFragmentManager.findFragmentByTag(SignatureFragment::class.java.simpleName)

        if (fragment !is SignatureFragment) {
            Log.d("DigitalSignature", "Fragment Name :" + SignatureFragment::class.java.simpleName)
            mFragmentManager
                .beginTransaction()
                .add(R.id.frame_container_signature, mHomeFragment, SignatureFragment::class.java.simpleName)
                .commit()
        }
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
        this.finish()
    }
}