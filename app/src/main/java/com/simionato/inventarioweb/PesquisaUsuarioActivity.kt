package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.UsuarioAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaUsuarioBinding
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.UsuarioQuery01Model
import com.simionato.inventarioweb.parametros.ParametroUsuario01
import com.simionato.inventarioweb.services.UsuarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PesquisaUsuarioActivity : AppCompatActivity() {
    val params : ParametroUsuario01 = ParametroUsuario01(
        1,
        0,
        "",
        "",
        0,
        0,
        50,
        "N",
        "Código",
        true
    )

    private var usuarios = listOf<UsuarioQuery01Model>()

    private val binding by lazy {
        ActivityPesquisaUsuarioBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        iniciar()
    }

    private fun iniciar(){
        inicializarTooBar()

        getUsuarios()

    }

    private fun inicializarTooBar(){
        binding.ToolBar07.title = "Controle De Ativos"
        binding.ToolBar07.subtitle = "Pesquisa De Usuário"
        binding.ToolBar07.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar07.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar07.inflateMenu(R.menu.menu_pesquisa)
        binding.ToolBar07.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_pesquisa_sair -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private  fun getUsuarios(){
        try {
            val usuarioService = InfraHelper.apiInventario.create( UsuarioService::class.java )
            binding.llProgress07.visibility = View.VISIBLE
            usuarioService.getUsuarios(params).enqueue(object : Callback<List<UsuarioQuery01Model>> {
                override fun onResponse(
                    call: Call<List<UsuarioQuery01Model>>,
                    response: Response<List<UsuarioQuery01Model>>
                ) {
                    binding.llProgress07.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {


                            val usuarios = response.body()

                            var mensa:String = ""

                            if (usuarios !== null) {

                                val adapter = UsuarioAdapter(usuarios){usuario ->
                                    val returnIntent: Intent = Intent()
                                    returnIntent.putExtra("id_usuario",usuario.id)
                                    returnIntent.putExtra("razao",usuario.razao)
                                    setResult(Activity.RESULT_OK,returnIntent)
                                    finish()
                                }
                                binding.rvLista07.adapter = adapter
                                binding.rvLista07.layoutManager =
                                    LinearLayoutManager(binding.rvLista07.context)
                                binding.rvLista07.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista07.context,
                                        RecyclerView.VERTICAL
                                    )
                                )
                                binding.svPesquisa07.setOnQueryTextListener(object :
                                    OnQueryTextListener {
                                    override fun onQueryTextSubmit(p0: String?): Boolean {
                                        return false
                                    }

                                    override fun onQueryTextChange(newText: String?): Boolean {

                                        adapter.filter.filter(newText)

                                        return false
                                    }

                                })
                            }

                        }
                        else {
                            binding.llProgress07.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Usuário Não Encontrado! ${message.getMessage().toString()}",
                                    Toast.LENGTH_SHORT)
                            } else {
                                showToast(message.getMessage().toString(),
                                    Toast.LENGTH_SHORT)
                            }
                        }
                    } else {
                        binding.llProgress07.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!", Toast.LENGTH_SHORT)
                    }
                }
                override fun onFailure(call: Call<List<UsuarioQuery01Model>>, t: Throwable) {
                    binding.llProgress07.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress07.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

        }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }

}