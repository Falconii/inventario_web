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
        inflateTela()

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

        binding.ivSemUsuarioEntrar00.setOnClickListener {
           chamaLogin()
        }

        binding.btLancamentoUnico00.setOnClickListener {
            chamaLancamento()
        }

        binding.btInventarios00.setOnClickListener {
            chamaInventario()
        }

        binding.btNovoProduto00.setOnClickListener{
            chamaProduto()
        }

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
        binding.ToolBar00.setTitle(R.string.titulo_barra)
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
                    chamaLogin()
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

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

    private fun inflateTela(){
        if (usuario.id > 0) {
            binding.llSemUsuario00.visibility = View.GONE
            binding.llScrollView.visibility = View.VISIBLE
        } else {
            binding.llSemUsuario00.visibility = View.VISIBLE
            binding.llScrollView.visibility = View.GONE
        }
        showHeader()
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
               inflateTela()
            }
            if(it.resultCode == 100){
                empresa = EmpresaModel()
                usuario = UsuarioModel()
                local = LocalModel()
                Inventario = InventarioModel()
                inflateTela()
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
                inflateTela()
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



