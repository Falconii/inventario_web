package com.simionato.inventarioweb.agendamentos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UploadWorker(
    context: Context,
    workerParams: WorkerParameters

) : CoroutineWorker(context, workerParams) {

    private val daoFoto by lazy {
        daoFotoUpload(DatabaseHelper(applicationContext));
    }

    override suspend fun doWork(): Result {
        return try {
            val pendingPhotos = daoFoto.getPhotosUpLoad()
            Log.i(
                "UploadWorker",
                "⏱️ Fotos pendentes: ${pendingPhotos.size} às ${getCurrentDateTime()}"
            )

            if (pendingPhotos.isEmpty()) return Result.success()

            for (foto in pendingPhotos) {
                try {
                    enviarFotoSuspend(foto)
                } catch (e: Exception) {
                    Log.e("UploadWorker", "⚠️ Erro ao enviar foto ${foto.idFile}: ${e.message}")
                    atualizarStatusErro(foto)
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("UploadWorker", "💥 Erro geral no Worker: ${e.message}")
            Result.failure()
        }
    }

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }

    private suspend fun enviarFotoSuspend(foto: FotoUploadModel) {
        val fotoUri = Uri.parse(foto.idFile)
        val file = getFileFromUri(applicationContext, fotoUri)
            ?: throw Exception("Arquivo não encontrado.")

        val filePart = MultipartBody.Part.createFormData(
            "file", file.name,
            RequestBody.create(MultipartBody.FORM, file)
        )

        val parts = mapOf(
            "id_empresa" to foto.idEmpresa.toString(),
            "id_local" to foto.idLocal.toString(),
            "id_inventario" to foto.idInventario.toString(),
            "id_imobilizado" to foto.idImobilizado.toString(),
            "id_pasta" to foto.idPasta,
            "id_file" to foto.idFile,
            "old_name" to foto.fileNameOriginal,
            "id_usuario" to ParametroGlobal.Dados.usuario.id.toString(),
            "data" to getHoje(),
            "destaque" to foto.destaque,
            "obs" to foto.obs,
            "localizacao" to "N"
        ).mapValues { RequestBody.create(MultipartBody.FORM, it.value) }

        val service = InfraHelper.apiInventario.create(FotoService::class.java)

        val response = service.uploadfotov5_2_disp(
            parts["id_empresa"]!!,
            parts["id_local"]!!,
            parts["id_inventario"]!!,
            parts["id_imobilizado"]!!,
            parts["id_pasta"]!!,
            parts["id_file"]!!,
            parts["old_name"]!!,
            parts["id_usuario"]!!,
            parts["data"]!!,
            parts["destaque"]!!,
            parts["obs"]!!,
            parts["localizacao"]!!,
            filePart
        )

        if (response.isSuccessful && response.body() != null) {
            daoFoto.deletePhoto(foto.id)
            apagarFoto(applicationContext, fotoUri)
        } else {
            logHttpError(response)
            atualizarStatusErro(foto)
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val filePathColumn = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath?.let { File(it) }
    }

    fun getHoje(): String {

        try {

            val date = Date()

            val format = android.icu.text.SimpleDateFormat("dd/MM/yyyy")

            val data = format.format(date)

            return data

        } catch (e: Exception) {
            return ""
        }

    }

    fun apagarFoto(context: Context, fotoUri: Uri): Boolean {
        return try {
            val deletados = context.contentResolver.delete(fotoUri, null, null)
            deletados > 0
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun atualizarStatusErro(foto: FotoUploadModel) {
        foto.status_upload = "2"
        daoFoto.updatePhoto(foto)
    }

    private fun logHttpError(response: Response<RetornoUpload>) {
        try {
            val gson = Gson()
            val errorMsg =
                gson.fromJson(response.errorBody()?.charStream(), HttpErrorMessage::class.java)
            Log.w("UploadWorker", "Erro HTTP: ${errorMsg}")
        } catch (e: Exception) {
            Log.e("UploadWorker", "Erro ao decodificar resposta HTTP.")
        }
    }


}