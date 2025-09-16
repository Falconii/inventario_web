package com.simionato.inventarioweb

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.adapters.FotoUploadAdapter
import com.simionato.inventarioweb.adapters.FotoaAtivosLocalAdapter
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.databinding.ActivityFotosLocaisBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.EstadoUpload
import com.simionato.inventarioweb.global.showToast
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.models.FotoUploadModel
import kotlinx.coroutines.launch
import java.io.File

class FotosLocaisActivity : AppCompatActivity() {

    private val adapter = FotoaAtivosLocalAdapter()

    private val binding by lazy { ActivityFotosLocaisBinding.inflate(layoutInflater) }

    private var imobilizado = 0;


    private lateinit var daoFoto: daoFotoUpload


    private var fotos: List<FotoUploadModel> = listOf();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //Validando Paramentros
        if ((ParametroGlobal.Dados.usuario.id == 0)   ||
            (ParametroGlobal.Dados.local.id   == 0)   ||
            (ParametroGlobal.Dados.Inventario.codigo == 0)){
            showToast(applicationContext,"Ambiente Incorreto!!")
            finish()
        }

        try {
            val bundle = intent.extras
            if (bundle != null) {
                imobilizado = bundle.getInt("imobilizado",0)
            } else {
                showToast(applicationContext,"Parâmetro Código Da Ativo Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast(applicationContext,"Erro Nos Parametros: ${error.message}")
            finish()
        }

        val dbHelper = DatabaseHelper.getInstance(this)
        if (dbHelper == null) {
            Toast.makeText(this, "Banco de dados não disponível", Toast.LENGTH_LONG).show()
            return
        }

        // Instancia o DAO
        daoFoto = daoFotoUpload(dbHelper)

        iniciar()
    }

    private fun iniciar(){
        inicializarToolbar()
        getFotos()
    }

    private fun inicializarToolbar() = with(binding.ToolBar106) {

        title    = "Controle De Ativos"
        subtitle = com.simionato.inventarioweb.global.ParametroGlobal.Dados.Inventario.descricao
        setTitleTextColor(androidx.core.content.ContextCompat.getColor(context, com.simionato.inventarioweb.R.color.white))
        setSubtitleTextColor(androidx.core.content.ContextCompat.getColor(context, com.simionato.inventarioweb.R.color.white))
        inflateMenu(com.simionato.inventarioweb.R.menu.menu_login)

        setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.simionato.inventarioweb.R.id.item_cancel) {
                setResult(android.app.Activity.RESULT_OK, Intent())
                finish()
            }
            true
        }
    }



    fun getFotos() {

        try {
            binding.llProgress106.visibility = View.VISIBLE
            this.fotos = daoFoto.getPhotosAll()
            ativarAdapter()
            binding.llProgress106.visibility = View.GONE
        } catch (e: Exception) {
            binding.llProgress106.visibility = View.GONE
            showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }

    fun ativarAdapter(){

        binding.rvLista106.adapter = adapter
        adapter.lista = this.fotos;
        binding.rvLista106.layoutManager =
            LinearLayoutManager(binding.rvLista106.context)
        binding.rvLista106.addItemDecoration(
            DividerItemDecoration(
                binding.rvLista106.context,
                RecyclerView.VERTICAL
            )
        )

    }
}