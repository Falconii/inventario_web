package com.simionato.inventarioweb.agendamentos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.UploadStatusHelper
import com.simionato.inventarioweb.global.apagarFotoGaleria
import com.simionato.inventarioweb.global.getCurrentDateTime
import com.simionato.inventarioweb.global.getFileFromUri
import com.simionato.inventarioweb.global.getHoje
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

            if (pendingPhotos.isEmpty()) {
                UploadStatusHelper.mostrarNotificacao(
                    applicationContext,
                    "Upload finalizado",
                    "Nenhuma foto pendente para envio."
                )
                return Result.success()
            }

            var houveErro = false

            var index = 1

            for (foto in pendingPhotos) {
                try {
                    setProgress(workDataOf("progresso_upload" to "Enviando Foto ${index}/${pendingPhotos.size-1}"))
                    enviarFotoSuspend(foto)
                } catch (e: Exception) {
                    houveErro = true
                    atualizarStatusErro(foto)
                }

            }

            if (houveErro) {
                UploadStatusHelper.mostrarNotificacao(
                    applicationContext,
                    "Erro no upload",
                    "Algumas fotos não foram enviadas."
                )
            } else {
                UploadStatusHelper.mostrarNotificacao(
                    applicationContext,
                    "Upload concluído",
                    "Todas as fotos foram enviadas com sucesso."
                )
            }

            Result.success()


        } catch (e: Exception) {
            UploadStatusHelper.mostrarNotificacao(
                applicationContext,
                "Falha crítica",
                "Ocorreu um erro inesperado no envio."
            )
            Result.failure()
        }
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
            apagarFotoGaleria(applicationContext, fotoUri)
        } else {
            logHttpError(response)
            atualizarStatusErro(foto)
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