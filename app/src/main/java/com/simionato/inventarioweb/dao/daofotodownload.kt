package com.simionato.inventarioweb.dao

import android.content.ContentValues
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.models.FotoUpload

class daofotodownload(private val dbHelper: DatabaseHelper) {
    fun insertPhoto(name: String, path: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, name)
            put(DatabaseHelper.COLUMN_PATH, path)
            put(DatabaseHelper.COLUMN_STATUS, 0) // Status inicial: não processado
        }
        return db.insert(DatabaseHelper.TABLE_NAME, null, values)
    }

    fun updatePhotoStatus(photoId: Int, status: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, status)
        }
        db.update(DatabaseHelper.TABLE_NAME, values, "${DatabaseHelper.COLUMN_ID}=?", arrayOf(photoId.toString()))
    }

    fun getPendingPhotos(): List<FotoUpload> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_NAME,
            arrayOf(DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_PATH),
            "${DatabaseHelper.COLUMN_STATUS} IN (0, 3)", // Filtra apenas as pendentes
            null, null, null, null
        )

        val photos = mutableListOf<FotoUpload>()
        while (cursor.moveToNext()) {
            photos.add(
                FotoUpload(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATH)),
                    0
                )
            )
        }
        cursor.close()
        return photos
    }
}