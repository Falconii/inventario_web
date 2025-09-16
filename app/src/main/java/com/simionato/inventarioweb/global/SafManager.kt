package com.simionato.inventarioweb.global
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import com.simionato.inventarioweb.infra.DatabaseHelper

object SafManager  {


    private const val PREFS_NAME = "saf_prefs"
    private const val KEY_URI = "simionato_uri"

    fun solicitarAcesso(activityLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
        activityLauncher.launch(intent)
    }

    fun tratarResultado(context: Context, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data?.data == null) return

        val uri = data.data!!
        val docId = DocumentsContract.getTreeDocumentId(uri)

        if (docId.contains("simionato", ignoreCase = true)) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            salvarUri(context, uri)
            val dbHelper = DatabaseHelper.getInstance(context)
            if (dbHelper == null) {
                showToast(context, "Banco de dados não disponível")
                return
            }
        } else {
            Log.w("SafManager", "Pasta selecionada não é 'simionato': $docId")
        }
    }

    fun hasPermissao(context: Context): Boolean {
        val uri = getUri(context) ?: return false
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission && it.isWritePermission
        }
    }

    fun getUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_URI, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun getPastaSimionato(context: Context): DocumentFile? {
        val uri = getUri(context) ?: return null
        return DocumentFile.fromTreeUri(context, uri)
    }

    private fun salvarUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_URI, uri.toString()).apply()
    }

}