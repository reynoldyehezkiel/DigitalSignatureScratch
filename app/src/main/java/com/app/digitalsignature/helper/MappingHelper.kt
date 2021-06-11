package com.app.digitalsignature.helper

import android.database.Cursor
import com.app.digitalsignature.db.DatabaseContract
import com.app.digitalsignature.entity.Document

object MappingHelper {
    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<Document> {
        val notesList = ArrayList<Document>()

        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.DocumentColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.DocumentColumns.FILENAME))
                val description =
                    getString(getColumnIndexOrThrow(DatabaseContract.DocumentColumns.SIZE))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.DocumentColumns.DATE))
                notesList.add(Document(id, title, description, date))
            }
        }

        return notesList
    }
}