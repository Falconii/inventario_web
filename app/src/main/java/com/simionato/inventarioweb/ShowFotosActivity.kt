package com.simionato.inventarioweb

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.FotoAdapter
import com.simionato.inventarioweb.databinding.ActivityShowFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.parametros.ParametroFoto01
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowFotosActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityShowFotosBinding.inflate(layoutInflater)
    }

    private var params:ParametroFoto01 = ParametroFoto01()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getFotos()
        /*
        try {
            val url =
                URL("https://drive.google.com/uc?export=view&id=1QSfT3hO40mN-9znlqq7hmdHWQRBe2kRi")
            Glide.with(this)
                .load(url)
                .centerCrop()
                .into(binding.imFoto30)
            getFotos()

        } catch (e:Exception){
            Log.i("zyzz","Erro-> ${e.message}")
        }
*/
    }

    private fun getFotos(){
        try {
            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )
            params.id_empresa = ParametroGlobal.Dados.empresa.id
            params.id_local = ParametroGlobal.Dados.local.id
            params.id_inventario = ParametroGlobal.Dados.Inventario.codigo
            params.destaque = ""
            binding.llProgress30.visibility = View.VISIBLE
            fotoService.getFotos(params).enqueue(object :
                Callback<List<FotoModel>> {
                override fun onResponse(
                    call: Call<List<FotoModel>>,
                    response: Response<List<FotoModel>>
                ) {
                    binding.llProgress30.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var fotos = response.body()

                            if (fotos !== null) {

                                val adapter = FotoAdapter(fotos){foto ->
                                    chamaFotoWeb(foto)
                                }
                                binding.rvLista30.adapter = adapter
                                binding.rvLista30.layoutManager =
                                    LinearLayoutManager(binding.rvLista30.context)
                                binding.rvLista30.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista30.context,
                                        RecyclerView.VERTICAL
                                    )
                                )


                            } else {
                                showToast("Falha No Retorno Da Requisição!")
                            }

                        }
                        else {
                            binding.llProgress30.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            var locais: List<LocalModel> = listOf()
                            if (response.code() == 409){
                                showToast("Tabela De Fotos Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress30.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<List<FotoModel>>, t: Throwable) {
                    binding.llProgress30.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress30.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    fun chamaFotoWeb(foto:FotoModel){
        val intent = Intent(this,FotoWebActivity::class.java)
        intent.putExtra("id_file",foto.id_file)
        startActivity(intent)
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}