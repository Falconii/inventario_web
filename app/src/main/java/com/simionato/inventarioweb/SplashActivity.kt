package com.simionato.inventarioweb

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.databinding.ActivitySplashBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.empresa
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.local
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.global.UserProfile
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.AmbienteModel
import com.simionato.inventarioweb.models.PadraoModel
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

class SplashActivity : AppCompatActivity() {

    //val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "INVENTARIO_PREFERS")
    //private val id_empresa_key = intPreferencesKey("id_empresa")
    //private val id_usuario_key = intPreferencesKey("id_usuario")
    
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        binding.llProgress90.visibility = View.GONE

        chamaMain()

      /*  lifecycleScope.launch(Dispatchers.IO) {
            getUserProfile().collect {
                withContext(Dispatchers.Main) {
                    Log.i("zyzz", "Memoria :${it}")
                    if (it.id_usuario == 0){
                        //Log.i("zyzz", "Vou chamar login, pois usuário é zero")
                        //chamaLogin()
                    } else {
                        Log.i("zyzz", "Vou carregar padrao ${it.id_empresa} ${it.id_usuario}")
                        getPadrao(it.id_empresa,it.id_usuario)
                    }
                }
            }
        }*/
    }

    private fun getPadrao(id_empresa: Int,id_usuario: Int) {
        try {
            val padraoService = InfraHelper.apiInventario.create(PadraoService::class.java)
            binding.llProgress90.visibility = View.VISIBLE
            padraoService.getPadrao(
                id_empresa,
                id_usuario
            ).enqueue(object :
                Callback<PadraoModel> {
                override fun onResponse(
                    call: Call<PadraoModel>,
                    response: Response<PadraoModel>
                ) {
                    binding.llProgress90.visibility = View.GONE
                    Log.i("zyzz","Retornei da request ${response} ${response.isSuccessful}")
                    if (response != null) {
                        if (response.isSuccessful) {

                            var padrao = response.body()

                            padrao = (padrao ?: PadraoModel()) as PadraoModel

                            Log.i("zyzz","Achei O Padrão ${padrao}")

                            getAmbiente(padrao)

                        } else {
                            binding.llProgress90.visibility = View.GONE
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
                        binding.llProgress90.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<PadraoModel>, t: Throwable) {
                    binding.llProgress90.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            binding.llProgress90.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    /*private fun getUserProfile() = dataStore.data.map { preferences ->
        UserProfile(
            id_empresa = preferences[id_empresa_key] ?: 1,
            id_usuario = preferences[id_usuario_key] ?: 0
        )
    }*/
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
                    binding.llProgress90.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ambiente = response.body()

                            if (ambiente != null )
                                if (ambiente.id_retorno == 200) {
                                    empresa = ambiente.empresa!!
                                    usuario = ambiente.usuario!!
                                    local = ambiente.local!!
                                    Inventario = ambiente.inventario!!
                                    showToast("Ambiente Carregado Com Sucesso!")
                                    chamaMain()
                                } else {
                                    showToast("Ambiente Não Disponivel Para Este Usuário!")
                                }
                        } else {
                            binding.llProgress90.visibility = View.GONE
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
                        //binding.llProgress90.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição! Tente Mais Tarde!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AmbienteModel>, t: Throwable) {
                    binding.llProgress90.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })
        } catch (e: Exception) {
            binding.llProgress90.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }


    }

    fun chamaMain(){
        val intent = Intent(this,MainActivity::class.java)
        getRetornoMain.launch(intent)
    }

    private val getRetornoMain =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            Log.i("zyzz","Retorno da Main")
            finish()
        }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}