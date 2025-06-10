package com.simionato.inventarioweb.agendamentos
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simionato.inventarioweb.dao.daofotodownload
import com.simionato.inventarioweb.infra.DatabaseHelper

class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams){
    override fun doWork(): Result {
        val photoRepository = daofotodownload(DatabaseHelper(applicationContext))
        val pendingPhotos = photoRepository.getPendingPhotos()
        Log.i("SRV","pendingPhotos -> ${pendingPhotos.count()}")
        val dateTimeString = getCurrentDateTime()
        Log.i("SRV","Data e Hora: $dateTimeString")
        pendingPhotos.forEach { photo ->
            photoRepository.updatePhotoStatus(photo.id, 2) // Atualiza no banco
        }

        return Result.success()

    }


    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }

}