package com.simionato.inventarioweb.repositories

import android.content.Context
import android.net.Uri
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.services.FotoService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.simionato.inventarioweb.global.ParametroGlobal.*

class FotoRepository(
    private val service: FotoService,
    private val dao: daoFotoUpload
) {

    suspend fun uploadFoto(context: Context,foto: FotoUploadModel): EstadoUpload {
        val fotoUri = Uri.parse(foto.idFile)
        val file = getFileFromUri(context,fotoUri) ?: return EstadoUpload.Falha("Erro no upload: Foto Não Encontrada")

        val filePart = MultipartBody.Part.createFormData("file", file.name,
            RequestBody.create(MultipartBody.FORM, file))

        val params = mapOf(
            "id_empresa"     to foto.idEmpresa.toString(),
            "id_local"       to foto.idLocal.toString(),
            "id_inventario"  to foto.idInventario.toString(),
            "id_imobilizado" to foto.idImobilizado.toString(),
            "id_pasta"       to foto.idPasta,
            "id_file"        to foto.idFile,
            "old_name"       to foto.fileNameOriginal,
            "id_usuario"     to ParametroGlobal.Dados.usuario.id.toString(),
            "data"           to ParametroGlobal.Util.getHoje(),
            "destaque"       to foto.destaque,
            "obs"            to foto.obs,
            "localizacao"    to "N"
        ).mapValues { RequestBody.create(MultipartBody.FORM, it.value) }

        val response = service.uploadfotov5_2_disp(
            params["id_empresa"]!!, params["id_local"]!!, params["id_inventario"]!!,
            params["id_imobilizado"]!!, params["id_pasta"]!!, params["id_file"]!!,
            params["old_name"]!!, params["id_usuario"]!!, params["data"]!!,
            params["destaque"]!!, params["obs"]!!, params["localizacao"]!!, filePart
        )

        return if (response.isSuccessful && response.body() != null) {
            dao.deletePhoto(foto.id)
            EstadoUpload.Sucesso
        } else {
            foto.status_upload = "2"
            dao.updatePhoto(foto)
            EstadoUpload.Falha("Erro no upload: ${response.code()}")

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


}
