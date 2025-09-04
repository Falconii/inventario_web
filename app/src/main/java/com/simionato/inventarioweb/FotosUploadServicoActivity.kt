package com.simionato.inventarioweb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.adapters.FotoUploadAdapter
import com.simionato.inventarioweb.agendamentos.UploadWorker
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.databinding.ActivityFotosUploadServicoBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import com.simionato.inventarioweb.global.ParametroGlobal.*
import com.simionato.inventarioweb.global.ParametroGlobal.Util.Companion.getHoje
import com.simionato.inventarioweb.global.getFileFromUri
import com.simionato.inventarioweb.infra.DatabaseHelper

class FotosUploadServicoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFotosUploadServicoBinding.inflate(layoutInflater)
    }

    private val daoFoto by lazy {
        daoFotoUpload(DatabaseHelper(applicationContext));
    }

    private var fotos: List<FotoUploadModel> = listOf();


    private lateinit var  dialogDelete: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ParametroGlobal.Ambiente.itsOK()) {
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }
        try {
            val bundle = intent.extras
            if (bundle != null) {
                //id_imobilizado = bundle.getInt("id_imobilizado", 0)
                //descricao = bundle.getString("descricao")!!
            } else {
                //showToast("Parâmetro Foto Incorreto!!")
                //finish()
            }
        } catch (error: Exception) {
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }
        setContentView(binding.root)

        //binding.llProgress20.visibility = View.GONE

        iniciar()
    }

    fun iniciar() {
        binding.llProgress77.visibility = View.GONE
        inicializarTooBar()
        getFotos()
    }


    fun inicializarTooBar() {
        binding.ToolBar77.title = "Controle De Ativos"
        binding.ToolBar77.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar77.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar77.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar77.inflateMenu(R.menu.menu_upload_service)
        binding.ToolBar77.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_upload_srv_sair -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_upload_srv_upload -> {

                    // Executa imediatamente uma vez
                    //val oneTimeRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                    //    .setConstraints(
                    //        Constraints.Builder()
                    //            .setRequiredNetworkType(NetworkType.CONNECTED)
                    //            .build()
                    //    )
                    //    .build()

                    //WorkManager.getInstance(applicationContext).enqueue(oneTimeRequest)
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }

            }
        }
    }

    fun getFotos() {

        try {
            binding.llProgress77.visibility = View.VISIBLE
            this.fotos = daoFoto.getPhotosAll()
            binding.llProgress77.visibility = View.GONE
            val adapter = FotoUploadAdapter(fotos
            ) { foto, idAcao ->

                if (idAcao == CadastrosAcoes.Consulta) {
                    //chamaFotoWeb(foto)
                }
                if (idAcao == CadastrosAcoes.Exclusao) {
                    showDialogDelete(foto)
                }
                if (idAcao == CadastrosAcoes.Edicao) {
                    //chamaFotoEdicao(foto)
                }
                if (idAcao == CadastrosAcoes.UpLoadFoto) {
                lifecycleScope.launch {
                    enviarFotoFlow(foto).collect { estado ->
                        when (estado) {
                            is EstadoUpload.Carregando -> {
                                binding.llProgress77.visibility = View.VISIBLE
                            }
                            is EstadoUpload.Sucesso -> {
                                binding.llProgress77.visibility = View.GONE
                                showToast("Foto enviada com sucesso!", Toast.LENGTH_SHORT)
                                getFotos()
                            }
                            is EstadoUpload.Falha -> {
                                binding.llProgress77.visibility = View.GONE
                                showToast(estado.mensagem, Toast.LENGTH_LONG)
                            }
                        }
                    }
                }
            }
            }
            binding.rvLista77.adapter = adapter
            binding.rvLista77.layoutManager =
                LinearLayoutManager(binding.rvLista77.context)
            binding.rvLista77.addItemDecoration(
                DividerItemDecoration(
                    binding.rvLista77.context,
                    RecyclerView.VERTICAL
                )
            )

        } catch (e: Exception) {
            binding.llProgress77.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun showDialogDelete(foto: FotoUploadModel){
        val builder = AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Confirma A exclusão Da Foto ?")
            .setNegativeButton("Não"){_,_  -> dialogDelete.dismiss()
            }
            .setPositiveButton("Sim"){_,_ ->
                deleteFoto(foto)
            }
        dialogDelete = builder.create()

        dialogDelete.show()
    }

    private fun deleteFoto(foto:FotoUploadModel){
        try {

            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )
            binding.llProgress77.visibility = View.VISIBLE
            fotoService.deleteFotoDB(
                foto.idEmpresa,
                foto.idLocal,
                foto.idInventario,
                foto.idImobilizado,
                foto.idPasta,
                foto.idFile,
                foto.fileName
                ).enqueue( object  : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress77.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var retorno = response.body()

                            if (retorno !== null) {

                                daoFoto.deletePhoto(foto.id);

                                getFotos()

                            } else {

                                showToast("Falha No Retorno Da Requisição!")

                            }

                        }
                        else {
                            binding.llProgress77.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Não Encontrado Registro Temporário Da Foto. Foto Será Agada Do Celular!")
                                daoFoto.deletePhoto(foto.id)
                                getFotos()
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress77.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress77.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

            binding.llProgress77.visibility = View.VISIBLE
        }catch (e: Exception){
            binding.llProgress77.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun deleteFotoLocal(foto:FotoUploadModel){
        try {
            binding.llProgress77.visibility = View.VISIBLE
            daoFoto.deletePhoto(foto.id);
            getFotos()
            binding.llProgress77.visibility = View.GONE
        }catch (e: Exception){
            binding.llProgress77.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    /*
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

*/

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

    fun atualizarStatusErro(foto: FotoUploadModel) {
        foto.status_upload = "2"
        daoFoto.updatePhoto(foto)
    }

    fun logHttpError(response: Response<RetornoUpload>) {
        try {
            val gson = Gson()
            val errorMsg = gson.fromJson(response.errorBody()?.charStream(), HttpErrorMessage::class.java)
            Log.w("UploadWorker", "Erro HTTP: ${errorMsg}")
        } catch (e: Exception) {
            Log.e("UploadWorker", "Erro ao decodificar resposta HTTP.")
        }
    }

    fun enviarFotoFlow(foto: FotoUploadModel): Flow<EstadoUpload> = flow {
        emit(EstadoUpload.Carregando)

        try {
            val fotoUri = Uri.parse(foto.idFile)

            val file = getFileFromUri(applicationContext, fotoUri) ?: throw Exception("Arquivo não encontrado.")

            val filePart = MultipartBody.Part.createFormData(
                "file", file.name,
                RequestBody.create(MultipartBody.FORM, file)
            )

            val parts = mapOf(
                "id_empresa"     to foto.idEmpresa.toString(),
                "id_local"       to foto.idLocal.toString(),
                "id_inventario"  to foto.idInventario.toString(),
                "id_imobilizado" to foto.idImobilizado.toString(),
                "id_pasta"       to foto.idPasta,
                "id_file"        to foto.idFile,
                "file_name"       to foto.fileNameOriginal,
                "id_usuario"     to Dados.usuario.id.toString(),
                "data"           to getHoje(),
                "destaque"       to foto.destaque,
                "obs"            to foto.obs,
                "localizacao"    to "N"
            ).mapValues { RequestBody.create(MultipartBody.FORM, it.value) }

            val service = InfraHelper.apiInventario.create(FotoService::class.java)
            val response = service.uploadfotov5_2_disp(
                parts["id_empresa"]!!, parts["id_local"]!!, parts["id_inventario"]!!,
                parts["id_imobilizado"]!!, parts["id_pasta"]!!, parts["id_file"]!!,
                parts["file_name"]!!, parts["id_usuario"]!!, parts["data"]!!,
                parts["destaque"]!!, parts["obs"]!!, parts["localizacao"]!!, filePart
            )

            if (response.isSuccessful && response.body() != null) {
                daoFoto.deletePhoto(foto.id)
                apagarFoto(applicationContext, fotoUri)
                emit(EstadoUpload.Sucesso)
            } else {
                logHttpError(response)
                atualizarStatusErro(foto)
                emit(EstadoUpload.Falha("Erro ao enviar foto: ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(EstadoUpload.Falha("Exceção: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)


}