package com.app.digitalsignature.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.app.digitalsignature.db.DatabaseContract.DocumentColumns
import com.app.digitalsignature.db.DatabaseContract.DocumentColumns.Companion.TABLE_NAME

internal class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "dbdigitalsignatureapp"
        private const val DATABASE_VERSION = 1
        private const val SQL_CREATE_TABLE_DESCRIPTION = "CREATE TABLE $TABLE_NAME" +
                " (${DocumentColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DocumentColumns.FILENAME} TEXT NOT NULL," +
                " ${DocumentColumns.SIZE} TEXT NOT NULL," +
                " ${DocumentColumns.DATE} TEXT NOT NULL)"
//        private const val SQL_CREATE_TABLE_SIGNATURE = "CREATE TABLE $TABLE_NAME" +
//                " (${DocumentColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
//                " ${DocumentColumns.FILENAME} TEXT NOT NULL," +
//                " ${DocumentColumns.SIZE} TEXT NOT NULL," +
//                " ${DocumentColumns.DATE} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_DESCRIPTION)
//        db.execSQL(SQL_CREATE_TABLE_SIGNATURE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

}