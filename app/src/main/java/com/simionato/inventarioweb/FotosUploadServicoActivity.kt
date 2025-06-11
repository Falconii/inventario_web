package com.simionato.inventarioweb

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.adapters.FotoUploadAdapter
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.databinding.ActivityFotosUploadServicoBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.models.FotoUploadModel

class FotosUploadServicoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFotosUploadServicoBinding.inflate(layoutInflater)
    }
    private val daoFoto by lazy {
        daoFotoUpload(DatabaseHelper(applicationContext));
    }

    private var fotos:List<FotoUploadModel> = listOf();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ParametroGlobal.Ambiente.itsOK()){
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
        }  catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }
        setContentView(binding.root)

        //binding.llProgress20.visibility = View.GONE

        iniciar()
    }

    fun iniciar(){
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

    fun getFotos(){

        try {
             binding.llProgress77.visibility = View.VISIBLE
             this.fotos = daoFoto.getPhotos()
             binding.llProgress77.visibility = View.GONE
             val adapter = FotoUploadAdapter(fotos,{foto ->
                finish()
            })
            binding.rvLista77.adapter = adapter
            binding.rvLista77.layoutManager =
                LinearLayoutManager(binding.rvLista77.context)
            binding.rvLista77.addItemDecoration(
                DividerItemDecoration(
                    binding.rvLista77.context,
                    RecyclerView.VERTICAL
                )
            )

        } catch (e: Exception){
            binding.llProgress77.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}