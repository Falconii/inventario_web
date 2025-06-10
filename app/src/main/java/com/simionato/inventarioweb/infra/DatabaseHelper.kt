package com.simionato.inventarioweb.infra

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    "${context.filesDir}/simionato_ativo.db", // Caminho personalizado dentro de filesDir
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "photos"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PATH = "path"
        const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PATH TEXT NOT NULL,
                $COLUMN_STATUS INTEGER DEFAULT 0
            )
        """
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Evita destruir o banco ao atualizar a versão
        if (oldVersion < newVersion) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN nova_coluna TEXT")
        }
    }
}