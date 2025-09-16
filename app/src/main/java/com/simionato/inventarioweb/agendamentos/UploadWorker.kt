package com.simionato.inventarioweb.agendamentos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import com.simionato.inventarioweb.global.showToast
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


    private lateinit var daoFoto: daoFotoUpload

    override suspend fun doWork(): Result {
        val dbHelper = DatabaseHelper.getInstance(applicationContext)
        if (dbHelper == null) {
            showToast(applicationContext,"Banco de dados não disponível")
            return Result.failure()
        }

        // Instancia o DAO
        daoFoto = daoFotoUpload(dbHelper)
        return try {
            val pendingPhotos = daoFoto.getFotosUpLoad()
            Log.i(
                "UploadWorker",
                "⏱️ Fotos pendentes: ${pendingPhotos.size} às ${getCurrentDateTime()}"
            )

            if (pendingPhotos.isEmpty()) {
                UploadStatusHelper.limparDadosUpload(applicationContext)
                return Result.success()
            }

            var houveErro = false

            var index = 1

            for (foto in pendingPhotos) {
                try {
                    setProgress(workDataOf("progresso_upload" to "Enviando Foto ${index}/${pendingPhotos.size}"))
                    enviarFotoSuspend(foto)
                    index++
                } catch (e: ParametroGlobal.UpLoadExcessao) {
                    if (e.codigoErro != ParametroGlobal.CodigoErro.REG_TEMP){
                        houveErro = true
                        atualizarStatusErro(foto)
                    }
                }
            }

            if (houveErro) {
                UploadStatusHelper.mostrarNotificacao(
                    applicationContext,
                    "Erro no upload",
                    "Algumas fotos não foram enviadas."
                )
                UploadStatusHelper.limparDadosUpload(applicationContext)
            } else {
                UploadStatusHelper.mostrarNotificacao(
                    applicationContext,
                    "Upload concluído",
                    "Todas as fotos foram enviadas com sucesso."
                )
            }
            UploadStatusHelper.limparDadosUpload(applicationContext)
            setProgress(workDataOf("progresso_upload" to "Enviando Foto ${pendingPhotos.size-1}/${pendingPhotos.size-1}"))
            Result.success()
        } catch (e: Exception) {
            UploadStatusHelper.mostrarNotificacao(
                applicationContext,
                "Falha crítica",
                "Ocorreu um erro inesperado no envio."
            )
            setProgress(workDataOf("progresso_upload" to "Problemas Com As Fotos"))
            UploadStatusHelper.limparDadosUpload(applicationContext)
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
            daoFoto.deleteFoto(foto.id)
            apagarFotoGaleria(applicationContext, fotoUri)
        } else {
            var erro = logHttpError(response)
            if (erro.isNotEmpty()){
                if ("Não Existe Registro Temporário De Foto Do Dispositivo" in erro) {
                    daoFoto.deleteFoto(foto.id)
                    apagarFotoGaleria(applicationContext, fotoUri)
                    throw ParametroGlobal.UpLoadExcessao(ParametroGlobal.CodigoErro.REG_TEMP)
                }
                else {
                    throw ParametroGlobal.UpLoadExcessao(ParametroGlobal.CodigoErro.ERRO_DESCONHECIDO)
                }

            }
        }
    }

    private fun atualizarStatusErro(foto: FotoUploadModel) {
        foto.status_upload = "2"
        daoFoto.updateFoto(foto)
    }

    private fun logHttpError(response: Response<RetornoUpload>): String {
        var retorno = ""
        try {
            val gson = Gson()
            val errorMsg =
                gson.fromJson(response.errorBody()?.charStream(), HttpErrorMessage::class.java)
            retorno = errorMsg.getMessage().toString()
        } catch (e: Exception) {
            retorno =  e.message.toString()
        }
        return retorno
    }


}