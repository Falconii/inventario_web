package com.simionato.inventarioweb.global
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkInfo
import java.util.UUID

object UploadStatusHelper {

    private const val PREFS_NAME = "uploadPrefs"
    private const val KEY_LAST_PHOTO = "ultimaFoto"
    private const val KEY_LAST_WORK_ID = "ultimoWorkerId"
    private const val NOTIFICATION_CHANNEL_ID = "canal_upload"
    private const val NOTIFICATION_ID = 1001

    fun salvarUltimaFoto(context: Context, nome: String) {
        prefs(context).edit().putString(KEY_LAST_PHOTO, nome).apply()
    }

    fun recuperarUltimaFoto(context: Context): String? {
        return prefs(context).getString(KEY_LAST_PHOTO, null)
    }

    fun salvarIdWorker(context: Context, id: UUID) {
        prefs(context).edit().putString(KEY_LAST_WORK_ID, id.toString()).apply()
    }

    fun recuperarIdWorker(context: Context): UUID? {
        val idStr = prefs(context).getString(KEY_LAST_WORK_ID, null)
        return idStr?.let { UUID.fromString(it) }
    }

    fun mostrarNotificacao(context: Context, titulo: String, mensagem: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Status de Upload",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(canal)
        }

        val notificacao = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload) // ou seu ícone personalizado
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notificacao)
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun interpretarStatus(workInfo: WorkInfo?): String {
        return when (workInfo?.state) {
            WorkInfo.State.SUCCEEDED -> "✅ Upload concluído"
            WorkInfo.State.FAILED -> "❌ Falha no upload"
            WorkInfo.State.RUNNING -> "🚀 Enviando fotos..."
            WorkInfo.State.ENQUEUED -> "⏳ Aguardando execução"
            else -> "ℹ️ Status desconhecido"
        }
    }

    fun limparDadosUpload(context: Context) {
        prefs(context).edit()
            .remove(KEY_LAST_WORK_ID)
            .remove(KEY_LAST_PHOTO)
            .apply()
    }
}