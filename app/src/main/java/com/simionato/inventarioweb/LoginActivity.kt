package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.simionato.inventarioweb.databinding.ActivityLoginBinding
import com.simionato.inventarioweb.infra.InfraHelper.Companion.apiInventario
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.services.EmpresaService
import com.simionato.inventarioweb.services.UsuarioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            getEmpresa(1)
        }

        binding.btnValidar.setOnClickListener{
            try {
                var codigo = binding.editCodigo.text.toString().toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    getUsuario(codigo)
                }
            } catch ( e : NumberFormatException ){
                binding.txtResultado.setText("Código Inválido!")
            }

        }

        binding.btnCancelar.setOnClickListener{
            finish()
        }
    }


    private suspend fun getUsuario(id:Int){

        var retorno: Response<UsuarioModel>? = null

        try {
            val usuarioService = apiInventario.create( UsuarioService::class.java )
            retorno = usuarioService.getUsuario(1,id)
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("login",e.message as String)
        }

        if ( retorno != null ){
            if( retorno.isSuccessful ){
                val usuario = retorno.body()
                if (usuario != null){
                    if (usuario.senha == binding.editSenha.text.toString()){
                        binding.txtResultado.setText("Deu Certo!")
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.txtResultado.setText("Código Ou Senha Inválidos")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main){
                        binding.txtResultado.setText("Falha No Login")
                    }
                }
            }else{
                withContext(Dispatchers.Main){
                    binding.txtResultado.setText("Falha No Login!")
                }
            }
        } else {
            Log.e("login","Falha Na Pesquisa!")
        }
    }

    private suspend fun getEmpresa(id:Int){

        var retorno: Response<EmpresaModel>? = null

        try {
            val empresaAPI = apiInventario.create( EmpresaService::class.java )
            retorno = empresaAPI.getEmpresa(1)
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("login",e.message as String)
        }

        if ( retorno != null ){
            if( retorno.isSuccessful ){
                val empresa = retorno.body()
                if (empresa != null){
                        withContext(Dispatchers.Main) {
                            binding.editEmpresa.setText("${empresa.fantasi}")
                    }
                } else {
                    withContext(Dispatchers.Main){
                        binding.txtResultado.setText("[CODE] ${retorno.code()} - ${retorno.body()}")
                    }
                }
            }else{
                withContext(Dispatchers.Main){
                    binding.txtResultado.setText("Falha Na Pesquisa Da Empresa")
                }
            }
        } else {
            Log.e("login","Falha Na Pesquisa!")
        }
    }
}