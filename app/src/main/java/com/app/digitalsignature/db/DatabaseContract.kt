package com.app.digitalsignature.db

import android.provider.BaseColumns

internal class DatabaseContract {
    internal class DocumentColumns: BaseColumns {
        companion object {
            const val TABLE_NAME = "document"
            const val _ID = "_id"
            const val FILENAME = "filename"
            const val SIZE = "size"
            const val DATE = "date"
        }
    }
//    internal class SignatureColumns: BaseColumns {
//        companion object {
//            const val TABLE_NAME = "document"
//            const val _ID = "_id"
//            const val FILENAME = "filename"
//            const val SIZE = "size"
//            const val DATE = "date"
//        }
//    }
}