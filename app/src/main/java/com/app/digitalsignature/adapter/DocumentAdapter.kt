package com.app.digitalsignature.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.digitalsignature.CustomOnItemClickListener
import com.app.digitalsignature.R
import com.app.digitalsignature.entity.Document
//import com.app.digitalsignature.DocumentAddUpdateActivity
import kotlinx.android.synthetic.main.item_document.view.*

class DocumentAdapter(private val activity: Activity) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    private var listDocuments = ArrayList<Document>()
        set(listDocuments) {
            if (listDocuments.size > 0) {
                this.listDocuments.clear()
            }
            this.listDocuments.addAll(listDocuments)

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(listDocuments[position])
    }

    override fun getItemCount(): Int = this.listDocuments.size

    fun addItem(document: Document) {
        this.listDocuments.add(document)
        notifyItemInserted(this.listDocuments.size - 1)
    }

    fun updateItem(position: Int, document: Document) {
        this.listDocuments[position] = document
        notifyItemChanged(position, document)
    }

    fun removeItem(position: Int) {
        this.listDocuments.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listDocuments.size)
    }

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(document: Document) {
            with(itemView){
                fileNameTextView.text = document.fileName
                dateTimeTextView.text = document.date
                sizeTextView.text = document.size
                itemDocumentCardView.setOnClickListener(CustomOnItemClickListener(adapterPosition, object : CustomOnItemClickListener.OnItemClickCallback {
                    override fun onItemClicked(view: View, position: Int) {
//                        val intent = Intent(activity, DocumentAddUpdateActivity::class.java)
//                        intent.putExtra(DocumentAddUpdateActivity.EXTRA_POSITION, position)
//                        intent.putExtra(DocumentAddUpdateActivity.EXTRA_NOTE, document)
//                        activity.startActivityForResult(intent, DocumentAddUpdateActivity.REQUEST_UPDATE)
                    }
                }))
            }
        }
    }

}