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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityLoginBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.empresa
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.local
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.global.UserProfile
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.AmbienteModel
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.PadraoModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.parametros.ParametroEmpresa01
import com.simionato.inventarioweb.services.EmpresaService
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

val paramEmpresa: ParametroEmpresa01 = ParametroEmpresa01(
    0,
    "",
    "",
    0,
    50,
    "N",
    "Código",
    true
)

private lateinit var dataStore: DataStore<Preferences>
private val id_empresa_key = intPreferencesKey("id_empresa")
private val id_usuario_key = intPreferencesKey("id_usuario")
private var localEmpresa: EmpresaModel = EmpresaModel()

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress01.visibility = View.GONE
        inicializarTooBar()
        getEmpresas()

        binding.btLoginEntrar01.setOnClickListener{
            try {
                var codigo = binding.editCodigo01.text.toString().toInt()
                getPadrao(localEmpresa.id,codigo)
            } catch ( e : NumberFormatException ){
                Toast.makeText(this,"Código Inválido!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btLoginSair01.setOnClickListener{
            //saveParametrosSair()
            empresa = EmpresaModel()
            usuario = UsuarioModel()
            local   = LocalModel()
            Inventario = InventarioModel()
            val returnIntent: Intent = Intent()
            setResult(100,returnIntent)
            finish()

        }
    }

    private fun saveParametrosSair(){
        lifecycleScope.launch{
            saveUserProfile(1, 0)
        }
    }
    private fun inicializarTooBar(){
        binding.toolBar01.title = "Controle De Ativos"
        binding.toolBar01.subtitle = "Login Do Sistema"
        binding.toolBar01.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.toolBar01.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.toolBar01.inflateMenu(R.menu.menu_login)
        binding.toolBar01.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    finishAndRemoveTask()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private  fun getUserProfile() =  dataStore.data.map { preferences ->
        UserProfile(
            id_empresa = preferences[id_empresa_key] ?: 1,
            id_usuario = preferences[id_usuario_key] ?:1
        )
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
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ambiente = response.body()

                            if (ambiente != null )
                                if (ambiente.id_retorno == 200) {
                                    empresa = ambiente.empresa!!
                                    usuario = ambiente.usuario!!
                                    local = ambiente.local!!
                                    Inventario = ambiente.inventario!!
                                    if (usuario.senha.trim() == binding.editSenha01.text.toString().trim()){
                                        showToast("Ambiente Carregado Com Sucesso!")
                                        val returnIntent: Intent = Intent()
                                        setResult(RESULT_OK,returnIntent)
                                        finish()
                                    } else {
                                        showToast("Usuário E/OU Senha Inválidos!. Verifique")

                                    }
                                } else {
                                    showToast("Ambiente Não Disponivel Para Este Usuário!")
                                    finish()
                                }
                        } else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            showToast(
                                "${message.getMessage().toString()}",
                                Toast.LENGTH_SHORT
                            )
                            finish()
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição! Tente Mais Tarde!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<AmbienteModel>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                    finish()
                }
            })
        } catch (e: Exception) {
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            finish()
        }


    }
    private fun getPadrao(id_empresa: Int,id_usuario: Int) {
        try {
            val padraoService = InfraHelper.apiInventario.create(PadraoService::class.java)
            binding.llProgress01.visibility = View.VISIBLE
            padraoService.getPadrao(
                id_empresa,
                id_usuario
            ).enqueue(object :
                Callback<PadraoModel> {
                override fun onResponse(
                    call: Call<PadraoModel>,
                    response: Response<PadraoModel>
                ) {
                    binding.llProgress01.visibility = View.GONE
                    Log.i("zyzz","Retornei da request ${response} ${response.isSuccessful}")
                    if (response != null) {
                        if (response.isSuccessful) {

                            var padrao = response.body()

                            padrao = (padrao ?: PadraoModel()) as PadraoModel

                            getAmbiente(padrao)

                        } else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast(
                                    "Usuário Não Tem Padrao! Verifique Com o Coordenador!",
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                showToast(message.getMessage().toString())
                            }
                            finishSair()
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        showToast("Sem retorno Da Requisição! Tente Mais Tarde")
                        finishSair()
                    }
                }

                override fun onFailure(call: Call<PadraoModel>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                    finishSair()
                }
            })

        } catch (e: Exception) {
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            finishSair()
        }

    }

    private fun getEmpresas(){
        try {
            val empresaService = InfraHelper.apiInventario.create( EmpresaService::class.java )
            binding.llProgress01.visibility = View.VISIBLE
            empresaService.getEmpresas(paramEmpresa).enqueue(object :
                Callback<List<EmpresaModel>> {
                override fun onResponse(
                    call: Call<List<EmpresaModel>>,
                    response: Response<List<EmpresaModel>>
                ) {
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var empresas = response.body()!!

                            //localEmpresa = empresas?.find { emp -> emp.id == ParametroGlobal.Dados.empresa.id }!!

                            if (empresas.size > 0 ){
                                localEmpresa = empresas?.get(0)!!

                                binding.editEmpresa01.setText(localEmpresa.razao ?: "")



                            } else {
                                Log.i("zyzz","Nehuma Empresa Cadastrada! size == 0")
                                showToast("Nehuma Empresa Cadastrada!",Toast.LENGTH_LONG)
                                finishSair()
                            }
                            Log.i("zyzz","local empresa ${localEmpresa.razao}")

                        }
                        else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                Log.i("zyzz","Nehuma Empresa Cadastrada! 409")
                                showToast("Nehuma Empresa Cadastrada!",Toast.LENGTH_LONG)
                                finishSair()
                            } else {
                                showToast(message.getMessage().toString())
                                Log.i("zyzz","Nehuma Empresa Cadastrada! 409")
                                finishSair()
                            }
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Log.i("zyzz","Sem retorno Da Requisição! Tente Mais Tarde !")
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição! Tente Mais Tarde !", Toast.LENGTH_SHORT).show()
                        finishSair()
                    }
                }
                override fun onFailure(call: Call<List<EmpresaModel>>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    Log.i("zyzz","onFailure ${t.message.toString()}")
                    showToast(t.message.toString())
                    finishSair()
                }
            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            Log.i("zyzz","onFailure ${e.message.toString()}")
            showToast(" catch ${e.message.toString()}", Toast.LENGTH_LONG)
            finishSair()
        }
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }

    private suspend fun saveUserProfile(id_empresa: Int, id_usuario: Int) {
        dataStore.edit { preferences ->
            preferences[id_empresa_key] = id_empresa
            preferences[id_usuario_key] = id_usuario
            Log.i("zyzz","Salvei -> ${id_empresa} ${id_usuario}")
        }
    }

    private fun finishSair(){
        val returnIntent: Intent = Intent()
        setResult(100,returnIntent)
        finish()
    }


 }
