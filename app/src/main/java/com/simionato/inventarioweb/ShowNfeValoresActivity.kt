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
import com.simionato.inventarioweb.models.GrupoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.NfeModel
import com.simionato.inventarioweb.models.ValorModel
import com.simionato.inventarioweb.parametros.ParametroNfe02
import com.simionato.inventarioweb.services.NfeService
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

    private var nfe: NfeModel = NfeModel()


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
        getNfe()
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
        Log.i("zyzz","Pesquisa:  ${imoinventario}")
        try {
            val nfeService = InfraHelper.apiInventario.create( NfeService::class.java )
            binding.llProgress75.visibility = View.VISIBLE
            nfeService.getNfebyimobilizado(
                imoinventario.id_empresa,
                imoinventario.id_filial,
                imoinventario.id_imobilizado,
                imoinventario.imo_nfe,
                imoinventario.imo_serie,
                imoinventario.imo_item).enqueue(object :  Callback<List<NfeModel>>{
                override fun onResponse(
                    call: Call<List<NfeModel>>,
                    response: Response<List<NfeModel>>
                ) {
                    binding.llProgress75.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var nfes = response.body()!!

                            if (nfes.size >  0){
                                binding.llNfe75.visibility = View.VISIBLE
                                nfe = nfes[0]
                                loadNfe(nfe)
                            } else {
                                binding.llNfe75.visibility = View.GONE
                                showToast("NFE Não Encontrada!")
                            }
                        }
                        else {
                            binding.llProgress75.visibility = View.GONE
                            try {
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    showToast("NFE Não Encontrada!")
                                } else {
                                    showToast(message.getMessage().toString())
                                }
                            } catch (error:Exception){
                                showToast("NFE Não Encontrada!")
                            }
                            binding.llNfe75.visibility = View.GONE
                        }
                    } else {
                        binding.llProgress75.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<NfeModel>>, t: Throwable) {
                    binding.llProgress75.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })
        }catch (e: Exception){
            binding.llProgress75.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }
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
                        try {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast("VALORES Não Encontrados!")
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        } catch (error:Exception){
                            showToast("VALORES Não Encontrados!")
                        }
                        binding.llProgress75.visibility = View.GONE
                    }
                } else {
                    binding.llProgress75.visibility = View.GONE
                    Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<ValorModel>, t: Throwable) {
                binding.llProgress75.visibility = View.GONE
                showToast(t.message.toString())
            }
        })

    }catch (e: Exception){
        binding.llProgress75.visibility = View.GONE
        showToast("${e.message.toString()}",Toast.LENGTH_LONG)
    }
    }

    private fun loadNfe(nfe : NfeModel){
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.setMaximumFractionDigits(2)
        format.setCurrency(Currency.getInstance("BRL"))


        binding.editnronfe75.setText(ParametroGlobal.prettyText.tituloDescricaotres(
            "Nro NFe: ",
            nfe.nfe,
            "Série: ",
            nfe.serie,
            "Item: ",
            nfe.serie
        ))

        binding.editItem75.setText(ParametroGlobal.prettyText.tituloDescricao(
            "Chave Eletronica.: ",
            nfe.chavee
        ))

        binding.editDatas75.setText(ParametroGlobal.prettyText.tituloDescricaoDois(
            "Emissão: ",
            nfe.dtemissao,
            "Lançamento: ",
            nfe.dtlancamento
        ))

        binding.editValoresItens75.setText(ParametroGlobal.prettyText.tituloDescricaotres(
            "Qtd: ",
            format.format(nfe.qtd).substring(3),
            "P.Unit.: ",
            format.format(nfe.punit),
            "Vlr Cont.",
            format.format(nfe.vlrcontabil),
            true
        ))

        binding.editValorContabil75.setText(ParametroGlobal.prettyText.tituloDescricao(
            "Valor Contabil: ",
            format.format(nfe.vlrcontabil),
        ))

        binding.editValoresIcms75.setText(ParametroGlobal.prettyText.tituloDescricaotres(
            "Base Icms: ",
            format.format(nfe.baseicms),
            "Perc: ",
            format.format(nfe.percicms),
            "Vlr Icms: ",
            format.format(nfe.vlrcicms),
            true
        ))


    }

    private fun loadValor(valor:ValorModel){

        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.setMaximumFractionDigits(2)
        format.setCurrency(Currency.getInstance("BRL"))

        binding.editValores0175.setText(ParametroGlobal.prettyText.tituloDescricaoDois(
          "Aquisição: ",
          valor.dtaquisicao,
          "Vlr Aquis.: ",
            format.format(valor.vlraquisicao)))

        binding.editValores0275.setText(ParametroGlobal.prettyText.tituloDescricaoDois(
            "Tot. Depreciado: ",
            format.format(valor.totaldepreciado),
            "Vlr Residual: ",
            format.format(valor.vlrresidual)))

        binding.editValores0375.setText(ParametroGlobal.prettyText.tituloDescricao(
            "Reavaliação: ",
            format.format(valor.reavalicao)
            ))

        binding.editValores0475.setText(ParametroGlobal.prettyText.tituloDescricaoDois(
            "DEEMED: ",
            format.format(valor.deemed),
            "Consolidado: ",
            format.format(valor.vlrconsolidado)
        ))
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}