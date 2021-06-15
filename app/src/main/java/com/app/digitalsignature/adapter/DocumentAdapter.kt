package com.app.digitalsignature.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.digitalsignature.*
import com.app.digitalsignature.entity.Document
import kotlinx.android.synthetic.main.item_document.view.*


class DocumentAdapter(private val activity: MainActivity) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    var listDocuments = ArrayList<Document>()
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
                        val intent = Intent(context, DatabaseTest::class.java)
                        intent.putExtra(DatabaseTest.EXTRA_POSITION, position)
                        intent.putExtra(DatabaseTest.EXTRA_DOCUMENT, document)
                        activity.startActivityForResult(intent, DatabaseTest.REQUEST_UPDATE)
                    }
                }))
            }
        }
    }

}