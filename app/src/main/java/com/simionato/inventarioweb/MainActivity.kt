package com.simionato.inventarioweb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.InventarioAdapter
import com.simionato.inventarioweb.adapters.LocalAdapter
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.UserProfile
import com.simionato.inventarioweb.infra.InfraHelper
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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "INVENTARIO_PREFERS")
private val id_empresa_key = intPreferencesKey("id_empresa")
private val id_usuario_key = intPreferencesKey("id_usuario")
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarTooBar()

       lifecycleScope.launch(Dispatchers.IO) {
            getUserProfile().collect {
                withContext(Dispatchers.Main) {
                    Log.i("zyz", "Memoria :${it}")
                    ParametroGlobal.Dados.empresa.id = it.id_empresa
                    ParametroGlobal.Dados.usuario.id_empresa = it.id_empresa
                    ParametroGlobal.Dados.usuario.id = it.id_usuario
                    if (it.id_usuario == 0){
                        chamaLogin()
                    } else {
                        getPadrao(it.id_empresa,it.id_usuario)
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            saveUserProfile(1, 16)
            Log.i("zyz","Procurando Padrão")
            getPadrao(1,16)
        }

        binding.btnLogin.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )


        }

        binding.btnLancamento.setOnClickListener {
            startActivity(
                Intent(this, LancamentoActivity::class.java)
            )
        }

        binding.btnInventario.setOnClickListener {
            startActivity(
                Intent(this, InventarioActivity::class.java)
            )
        }

        binding.btnPesquisa.setOnClickListener {
            startActivity(
                Intent(this, PesquisaActivity::class.java)
            )
        }

        binding.btnParametros.setOnClickListener {
            startActivity(
                Intent(this, ParametrosActivity::class.java)
            )
        }

        binding.btnProduto.setOnClickListener({
            lifecycleScope.launch(Dispatchers.IO) {
                getUserProfile().collect {
                    withContext(Dispatchers.Main) {
                        binding.lblNomeUsuario.setText(it.id_usuario.toString())
                    }
                }
            }
            //startActivity(
            //    Intent(this, ProdutoActivity::class.java)
            //)
        })
    }

    private fun getPadrao(id_empresa: Int,id_ususario: Int) {
        try {
            val padraoService = InfraHelper.apiInventario.create(PadraoService::class.java)
            //binding.llProgress02.visibility = View.VISIBLE
            padraoService.getPadrao(
                id_empresa,
                id_ususario
            ).enqueue(object :
                Callback<PadraoModel> {
                override fun onResponse(
                    call: Call<PadraoModel>,
                    response: Response<PadraoModel>
                ) {
                    //binding.llProgress02.visibility = View.GONE
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
                    finish()
                    return@setOnMenuItemClickListener true
                }

                R.id.item_login -> {
                    startActivity(
                        Intent(applicationContext, LoginActivity::class.java)
                    )
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

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

    private fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(baseContext, mensagem, duracao).show()
    }

    private fun showHeader()
    {
        binding.editEmpresa00.setText("${ParametroGlobal.Dados.empresa.razao}")
        binding.editUsuario00.setText("${ParametroGlobal.Dados.usuario.razao}")
        binding.editLocal00.setText("${ParametroGlobal.Dados.local.razao}")
        binding.editInventario00.setText("${ParametroGlobal.Dados.Inventario.descricao}")
    }

    private fun chamaLogin(){
        startActivity(
            Intent(applicationContext, LoginActivity::class.java)
        )
    }
}



