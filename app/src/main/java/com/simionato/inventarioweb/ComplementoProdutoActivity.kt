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
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityComplementoProdutoBinding
import com.simionato.inventarioweb.databinding.ActivityFotoWebBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComplementoProdutoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityComplementoProdutoBinding.inflate(layoutInflater)
    }


    private var params: ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()
    
    private var id_imobilizado:Int = 0;

    private var fromLacamento:String = "N"

    private var imobilizadoInventario : ImobilizadoinventarioModel = ImobilizadoinventarioModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.llProgress38.visibility = View.GONE

        id_imobilizado =  intent.getIntExtra("id_imobilizado",0)

        fromLacamento  =  intent.getStringExtra("fromLancamento")!!

        if ( id_imobilizado == 0){
            showToast("Não Foi Informado O Código Do Imobilizado!");
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
            return
        }

        if (fromLacamento == null){
            fromLacamento = "N"
        }
        getImoInventario()
    }

    private fun iniciar(){

        inicializarTooBar()

        if (fromLacamento == "S"){
            binding.btInventarioComplemento38.visibility = View.GONE
            binding.textComplemento38.setText("COMPLEMENTO DE LANÇAMENTO DE INVENTÁRIO")
        } else {
            binding.btInventarioComplemento38.visibility = View.VISIBLE
            binding.textComplemento38.setText("COMPLEMENTO DE CADASTRO DE PRODUTO")
        }

        binding.btFotoComplemento38.setOnClickListener {
            chamaShowFotos()
        }

        binding.btInventarioComplemento38.setOnClickListener {
            chamaLancamento()
        }
    }

    private fun inicializarTooBar(){
        binding.ToolBar38.title = "Controle De Ativos"
        binding.ToolBar38.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar38.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar38.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar38.inflateMenu(R.menu.menu_login)
        binding.ToolBar38.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_OK,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }

    }

    private val getRetornoLancamento =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if ((it.resultCode == Activity.RESULT_OK) && (it.data?.extras != null)) {
                try {
                   //desprezo o retorno
                } catch (error: Exception) {
                    showToast("Erro No Retorno: ${error.message}")
                    finish()
                }
            }
        }

    private fun chamaLancamento() {
        val intent = Intent(this, LancamentoActivity::class.java)
        intent.putExtra("id_imobilizado", imobilizadoInventario.id_imobilizado)
        intent.putExtra("descricao", imobilizadoInventario.imo_descricao)
        intent.putExtra("idx", 0)
        getRetornoLancamento.launch(intent)
    }

    private fun chamaShowFotos() {
        val intent = Intent(this, ShowFotosActivity::class.java)
        intent.putExtra("ImoInventario", imobilizadoInventario)
        getRetornoShowFotos.launch(intent)
    }

    private val getRetornoShowFotos =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
        }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun getImoInventario(){

        params.id_imobilizado = id_imobilizado
       
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create( ImobilizadoInventarioService::class.java )
            binding.llProgress38.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :
                Callback<List<ImobilizadoinventarioModel>> {
                override fun onResponse(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    response: Response<List<ImobilizadoinventarioModel>>
                ) {
                    binding.llProgress38.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var inventarios = response.body()

                            imobilizadoInventario = (inventarios?.get(0) ?: ImobilizadoinventarioModel())

                            if (imobilizadoInventario.id_empresa != 0){
                                iniciar()
                                return
                            }
                            val returnIntent: Intent = Intent()
                            setResult(Activity.RESULT_OK,returnIntent)
                            finish()
                        }
                        else {
                            binding.llProgress38.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Ativo Não Encontrado!")
                            } else {
                                showToast(message.getMessage().toString())
                            }
                            val returnIntent: Intent = Intent()
                            setResult(Activity.RESULT_OK,returnIntent)
                            finish()
                        }
                    } else {
                        binding.llProgress38.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                        val returnIntent: Intent = Intent()
                        setResult(Activity.RESULT_OK,returnIntent)
                        finish()
                    }
                }
                override fun onFailure(call: Call<List<ImobilizadoinventarioModel>>, t: Throwable) {
                    binding.llProgress38.visibility = View.GONE
                    showToast(t.message.toString())
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_OK,returnIntent)
                    finish()
                }
            })

        }catch (e: Exception){
            binding.llProgress38.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_OK,returnIntent)
            finish()
        }

    }
    

}