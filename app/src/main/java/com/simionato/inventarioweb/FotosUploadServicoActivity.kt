package com.simionato.inventarioweb
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
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
import androidx.work.WorkInfo
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
import com.simionato.inventarioweb.global.UploadStatusHelper
import com.simionato.inventarioweb.global.apagarFotoGaleria
import com.simionato.inventarioweb.global.getFileFromUri
import com.simionato.inventarioweb.global.showToast
import com.simionato.inventarioweb.infra.DatabaseHelper
import java.util.UUID
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.simionato.inventarioweb.databinding.ActivityFotosBinding
import com.simionato.inventarioweb.global.SafManager


class FotosUploadServicoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFotosUploadServicoBinding.inflate(layoutInflater)
    }


    private val daoFoto by lazy {
        daoFotoUpload(dbHelper)
    }

    private var fotos: List<FotoUploadModel> = listOf();

    private lateinit var  dialogDelete: AlertDialog

    private val safLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            SafManager.tratarResultado(this, result.resultCode, result.data)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ParametroGlobal.Ambiente.itsOK()) {
            showToast(applicationContext,"Ambiente Incorreto!!")
            finish()
            return
        }
        setContentView(binding.root)

        if (!SafManager.hasPermissao(this)) {
            SafManager.solicitarAcesso(safLauncher)
        } else {
            val pasta = SafManager.getPastaSimionato(this)

            val dbHelper = DatabaseHelper.getInstance(this)
            if (dbHelper == null) {
                Toast.makeText(this, "Banco de dados não disponível", Toast.LENGTH_LONG).show()
                return
            }

            // Instancia o DAO



        }

        iniciar()
    }

    fun iniciar() {


        getFotos()


        val id = UploadStatusHelper.recuperarIdWorker(this)
        if (id != null) {
            workerStarter(id)
        }

        val nome = UploadStatusHelper.recuperarUltimaFoto(this)
        if (nome != null) {
            showToast(applicationContext,"Última enviada: $nome");
        }
        binding.llProgress77.visibility = View.GONE
        inicializarTooBar()
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

                    if (this.fotos.size == 0){
                       showToast(applicationContext,"Nehuma Foto Para Enviar!")
                    } else {
                        workerStarter(null)
                    }

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
            ativarAdapter()
            binding.llProgress77.visibility = View.GONE
        } catch (e: Exception) {
            binding.llProgress77.visibility = View.GONE
            showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }

    fun ativarAdapter(){

        val adapter = FotoUploadAdapter(this.fotos
        ) { foto, idAcao ->

            if (idAcao == CadastrosAcoes.Consulta) {
                chamaFotosLocaisAtivos(foto)
            }
            if (idAcao == CadastrosAcoes.Exclusao) {
                showDialogDelete(foto)
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
                                showToast(applicationContext,"Foto enviada com sucesso!", Toast.LENGTH_SHORT)
                                getFotos()
                            }
                            is EstadoUpload.SucessoParcial -> {
                                binding.llProgress77.visibility = View.GONE
                                showToast(applicationContext,estado.mensagem, Toast.LENGTH_SHORT)
                                getFotos()
                            }
                            is EstadoUpload.Falha -> {
                                binding.llProgress77.visibility = View.GONE
                                showToast(applicationContext,estado.mensagem, Toast.LENGTH_LONG)
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

                                daoFoto.deleteFoto(foto.id);

                                getFotos()

                            } else {

                                showToast(applicationContext,"Falha No Retorno Da Requisição!")

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
                                showToast(applicationContext,"Não Encontrado Registro Temporário Da Foto. Foto Será Agada Do Celular!")
                                daoFoto.deleteFoto(foto.id)
                                getFotos()
                            } else {
                                showToast(applicationContext,"${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress77.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress77.visibility = View.GONE
                    showToast(applicationContext,t.message.toString())
                }
            })

            binding.llProgress77.visibility = View.VISIBLE
        }catch (e: Exception){
            binding.llProgress77.visibility = View.GONE
            showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun deleteFotoLocal(foto:FotoUploadModel){
        try {
            binding.llProgress77.visibility = View.VISIBLE
            daoFoto.deleteFoto(foto.id);
            getFotos()
            binding.llProgress77.visibility = View.GONE
        }catch (e: Exception){
            binding.llProgress77.visibility = View.GONE
            showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }


    fun atualizarStatusErro(foto: FotoUploadModel) {
        foto.status_upload = "2"
        daoFoto.updateFoto(foto)
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
                var mensagem_error = "";
                try{
                    daoFoto.deleteFoto(foto.id)
                } catch (e:Exception){
                    mensagem_error = "Foto Enviada Com Sucesso. Mas Continua No Banco. Isso Não Atrapalha Operacionalmente\n"
                }
                try {
                    apagarFotoGaleria(applicationContext, fotoUri)
                } catch (e: Exception) {
                    mensagem_error += "Foto Enviada Com Sucesso. Mas Continua No Galeria. Não Atrapalha Operacionalmente"
                }
                if (mensagem_error == ""){
                    emit(EstadoUpload.Sucesso);
                } else {
                    emit(EstadoUpload.SucessoParcial(mensagem_error))
                }
            } else {
                logHttpError(response)
                atualizarStatusErro(foto)
                emit(EstadoUpload.Falha("Erro ao enviar foto: ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(EstadoUpload.Falha("Exceção: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun workerStarter(id: UUID?) {
        if (id == null) {
            // 🔹 Cria e enfileira um novo Worker
            val oneTimeRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("uploadFotos")
                .build()

            WorkManager.getInstance(applicationContext).enqueue(oneTimeRequest)

            UploadStatusHelper.salvarIdWorker(applicationContext, oneTimeRequest.id)

            observarWorker(oneTimeRequest.id)
        } else {
            observarWorker(id)
        }
    }

    private fun observarWorker(id: UUID) {
        WorkManager.getInstance(applicationContext)
            .getWorkInfoByIdLiveData(id)
            .observe(this) { workInfo ->
                val progress = workInfo?.progress?.getString("progresso_upload")
                if (!progress.isNullOrEmpty()) {
                    binding.textViewProgress77.text = "Enviando: $progress"
                }

                //val mensagem = UploadStatusHelper.interpretarStatus(workInfo)
                //showToast(applicationContext, mensagem)

                when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING -> {
                        binding.rvLista77.visibility = View.GONE
                        binding.llProgress77.visibility = View.VISIBLE
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        binding.rvLista77.visibility = View.VISIBLE
                        binding.llProgress77.visibility = View.GONE
                        Toast.makeText(this, "Upload finalizado!", Toast.LENGTH_SHORT).show()
                        UploadStatusHelper.limparDadosUpload(applicationContext)
                        getFotos()
                    }

                    WorkInfo.State.FAILED -> {
                        binding.rvLista77.visibility = View.VISIBLE
                        binding.llProgress77.visibility = View.GONE
                        UploadStatusHelper.limparDadosUpload(applicationContext)
                        Toast.makeText(this, "Erro ao enviar fotos", Toast.LENGTH_LONG).show()
                    }

                    WorkInfo.State.CANCELLED -> {
                        binding.rvLista77.visibility = View.GONE
                        binding.llProgress77.visibility = View.GONE
                        UploadStatusHelper.limparDadosUpload(applicationContext)
                        showToast(applicationContext, "⚠️ Upload cancelado")
                    }

                    else -> {}
                }
            }
    }

    fun chamaFotosLocaisAtivos(foto:FotoUploadModel){
        val intent = Intent(this,FotosLocaisActivity::class.java)
        intent.putExtra("imobilizado",foto.idImobilizado)
        startActivity(intent)
    }



    private fun criarPastaSimionato() {
        val pasta = File(Environment.getExternalStorageDirectory(), "simionato")
        if (!pasta.exists()) {
            pasta.mkdirs()
        }
    }


}
