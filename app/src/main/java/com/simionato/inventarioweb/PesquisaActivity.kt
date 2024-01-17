package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simionato.inventarioweb.adapters.PesquisaAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaBinding
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.UsuarioQuery01Model
import com.simionato.inventarioweb.parametros.ParametroUsuario01
import com.simionato.inventarioweb.services.UsuarioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class PesquisaActivity : AppCompatActivity() {
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
        ActivityPesquisaBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.IO).launch {
            getUsuarios()
        }

    }

    private suspend fun getUsuarios(){
        var retorno: Response<List<UsuarioQuery01Model>>? = null
        try {
            val usuarioService = InfraHelper.apiInventario.create( UsuarioService::class.java )
            retorno = usuarioService.getUsuarios(params)

        }catch (e: Exception){
            e.printStackTrace()
            Log.e("lista",e.message as String)
        }

        if ( retorno != null ){
            if( retorno.isSuccessful ){
                val usuarios = retorno.body()
                var mensa:String = ""

                if (usuarios !== null) {
                    withContext(Dispatchers.Main) {
                        val adapter = PesquisaAdapter(usuarios)
                        binding.rvLista.adapter = adapter
                        binding.rvLista.layoutManager = LinearLayoutManager(binding.rvLista.context)
                        binding.rvLista.addItemDecoration(
                            DividerItemDecoration( binding.rvLista.context,RecyclerView.VERTICAL)
                        )
                        binding.svPesquisa.setOnQueryTextListener(object : OnQueryTextListener {
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
            }else{
                Log.i("lista","[CODE] ${retorno.code()}\n${retorno.body()}\n${retorno.raw()}")
            }
        } else {
            Log.e("lista","Falha Na Pesquisa!")
        }

    }
}