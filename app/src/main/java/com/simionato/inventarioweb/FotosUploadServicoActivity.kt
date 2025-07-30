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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.adapters.FotoUploadAdapter
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.databinding.ActivityFotosUploadServicoBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Date

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


    private fun inicializarTooBar() {
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

                R.id.menu_upload_srv_atualizar -> {
                    finish()
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
            this.fotos = daoFoto.getPhotos()
            binding.llProgress77.visibility = View.GONE
            val adapter = FotoUploadAdapter(fotos, { foto, idAcao ->

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
                    upLoadFoto(foto)
                }
            }
            )
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
                                showToast("Tabela De Fotos Vazia")
                                //var fotos: List<FotoModel> = listOf()
                                //montaLista(fotos)
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

    private fun upLoadFoto(foto:FotoUploadModel) {
        try {

            val fotoUri = Uri.parse(foto.idFile)

            var file = this.getFileFromUri(applicationContext, fotoUri)

            if (file == null) {
                return
            }

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa =
                RequestBody.create(MultipartBody.FORM, ParametroGlobal.Dados.empresa.id.toString())

            val id_local =
                RequestBody.create(MultipartBody.FORM, ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM, Inventario.codigo.toString())

            val id_imobilizado =
                RequestBody.create(MultipartBody.FORM, foto.idImobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM, "")

            val id_file = RequestBody.create(MultipartBody.FORM, "")

            val old_name = RequestBody.create(MultipartBody.FORM, "")

            val id_usuario =
                RequestBody.create(MultipartBody.FORM, ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM, getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM, foto.destaque)

            val obs = RequestBody.create(MultipartBody.FORM, foto.obs)

            val localizacao = RequestBody.create(MultipartBody.FORM, "N")

            binding.progressBar77.visibility = View.VISIBLE

            try {
                val fotoService = InfraHelper.apiInventario.create(FotoService::class.java)

                fotoService.postUploadFoto(
                    id_empresa,
                    id_local,
                    id_inventario,
                    id_imobilizado,
                    id_pasta,
                    id_file,
                    old_name,
                    id_usuario,
                    data,
                    destaque,
                    obs,
                    localizacao,
                    body
                )
                    .enqueue(object : Callback<RetornoUpload> {
                        override fun onResponse(
                            call: Call<RetornoUpload>,
                            response: Response<RetornoUpload>
                        ) {
                            binding.llProgress77.visibility = View.GONE

                            if (response != null) {
                                if (response.isSuccessful) {

                                    var mensagem = response.body()

                                    if (mensagem !== null) {

                                        showToast("${mensagem.message}")

                                        daoFoto.deletePhoto(foto.id);

                                        getFotos()

                                        showToast("Foto Excluida Do Celular!")

                                    } else {
                                        showToast("Falha No Retorno Da Requisição!")

                                    }

                                } else {
                                    binding.llProgress77.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast(
                                        "${message.getMessage().toString()}",
                                        Toast.LENGTH_SHORT
                                    )

                                }
                            } else {
                                binding.llProgress77.visibility = View.GONE
                                showToast("Não Foi Possivel Inserir A Foto Na Nuvem")
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress77.visibility = View.GONE
                            showToast("${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception) {
                binding.llProgress77.visibility = View.GONE
                showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            }

        } catch (error: Exception) {
            Log.e("ww", "${error.message}")
            showToast("Falha Ao Preparar A Foto Para Transmissão!")
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
    fun getHoje():String{

        try {

            val date = Date()

            val format = SimpleDateFormat("dd/MM/yyyy")

            val data = format.format(date)

            return data

        } catch (e:Exception)
        {
            return ""
        }

    }
}