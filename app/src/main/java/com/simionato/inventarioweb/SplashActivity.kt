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
import com.simionato.inventarioweb.models.mensagemModel
import com.simionato.inventarioweb.services.HelloService
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


        splash()

    }


    private fun splash() {
        try {
            val helloService = InfraHelper.apiInventario.create(HelloService::class.java)
            helloService.splash().enqueue( object : Callback<mensagemModel>{
                override fun onResponse(
                    call: Call<mensagemModel>,
                    response: Response<mensagemModel>
                ) {
                    Log.i("zyzz","Retorno ${response.isSuccessful} ${response.code()}")
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ambiente = response.body()

                            if (ambiente != null )
                                chamaMain()
                                } else {
                                    showToast("Falha Na Comunicação. Sem retorno")
                                    finish()
                                }
                        } else {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                        showToast("Falha Na Comunicação Com A Nuvem! Tente Mais Tarde")
                        finish()
                        }
                }

                override fun onFailure(call: Call<mensagemModel>, t: Throwable) {
                    showToast("${t.message}")
                    finish()
                }
            })
        } catch (e: Exception) {
            showToast("Falha Interna ${e.message}")
            finish()
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