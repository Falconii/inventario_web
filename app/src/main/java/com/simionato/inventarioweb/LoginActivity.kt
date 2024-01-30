package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityLoginBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.UserProfile
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.parametros.ParametroEmpresa01
import com.simionato.inventarioweb.services.EmpresaService
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


private val id_empresa_key = intPreferencesKey("id_empresa")
private val id_usuario_key = intPreferencesKey("id_usuario")

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarTooBar()
        lifecycleScope.launch(Dispatchers.IO){
            getUserProfile().collect{
                withContext(Dispatchers.Main){
                    ParametroGlobal.Dados.empresa.id = it.id_empresa
                    getEmpresas()
                }
            }
        }

        binding.btLoginEntrar01.setOnClickListener{
            try {
                var codigo = binding.editCodigo01.text.toString().toInt()
                getUsuario(codigo)
            } catch ( e : NumberFormatException ){
                Toast.makeText(this,"Código Inválido!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btLoginSair01.setOnClickListener{
            saveParametrosSair()
            val returnIntent: Intent = Intent()
            returnIntent.putExtra("id_empresa",1)
            returnIntent.putExtra("id_usuario",0)
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()

        }
    }

    private fun saveParametrosSair(){
        lifecycleScope.launch(Dispatchers.IO) {
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
                    val returnIntent: Intent = Intent()
                    returnIntent.putExtra("id_empresa",0)
                    returnIntent.putExtra("id_usuario",0)
                    setResult(100,returnIntent)
                    finish()
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
    private  fun getUsuario(id_usuario:Int){
        try {
            val usuarioService = InfraHelper.apiInventario.create( UsuarioService::class.java )
            binding.llProgress01.visibility = View.VISIBLE
            usuarioService.getUsuario(1,id_usuario).enqueue(object :Callback<UsuarioModel>{
                override fun onResponse(
                    call: Call<UsuarioModel>,
                    response: Response<UsuarioModel>
                ) {
                    binding.llProgress01.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var usuario = response.body()

                            usuario = (usuario ?: UsuarioModel()) as UsuarioModel

                            if (usuario.senha.trim() != binding.editSenha01.text.toString()
                                    .trim()
                            ) {
                                showToast("Problemas Com O Usuário!!")
                            } else {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    saveUserProfile(usuario.id_empresa, usuario.id)
                                    withContext(Dispatchers.Main) {
                                        val returnIntent: Intent = Intent()
                                        returnIntent.putExtra("id_empresa",usuario.id_empresa)
                                        returnIntent.putExtra("id_usuario",usuario.id)
                                        setResult(Activity.RESULT_OK,returnIntent)
                                        finish()
                                    }
                                }
                            }
                        }
                        else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Usuário Não Encontrado! ${message.getMessage().toString()}",Toast.LENGTH_SHORT)
                            } else {
                                Toast.makeText(applicationContext,message.getMessage().toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UsuarioModel>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
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

                            var empresas = response.body()

                            val empresa = empresas?.find { emp -> emp.id == ParametroGlobal.Dados.empresa.id }

                            with(binding) {
                                binding.editEmpresa.setText(empresa!!.razao ?: "")
                            }

                        }
                        else {
                            binding.llProgress01.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Empresas Não Encontrada! ${message.getMessage().toString()}",
                                    Toast.LENGTH_SHORT)
                                Toast.makeText(applicationContext,"Empresas Não Encontrada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(applicationContext,message.getMessage().toString(),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.llProgress01.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<EmpresaModel>>, t: Throwable) {
                    binding.llProgress01.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress01.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }

    private suspend fun saveUserProfile(id_empresa: Int, id_usuario: Int) {
        dataStore.edit { preferences ->
            preferences[id_empresa_key] = id_empresa
            preferences[id_usuario_key] = id_usuario
        }
        Log.i("zyzz","Salvei -> ${id_empresa} ${id_usuario}")
    }


 }
