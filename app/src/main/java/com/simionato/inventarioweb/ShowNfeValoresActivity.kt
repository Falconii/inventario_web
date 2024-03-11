package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityShowNfeValoresBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.ValorModel
import com.simionato.inventarioweb.parametros.ParametroNfe02
import com.simionato.inventarioweb.services.ValorService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowNfeValoresActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityShowNfeValoresBinding.inflate(layoutInflater)
    }

    private var imoinventario :ImobilizadoinventarioModel = ImobilizadoinventarioModel()

    private var paramNfe = ParametroNfe02()


     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ParametroGlobal.Ambiente.itsOK()){
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }
        try {
            val bundle = intent.extras
            Log.i("zyzz", "Chegue na showfotos ${bundle}")
            if (bundle != null) {
                imoinventario = if (Build.VERSION.SDK_INT >= 33) bundle.getParcelable(
                    "ImoInventario",
                    ImobilizadoinventarioModel::class.java
                )!!
                else bundle.getParcelable("ImoInventario")!!
            } else {
                showToast("Parâmetro Do Inventário Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }
        setContentView(binding.root)
         iniciar()
    }

    private fun iniciar(){
        inicializarTooBar()
        binding.txtViewSituacao75.setText(ParametroGlobal.prettyText.ambiente_produto(imoinventario.id_imobilizado,imoinventario.imo_descricao))
        getValores()
    }

    private fun inicializarTooBar(){
        binding.ToolBar75.title = "Controle De Ativos"
        binding.ToolBar75.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar75.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar75.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar75.inflateMenu(R.menu.menu_show_nfe_valores)
        binding.ToolBar75.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_show_nfe_valores_exit -> {
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


    private fun getNfe(){
     /*
            param.id_empresa = imoinventario.id_empresa
            param.id_filial  = imoinventario.id_filial
            param.cnpj_fornecedor = imoinventario.imo_nfe
            param.razao_fornecedor: String ,
            param.id_imobilizado: Int ,
            param.nfe: String ,
            param.serie: String ,
            param.item: String
        }
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create( ImobilizadoInventarioService::class.java )
            binding.llProgress75.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :

        }catch (e: Exception){
            binding.llProgress75.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }
       */
    }

    private fun getValores(){
    try {
        val valorService = InfraHelper.apiInventario.create( ValorService::class.java )
        binding.llProgress75.visibility = View.VISIBLE
        valorService.getValor(imoinventario.id_empresa,imoinventario.id_filial,imoinventario.id_imobilizado).enqueue(object : Callback<ValorModel>{
            override fun onResponse(call: Call<ValorModel>, response: Response<ValorModel>) {
                binding.llProgress75.visibility = View.GONE
                if (response != null) {
                    if (response.isSuccessful) {

                        var valor = response.body()

                        if (valor != null){
                            binding.llValores.visibility = View.VISIBLE
                            loadValor(valor)
                        } else {
                            binding.llValores.visibility = View.GONE
                        }

                    }
                    else {
                        binding.llProgress75.visibility = View.GONE
                        val gson = Gson()
                        val message = gson.fromJson(
                            response.errorBody()!!.charStream(),
                            HttpErrorMessage::class.java
                        )
                        if (response.code() == 409){
                            showToast("Valores Não Encontrados!")
                        } else {
                            showToast(message.getMessage().toString())
                        }
                        binding.llValores.visibility = View.GONE
                    }
                } else {
                    binding.llProgress75.visibility = View.GONE
                    Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<ValorModel>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

    }catch (e: Exception){
        binding.llProgress75.visibility = View.GONE
        showToast("${e.message.toString()}",Toast.LENGTH_LONG)
    }
    }

    private fun loadValor(valor:ValorModel){

        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.setMaximumFractionDigits(2)
        format.setCurrency(Currency.getInstance("BRL"))

        binding.editValores0175.setText(ParametroGlobal.prettyText.tituloDescricaoDois(
          "Aquisição: ",
          valor.dtaquisicao,
          "Vlr Aquis.: ",format.format(valor.vlraquisicao)))

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}