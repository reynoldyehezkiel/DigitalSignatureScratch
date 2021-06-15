package com.app.digitalsignature.ui.signature

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.digitalsignature.R
import com.app.digitalsignature.databinding.FragmentSignatureBinding

class SignatureFragment : Fragment(R.layout.fragment_signature) {

    private lateinit var binding: FragmentSignatureBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignatureBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
    }

    private fun setListener(){
        binding.createSignature.setOnClickListener {
            val intent = Intent (activity, SignatureActivity::class.java)
            startActivity(intent)
        }
    }
}