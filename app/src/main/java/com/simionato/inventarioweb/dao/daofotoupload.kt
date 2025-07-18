package com.simionato.inventarioweb.dao

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.models.FotoUploadModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class daoFotoUpload(private val dbHelper: DatabaseHelper) {

    fun insertPhoto(photo: FotoUploadModel): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
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
        return db.insert(DatabaseHelper.TABLE_PHOTOS, null, values) != -1L
    }

    fun getPhotos(): List<FotoUploadModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PHOTOS,
            arrayOf(
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_ID_EMPRESA,
                DatabaseHelper.COLUMN_ID_LOCAL,
                DatabaseHelper.COLUMN_ID_INVENTARIO,
                DatabaseHelper.COLUMN_ID_IMOBILIZADO,
                DatabaseHelper.COLUMN_ID_PASTA,
                DatabaseHelper.COLUMN_ID_FILE,
                DatabaseHelper.COLUMN_FILE_NAME,
                DatabaseHelper.COLUMN_FILE_NAME_ORIGINAL,
                DatabaseHelper.COLUMN_ID_USUARIO,
                DatabaseHelper.COLUMN_DATA,
                DatabaseHelper.COLUMN_DESTAQUE,
                DatabaseHelper.COLUMN_OBS,
                DatabaseHelper.COLUMN_LOCALIZACAO,
                DatabaseHelper.COLUMN_DESCRICAO,
                DatabaseHelper.COLUMN_RAZAO,
                DatabaseHelper.COLUMN_STATUS_UPLOAD,
                DatabaseHelper.COLUMN_DATA_UPLOAD,
                DatabaseHelper.COLUMN_USER_INSERT,
                DatabaseHelper.COLUMN_USER_UPDATE
            ),
            null, null, null, null, null
        )

        val photos = mutableListOf<FotoUploadModel>()
        while (cursor.moveToNext()) {
            photos.add(
                FotoUploadModel(
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
            )
        }
        cursor.close()
        return photos
    }
    fun updatePhoto(photo: FotoUploadModel): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
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
            put(DatabaseHelper.COLUMN_USER_UPDATE, photo.userUpdate) // Atualizado por último
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_PHOTOS, values,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(photo.id.toString())
        )
        return rowsAffected > 0
    }

    fun deletePhoto(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            DatabaseHelper.TABLE_PHOTOS,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(id.toString())
        )
        return rowsDeleted > 0
    }


}