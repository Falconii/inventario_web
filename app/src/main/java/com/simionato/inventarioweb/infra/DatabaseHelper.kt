package com.simionato.inventarioweb.infra

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    "${context.filesDir}/simionato.db",
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_VERSION = 3 // Atualizamos a versão do banco
        const val TABLE_PHOTOS = "photos"
        const val TABLE_LANCAMENTOS = "lancamentos"

        // Colunas da tabela "photos"
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
        const val COLUMN_USER_INSERT = "user_insert"
        const val COLUMN_USER_UPDATE = "user_update"

        // Colunas da tabela "lançamentos"
        const val COLUMN_ID_EMPRESA_LANC = "id_empresa"
        const val COLUMN_ID_LOCAL_LANC = "id_local"
        const val COLUMN_ID_INVENTARIO_LANC = "id_inventario"
        const val COLUMN_ID_FOTO_LANC = "id_foto"
        const val COLUMN_ID_EXEC = "id_exec"
        const val COLUMN_DESCRICAO = "descricao"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPhotosTable = """
            CREATE TABLE $TABLE_PHOTOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ID_EMPRESA INTEGER NOT NULL,
                $COLUMN_ID_LOCAL INTEGER NOT NULL,
                $COLUMN_ID_INVENTARIO INTEGER NOT NULL,
                $COLUMN_ID_IMOBILIZADO INTEGER NOT NULL,
                $COLUMN_ID_PASTA VARCHAR(255) NOT NULL,
                $COLUMN_ID_FILE VARCHAR(255) NOT NULL,
                $COLUMN_FILE_NAME VARCHAR(255) NOT NULL,
                $COLUMN_FILE_NAME_ORIGINAL VARCHAR(255) NOT NULL,
                $COLUMN_ID_USUARIO INTEGER NOT NULL,
                $COLUMN_DATA DATE NOT NULL,
                $COLUMN_DESTAQUE CHAR(1) NOT NULL,
                $COLUMN_OBS VARCHAR(255),
                $COLUMN_USER_INSERT INTEGER NOT NULL,
                $COLUMN_USER_UPDATE INTEGER
            )
        """

        val createLancamentosTable = """
            CREATE TABLE $TABLE_LANCAMENTOS (
                $COLUMN_ID_EMPRESA_LANC INTEGER NOT NULL,
                $COLUMN_ID_LOCAL_LANC INTEGER NOT NULL,
                $COLUMN_ID_INVENTARIO_LANC INTEGER NOT NULL,
                $COLUMN_ID_FOTO_LANC INTEGER NOT NULL,
                $COLUMN_ID_EXEC INTEGER NOT NULL,
                $COLUMN_DESCRICAO TEXT NOT NULL,
                PRIMARY KEY ($COLUMN_ID_EMPRESA_LANC, $COLUMN_ID_LOCAL_LANC, $COLUMN_ID_INVENTARIO_LANC, $COLUMN_ID_FOTO_LANC)
            )
        """

        db.execSQL(createPhotosTable)
        db.execSQL(createLancamentosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PHOTOS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_LANCAMENTOS")
            onCreate(db) // Recria as tabelas para aplicar as mudanças
        }
    }
}