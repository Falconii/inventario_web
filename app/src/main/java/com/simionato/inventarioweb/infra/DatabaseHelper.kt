package com.simionato.inventarioweb.infra

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.simionato.inventarioweb.global.SafManager
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper private constructor(context: Context, dbFile: File) :
    SQLiteOpenHelper(context, dbFile.absolutePath, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 11
        private const val DATABASE_NAME = "simionato.db"

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper? {
            if (instance == null) {
                synchronized(this) {
                    val dbFile = getDatabaseFileFromSaf(context)
                    if (dbFile != null) {
                        instance = DatabaseHelper(context, dbFile)
                    } else {
                        Log.e("DatabaseHelper", "Não foi possível localizar a pasta simionato via SAF.")
                    }
                }
            }
            return instance
        }

        private fun getDatabaseFileFromSaf(context: Context): File? {
            val simionatoFolder = SafManager.getPastaSimionato(context) ?: return null
            val dbDocument = simionatoFolder.findFile(DATABASE_NAME)
                ?: simionatoFolder.createFile("application/octet-stream", DATABASE_NAME)

            val uri = dbDocument?.uri ?: return null

            // Copiar o conteúdo para um arquivo temporário local
            val tempFile = File(context.cacheDir, DATABASE_NAME)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Erro ao copiar banco do SAF: ${e.message}")
                return null
            }

            return tempFile
        }

        // Constantes de tabelas e colunas
        const val TABLE_PHOTOS = "photos"
        const val TABLE_LANCAMENTOS = "lancamentos"
        const val COLUMN_ID = "id"
        const val COLUMN_ID_EMPRESA = "id_empresa"
        const val COLUMN_ID_LOCAL = "id_local"
        const val COLUMN_ID_INVENTARIO = "id_inventario"
        const val COLUMN_ID_IMOBILIZADO = "id_imobilizado"
        const val COLUMN_ID_PASTA = "id_pasta"
        const val COLUMN_ID_FILE = "id_file"
        const val COLUMN_FILE_NAME = "file_name"
        const val COLUMN_FILE_NAME_ORIGINAL = "file_name_original"
        const val COLUMN_ID_USUARIO = "id_usuario"
        const val COLUMN_DATA = "data"
        const val COLUMN_DESTAQUE = "destaque"
        const val COLUMN_OBS = "obs"
        const val COLUMN_LOCALIZACAO = "localizacao"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_RAZAO = "razao"
        const val COLUMN_STATUS_UPLOAD = "status_upload"
        const val COLUMN_DATA_UPLOAD = "data_upload"
        const val COLUMN_USER_INSERT = "user_insert"
        const val COLUMN_USER_UPDATE = "user_update"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Criação das tabelas
        val createPhotosTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_PHOTOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ID_EMPRESA TEXT,
                $COLUMN_ID_LOCAL TEXT,
                $COLUMN_ID_INVENTARIO TEXT,
                $COLUMN_ID_IMOBILIZADO TEXT,
                $COLUMN_ID_PASTA TEXT,
                $COLUMN_ID_FILE TEXT,
                $COLUMN_FILE_NAME TEXT,
                $COLUMN_FILE_NAME_ORIGINAL TEXT,
                $COLUMN_ID_USUARIO TEXT,
                $COLUMN_DATA TEXT,
                $COLUMN_DESTAQUE INTEGER,
                $COLUMN_OBS TEXT,
                $COLUMN_LOCALIZACAO TEXT,
                $COLUMN_DESCRICAO TEXT,
                $COLUMN_RAZAO TEXT,
                $COLUMN_STATUS_UPLOAD INTEGER,
                $COLUMN_DATA_UPLOAD TEXT,
                $COLUMN_USER_INSERT TEXT,
                $COLUMN_USER_UPDATE TEXT
            )
        """.trimIndent()

        db.execSQL(createPhotosTable)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PHOTOS")
            onCreate(db) // Recria as tabelas para aplicar as mudanças
        }
    }
}