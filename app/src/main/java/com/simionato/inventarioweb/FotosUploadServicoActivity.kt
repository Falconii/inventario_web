package com.simionato.inventarioweb

import android.os.Bundle
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
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
}