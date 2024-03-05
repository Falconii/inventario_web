package com.simionato.inventarioweb

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.empresa
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.local
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.global.UserProfile
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.AmbienteModel
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.PadraoModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.services.EmpresaService
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.services.LocalService
import com.simionato.inventarioweb.services.PadraoService
import com.simionato.inventarioweb.services.UsuarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "INVENTARIO_PREFERS")
//private val id_empresa_key = intPreferencesKey("id_empresa")
//private val id_usuario_key = intPreferencesKey("id_usuario")
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarTooBar()
        showHeader()
        Log.i("zyzz","Start Main Activity !!!!!!!")
        /*
       lifecycleScope.launch(Dispatchers.IO) {
            getUserProfile().collect {
                withContext(Dispatchers.Main) {
                    Log.i("zyzz", "Memoria :${it}")
                    ParametroGlobal.Dados.empresa.id = it.id_empresa
                    ParametroGlobal.Dados.usuario.id_empresa = it.id_empresa
                    ParametroGlobal.Dados.usuario.id = it.id_usuario
                    if (it.id_usuario == 0){
                        //Log.i("zyzz", "Vou chamar login, pois usuário é zero")
                        //chamaLogin()
                    } else {
                        Log.i("zyzz", "Vou carregar padrao ${it.id_empresa} ${it.id_usuario}")
                        getPadrao(it.id_empresa,it.id_usuario)
                    }
                }
            }


        }
*/
        /*binding.btnLogin.setOnClickListener {
           chamaLogin()
        }

        binding.btnLancamento.setOnClickListener {
            chamaLancamento()
        }

        binding.btnInventario00.setOnClickListener {
            chamaInventario()
        }

        binding.btnPesquisa.setOnClickListener {
            startActivity(
                Intent(this, PesquisaUsuarioActivity::class.java)
            )
        }

        binding.btnParametros.setOnClickListener {
            startActivity(
                Intent(this, SplashActivity::class.java)
            )
        }

        binding.btnProduto.setOnClickListener{
            chamaProduto()
        }

        binding.btnShowFotos.setOnClickListener{
            getImoInventario()
        }

        binding.btnFotoWeb.setOnClickListener{
            chamaFotoWeb()
        }*/
    }

    /*private fun getPadrao(id_empresa: Int,id_usuario: Int) {
        try {
            val padraoService = InfraHelper.apiInventario.create(PadraoService::class.java)
            //binding.llProgress02.visibility = View.VISIBLE
            padraoService.getPadrao(
                id_empresa,
                id_usuario
            ).enqueue(object :
                Callback<PadraoModel> {
                override fun onResponse(
                    call: Call<PadraoModel>,
                    response: Response<PadraoModel>
                ) {
                    //binding.llProgress02.visibility = View.GONE
                    Log.i("zyzz","Retornei da request ${response}")
                    if (response != null) {
                        if (response.isSuccessful) {

                            var padrao = response.body()

                            padrao = (padrao ?: PadraoModel()) as PadraoModel


                            Log.i("zyz","Achei O Padrão ${padrao}")

                            ParametroGlobal.Dados.empresa.id = padrao.id_empresa_padrao

                            ParametroGlobal.Dados.usuario.id_empresa = padrao.id_empresa_padrao
                            ParametroGlobal.Dados.usuario.id = padrao.id_usuario

                            ParametroGlobal.Dados.local.id_empresa = padrao.id_empresa_padrao
                            ParametroGlobal.Dados.local.id = padrao.id_local_padrao

                            ParametroGlobal.Dados.Inventario.id_empresa = padrao.id_empresa_padrao
                            ParametroGlobal.Dados.Inventario.id_filial = padrao.id_local_padrao
                            ParametroGlobal.Dados.Inventario.codigo = padrao.id_inv_padrao
                            loadParametros()

                        } else {
                            //binding.llProgress02.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast(
                                    "Usuário Não Tem Padrao! ${message.getMessage().toString()}",
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        }
                    } else {
                        //binding.llProgress02.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<PadraoModel>, t: Throwable) {
                    //binding.llProgress02.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            //binding.llProgress02.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
*/
    private fun inicializarTooBar() {
        binding.ToolBar00.title = "Controle De Ativos"
        binding.ToolBar00.subtitle = "Tela Principal"
        binding.ToolBar00.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar00.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar00.inflateMenu(R.menu.menu_principal)
        binding.ToolBar00.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_cancel -> {
                    //finishAndRemoveTask()
                    finish()
                    return@setOnMenuItemClickListener true
                }

                R.id.item_login -> {
                    chamaLogin()
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
/*
    private suspend fun saveUserProfile(id_empresa: Int, id_ususario: Int) {
        dataStore.edit { preferences ->
            preferences[id_empresa_key] = id_empresa
            preferences[id_usuario_key] = id_ususario
        }
        Log.i("zyz", "Salvo id_empresa ${id_empresa} id_ususario ${id_ususario}")
    }

    private fun loadParametros() {
        ParametroGlobal.Dados.empresa.razao = "Empresa Não Encontrada!"
        ParametroGlobal.Dados.usuario.razao = "Usuário Não Encontrada!"
        ParametroGlobal.Dados.local.razao = "Local Não Encontrada!"
        ParametroGlobal.Dados.Inventario.descricao = "Inventario Não Encontrado!"
        getUsuario()
    }

    private fun getUserProfile() = dataStore.data.map { preferences ->
        UserProfile(
            id_empresa = preferences[id_empresa_key] ?: 1,
            id_usuario = preferences[id_usuario_key] ?: 0
        )
    }

    private fun getUsuario() {
        try {
            val usuarioService = InfraHelper.apiInventario.create(UsuarioService::class.java)
            //binding.llProgress01.visibility = View.VISIBLE
            usuarioService.getUsuario(
                ParametroGlobal.Dados.usuario.id_empresa,
                ParametroGlobal.Dados.usuario.id
            )
                .enqueue(object : Callback<UsuarioModel> {
                    override fun onResponse(
                        call: Call<UsuarioModel>,
                        response: Response<UsuarioModel>
                    ) {
                        //binding.llProgress01.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                var usuario = response.body()

                                usuario = (usuario ?: UsuarioModel()) as UsuarioModel

                                ParametroGlobal.Dados.usuario = usuario



                                getEmpresa()
                            } else {
                                //binding.llProgress01.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    showToast(
                                        "Usuário Não Encontrado! ${
                                            message.getMessage().toString()
                                        }", Toast.LENGTH_SHORT
                                    )
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        message.getMessage().toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            //binding.llProgress01.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UsuarioModel>, t: Throwable) {
                        //binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }
                })

        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getEmpresa() {
        try {

            val empresaService = InfraHelper.apiInventario.create(EmpresaService::class.java)
            //binding.llProgress01.visibility = View.VISIBLE
            empresaService.getEmpresa(ParametroGlobal.Dados.empresa.id).enqueue(object :
                Callback<EmpresaModel> {
                override fun onResponse(
                    call: Call<EmpresaModel>,
                    response: Response<EmpresaModel>
                ) {
                    //binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var empresa = response.body()

                            ParametroGlobal.Dados.empresa =
                                (empresa ?: EmpresaModel()) as EmpresaModel

                            getLocal()

                        } else {
                            //binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast(
                                    "Empresas Não Encontrada!",
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                Toast.makeText(
                                    applicationContext, message.getMessage().toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        //binding.llProgress01.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<EmpresaModel>, t: Throwable) {
                    //binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }

    private fun getLocal() {
        try {
            val localService = InfraHelper.apiInventario.create(LocalService::class.java)
            // binding.llProgress01.visibility = View.VISIBLE
            localService.getLocal(
                ParametroGlobal.Dados.local.id_empresa,
                ParametroGlobal.Dados.local.id
            )
                .enqueue(object : Callback<LocalModel> {
                    override fun onResponse(
                        call: Call<LocalModel>,
                        response: Response<LocalModel>
                    ) {
                        //binding.llProgress01.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                var local = response.body()

                                local = (local ?: LocalModel()) as LocalModel

                                ParametroGlobal.Dados.local = local

                                getInventario()

                            } else {
                                //binding.llProgress01.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    showToast("Local Não Encontrado!")
                                } else {
                                    showToast("${message.getMessage().toString()}")
                                }
                            }

                        } else {
                            //binding.llProgress01.visibility = View.GONE
                            showToast("Sem retorno Da Requisição!")
                        }
                    }

                    override fun onFailure(call: Call<LocalModel>, t: Throwable) {
                        //binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }
                })

        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getInventario() {
        try {
            val inventarioService = InfraHelper.apiInventario.create(InventarioService::class.java)
            inventarioService.getInventario(
                ParametroGlobal.Dados.Inventario.id_empresa,
                ParametroGlobal.Dados.Inventario.id_filial,
                ParametroGlobal.Dados.Inventario.codigo
            )
                .enqueue(object : Callback<InventarioModel> {
                    override fun onResponse(
                        call: Call<InventarioModel>,
                        response: Response<InventarioModel>
                    ) {
                        //binding.llProgress01.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                var inventario = response.body()

                                inventario = (inventario ?: InventarioModel()) as InventarioModel

                                ParametroGlobal.Dados.Inventario = inventario

                                showHeader()

                            } else {
                                //binding.llProgress01.visibility = View.GONE
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
                            //binding.llProgress01.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<InventarioModel>, t: Throwable) {
                        //binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }

                })

        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }


    }

    private fun getAmbiente(padrao:PadraoModel) {
        try {
            val usuarioService = InfraHelper.apiInventario.create(UsuarioService::class.java)
            usuarioService.getAmbiente(
                padrao.id_empresa,
                padrao.id_usuario
            ).enqueue( object : Callback<AmbienteModel>{
                override fun onResponse(
                    call: Call<AmbienteModel>,
                    response: Response<AmbienteModel>
                ) {
                    //binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ambiente = response.body()



                        } else {
                            //binding.llProgress01.visibility = View.GONE
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
                        //binding.llProgress01.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AmbienteModel>, t: Throwable) {
                    //binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })
        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }


    }
*/
    private fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(baseContext, mensagem, duracao).show()
    }

    private fun showHeader() {
        if (usuario.id > 0) {
            binding.llParametros00.visibility = View.VISIBLE
            binding.editEmpresa00.setText("${ParametroGlobal.Dados.empresa.razao}")
            binding.editUsuario00.setText("${ParametroGlobal.Dados.usuario.razao}")
            binding.editLocal00.setText("${ParametroGlobal.Dados.local.razao}")
            binding.editInventario00.setText("${ParametroGlobal.Dados.Inventario.descricao}")
        } else {
            binding.llParametros00.visibility = View.GONE
        }
    }
    private fun chamaLogin(){
        val intent = Intent(this,LoginActivity::class.java)
        getRetornoLogin.launch(intent)
    }

    private val getRetornoLogin =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            Log.i("zyzz","Retorno do login ${it.resultCode}")
            if(it.resultCode == Activity.RESULT_OK){
               showHeader()
            }
            if(it.resultCode == 100){
                empresa = EmpresaModel()
                usuario = UsuarioModel()
                local = LocalModel()
                Inventario = InventarioModel()
                showHeader()
            }
        }

    private fun chamaParametro(){
        val intent = Intent(this,ParametrosActivity::class.java)
        getRetornoParametro.launch(intent)
    }

    private val getRetornoParametro =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            Log.i("zyzz","Retorno do login ${it.resultCode}")
            if(it.resultCode == Activity.RESULT_OK){
                showHeader()
            }
        }

    private fun chamaProduto(){
        val intent = Intent(this,ProdutoActivity::class.java)
        getRetornoProduto.launch(intent)
    }

    private val getRetornoProduto =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
            }
        }


    private fun chamaInventario(){
        val intent = Intent(this,InventarioActivity::class.java)
        getRetornoInventario.launch(intent)
    }

    private val getRetornoInventario =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
            }
        }

    private fun chamaShowFotos(imoInventario:ImobilizadoinventarioModel){
        val intent = Intent(this,ShowFotosActivity::class.java)
        intent.putExtra("ImoInventario",imoInventario)
        getRetornoShowFotos.launch(intent)
    }

    private val getRetornoLancamento =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
            }
        }

    private fun chamaLancamento(){
        val intent = Intent(this,LancamentoActivity::class.java)
        intent.putExtra("id_imobilizado",0)
        intent.putExtra("descricao","")
        getRetornoLancamento.launch(intent)
    }

    private val getRetornoShowFotos =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
            }
        }

    private fun chamaFotoWeb(){
        val intent = Intent(this,FotoWebActivity::class.java)
        getRetornoShowFotos.launch(intent)
    }

    private val getRetornoFotoWeb =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
            }
        }

    private fun getImoInventario() {
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create(ImobilizadoInventarioService::class.java)
            imobilizadoInventarioService.getImobilizadoInventario(
                ParametroGlobal.Dados.Inventario.id_empresa,
                ParametroGlobal.Dados.Inventario.id_filial,
                ParametroGlobal.Dados.Inventario.codigo,
                3
            )
                .enqueue(object : Callback<ImobilizadoinventarioModel> {
                    override fun onResponse(
                        call: Call<ImobilizadoinventarioModel>,
                        response: Response<ImobilizadoinventarioModel>
                    ) {
                        //binding.llProgress01.visibility = View.GONE
                        Log.i("zyzz","Indo buscar imoinventario")
                        if (response != null) {
                            if (response.isSuccessful) {
                                Log.i("zyzz","Achei imoinventario ${response.body()}")
                                val imoinvent = response.body()

                                Log.i("zyzz","Retorno ${imoinvent}")

                                if (imoinvent != null){
                                     chamaShowFotos(imoinvent)
                                } else {
                                    showToast("Dados De Inventário Não Localizado")
                                }

                            } else {
                                //binding.llProgress01.visibility = View.GONE
                                Log.i("zyzz","Deu Merda")
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
                            //binding.llProgress01.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ImobilizadoinventarioModel>, t: Throwable) {
                        //binding.llProgress01.visibility = View.GONE
                        showToast(t.message.toString())
                    }

                })

        } catch (e: Exception) {
            //binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }


    }

}



