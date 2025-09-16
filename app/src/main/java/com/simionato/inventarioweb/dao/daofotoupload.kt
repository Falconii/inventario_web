package com.simionato.inventarioweb.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.models.FotoUploadModel

class daoFotoUpload(private val dbHelper: DatabaseHelper) {

    fun insertFoto(photo: FotoUploadModel): Boolean {
        val db = dbHelper.writableDatabase
        val values = toContentValues(photo)
        val result = db.insert(DatabaseHelper.TABLE_PHOTOS, null, values)
        db.close()
        return result != -1L
    }

    fun getPhotosAll(): List<FotoUploadModel> {
        val db = dbHelper.readableDatabase
        val photos = mutableListOf<FotoUploadModel>()
        db.query(DatabaseHelper.TABLE_PHOTOS, null, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                photos.add(fromCursor(cursor))
            }
        }
        db.close()
        return photos
    }

    fun getFotosUpLoad(): List<FotoUploadModel> {
        val db = dbHelper.readableDatabase
        val photos = mutableListOf<FotoUploadModel>()
        db.query(DatabaseHelper.TABLE_PHOTOS, null, "${DatabaseHelper.COLUMN_STATUS_UPLOAD} = 0", null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                photos.add(fromCursor(cursor))
            }
        }
        db.close()
        return photos
    }

    fun getById(id: Int): FotoUploadModel? {
        val db = dbHelper.readableDatabase
        var photo: FotoUploadModel? = null
        db.query(
            DatabaseHelper.TABLE_PHOTOS,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                photo = fromCursor(cursor)
            }
        }
        db.close()
        return photo
    }

    fun updateFoto(photo: FotoUploadModel): Boolean {
        val db = dbHelper.writableDatabase
        val values = toContentValues(photo)
        val result = db.update(
            DatabaseHelper.TABLE_PHOTOS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(photo.id.toString())
        )
        db.close()
        return result > 0
    }

    fun deleteFoto(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete(
            DatabaseHelper.TABLE_PHOTOS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return result > 0
    }

    fun clearTable(): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_PHOTOS, null, null)
        db.close()
        return result > 0
    }

    private fun toContentValues(photo: FotoUploadModel): ContentValues {
        return ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID_EMPRESA, photo.idEmpresa)
            put(DatabaseHelper.COLUMN_ID_LOCAL, photo.idLocal)
            put(DatabaseHelper.COLUMN_ID_INVENTARIO, photo.idInventario)
            put(DatabaseHelper.COLUMN_ID_IMOBILIZADO, photo.idImobilizado)
            put(DatabaseHelper.COLUMN_ID_PASTA, photo.idPasta)
            put(DatabaseHelper.COLUMN_ID_FILE, photo.idFile)
            put(DatabaseHelper.COLUMN_FILE_NAME, photo.fileName)
            put(DatabaseHelper.COLUMN_FILE_NAME_ORIGINAL, photo.fileNameOriginal)
            put(DatabaseHelper.COLUMN_ID_USUARIO, photo.idUsuario)
            put(DatabaseHelper.COLUMN_DATA, photo.data)
            put(DatabaseHelper.COLUMN_DESTAQUE, photo.destaque)
            put(DatabaseHelper.COLUMN_OBS, photo.obs)
            put(DatabaseHelper.COLUMN_LOCALIZACAO, photo.localizacao)
            put(DatabaseHelper.COLUMN_DESCRICAO, photo.descricao)
            put(DatabaseHelper.COLUMN_RAZAO, photo.razao)
            put(DatabaseHelper.COLUMN_STATUS_UPLOAD, photo.status_upload)
            put(DatabaseHelper.COLUMN_DATA_UPLOAD, photo.data_upload)
            put(DatabaseHelper.COLUMN_USER_INSERT, photo.userInsert)
            put(DatabaseHelper.COLUMN_USER_UPDATE, photo.userUpdate)
        }
    }

    private fun fromCursor(cursor: Cursor): FotoUploadModel {
        return FotoUploadModel(
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_EMPRESA)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_LOCAL)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_INVENTARIO)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_IMOBILIZADO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_PASTA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_FILE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILE_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILE_NAME_ORIGINAL)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_USUARIO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESTAQUE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OBS)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCALIZACAO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRICAO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RAZAO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS_UPLOAD)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATA_UPLOAD)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_INSERT)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_UPDATE))
        )
    }
}