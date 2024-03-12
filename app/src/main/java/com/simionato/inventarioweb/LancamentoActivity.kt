package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.databinding.ActivityLancamentoBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.LancamentoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


class LancamentoActivity : AppCompatActivity() {


    private var params:ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()

    private var ReadOnly = true

    private var id_imobilizado:Int = 0

    private var descricao:String = ""

    private var idx:Int = 0;

    private var requestCamara: ActivityResultLauncher<String>? = null
    private lateinit var inventario: ImobilizadoinventarioModel
    private val binding by lazy {
        ActivityLancamentoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Validando Paramentros
        if (ParametroGlobal.Ambiente.itsOK()){
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }

        try {
            val bundle = intent.extras

            if (bundle != null) {
                id_imobilizado = bundle.getInt("id_imobilizado", 0)
                descricao = bundle.getString("descricao")!!
                idx = bundle.getInt("idx",0)
            } else {
                showToast("Parâmetro Foto Incorreto!!")
                finish()
            }
        }  catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }

        if (ReadOnly){
            binding.txtViewSituacao02.setVisibility(View.GONE)
        }

        requestCamara = registerForActivityResult(ActivityResultContracts.RequestPermission(),){
            if (it){
                val intent = Intent(this,ScanBarCodeActivity::class.java)
                getResult.launch(intent)
            } else {
                Toast.makeText(this,"Permissão Negada",Toast.LENGTH_SHORT).show()
            }
        }

        binding.llProgress02.visibility = View.GONE

        if (id_imobilizado != 0){
            getInventario(id_imobilizado)
            inicializar()
        } else {
            clearInventario()
            formulario(false)
            inicializar()
        }


    }

    private fun inicializar(){

        inicializarTooBar()

        binding.imSearch01.setOnClickListener{

            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            // on below line hiding our keyboard.
            inputMethodManager.hideSoftInputFromWindow(binding.editCodigo01.getWindowToken(), 0)

            try {
                val codigo = binding.editCodigo01.text.toString().toInt()
                getInventario(codigo)
            } catch ( e : NumberFormatException ){
                Toast.makeText(it.context,"Código Inválido!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.editObs02.filters += InputFilter.AllCaps()

        binding.editCCNovol02.setOnClickListener {
            chamaPesquisaCc()
        }

        binding.ibLimparCcNew02.setOnClickListener {
            inventario.new_cc = ""
            inventario.new_cc_descricao = ""
            binding.editCCNovol02.setText(R.string.sem_cc_novo)
        }
        binding.btExcluir02.setOnClickListener{
            val lancamento:LancamentoModel = loadLancamento()
            deleteApontamento(lancamento)
        }

        binding.btCancelar02.setOnClickListener {
            if (id_imobilizado != 0) {
                finish()
            } else {
                clearInventario()
                binding.editCodigo01.setText("")
                formulario(false)
            }
        }

        binding.btGravar02.setOnClickListener{
            val lancamento:LancamentoModel = loadLancamento()
            if (lancamento.id_lanca == 0){
                saveApontamento(lancamento)
            } else {
                updateApontamento(lancamento)
            }
        }
    }

    private val getRetornoPequisaCc =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK && it.data != null){
                if(it.resultCode == Activity.RESULT_OK){
                    val codigo = it.data?.getStringExtra("codigo")
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo?.trim() != ""){
                            inventario.new_cc = codigo ?: ""
                            inventario.new_cc_descricao = descricao ?: ""
                            binding.editCCNovol02.setText(descricao)
                        }
                    } catch ( e : NumberFormatException ){
                        showToast("Código Inválido!",Toast.LENGTH_SHORT)
                    }
                }
            }
                if(it.resultCode == 100){
                    inventario.new_cc = ""
                    inventario.new_cc_descricao = ""
                    binding.editCCNovol02.setText(R.string.sem_cc_novo)

                }

        }

    private fun chamaPesquisaCc(){
        val intent = Intent(this,PesquisaCcActivity::class.java)
        getRetornoPequisaCc.launch(intent)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val value = it.data?.getStringExtra("codigo")
                try {
                    if (value != null){
                       val codigo = value.toInt()
                       getInventario(codigo)
                    } else {
                        Toast.makeText(this,"Código Retornado Inválido!",Toast.LENGTH_SHORT).show()
                    }
                } catch ( e : NumberFormatException ){
                    Toast.makeText(this,"Código Inválido!",Toast.LENGTH_SHORT).show()
                }
                binding.editCodigo01.setText(value?.padStart(6,'0'))
            } else {
                binding.editCodigo01.setText("")
            }
        }
    fun getHoje():String{

        try {

            val date = Date()

            val format = SimpleDateFormat("dd/MM/yyyy")

            val data = format.format(date)

            return data

        } catch (e:Exception)
        {
            return ""
        }

    }

    private fun inicializarTooBar(){
        binding.ToolBar02.title = "Controle De Ativos"
        binding.ToolBar02.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar02.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar02.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar02.inflateMenu(R.menu.menu_voltar)
        binding.ToolBar02.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_barcode -> {
                    requestCamara?.launch(android.Manifest.permission.CAMERA)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private fun getInventario(cod:Int){
        params.id_imobilizado = 0
        params.new_codigo = 0
        if (binding.rbAntigo02.isChecked){
            params.id_imobilizado = cod
        } else {
            params.new_codigo = cod
        }
        try {
            val imobilizadoInventarioService = InfraHelper.apiInventario.create( ImobilizadoInventarioService::class.java )
            binding.llProgress02.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :Callback<List<ImobilizadoinventarioModel>>{
                override fun onResponse(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    response: Response<List<ImobilizadoinventarioModel>>
                ) {
                    binding.llProgress02.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var inventarios = response.body()

                            inventario = (inventarios?.get(0) ?: ImobilizadoinventarioModel(
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                "",
                                0,
                                0,
                                "",
                                "",
                                0,
                                "",
                                "",
                                "",
                                "",
                                "",
                                0,
                                "",
                                "",
                                0,
                                "",
                                ""
                            )) as ImobilizadoinventarioModel

                            if (inventario.id_lanca !== 0) {
                                showToast( "Ativo Já Inventariado!",
                                    Toast.LENGTH_SHORT)
                            }

                            with(binding) {
                                formulario(true)
                                if (inventario.status == 5){
                                    binding.cbSituacao.isChecked = true
                                } else {
                                    binding.cbSituacao.isChecked = false
                                }

                                when (inventario.status) {
                                    0 -> {binding.txtViewSituac02.setTextColor(ContextCompat.getColor(applicationContext, R.color.corVermelho))}
                                    1 -> {binding.txtViewSituac02.setTextColor(ContextCompat.getColor(applicationContext, R.color.corVerde))}
                                    in 2..4 -> {binding.txtViewSituac02.setTextColor(ContextCompat.getColor(applicationContext, R.color.corAmarelo))}
                                    5 -> {binding.txtViewSituac02.setTextColor(ContextCompat.getColor(applicationContext, R.color.secondary))}
                                    else -> {binding.txtViewSituac02.setTextColor(ContextCompat.getColor(applicationContext, R.color.corVermelho))}
                                }
                                binding.txtViewSituac02.setText(
                                    ParametroGlobal.Situacoes.getSituacao(
                                        inventario.status
                                    )
                                )
                                binding.editNroLanc02.setText(
                                    if (inventario.id_lanca == 0) "" else inventario.id_lanca.toString()
                                )
                                binding.editData02.setText(inventario.lanc_dt_lanca)
                                binding.txtViewResp02.setText("RESP: ${inventario.usu_razao}")
                                binding.editCodigoAtual02.setText(inventario.id_imobilizado.toString().padStart(6,'0'))
                                binding.editCodigoNovo02.setText(
                                    if (inventario.new_codigo == 0) "" else inventario.new_codigo.toString()
                                )
                                binding.editDescricao02.setText(inventario.imo_descricao)
                                binding.editCCOriginal02.setText(inventario.cc_descricao)

                                if (inventario.new_cc_descricao == ""){
                                    binding.editCCNovol02.setText(R.string.sem_cc_novo)
                                } else {
                                    binding.editCCNovol02.setText("inventario.new_cc_descricao")
                                }
                            }

                        }
                        else {
                            binding.llProgress02.visibility = View.GONE
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
                        }
                    } else {
                        binding.llProgress02.visibility = View.GONE
                        Toast.makeText(applicationContext,"Sem retorno Da Requisição!",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<ImobilizadoinventarioModel>>, t: Throwable) {
                    binding.llProgress02.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress02.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }

    }
    private  fun saveApontamento(lancamento:LancamentoModel){
        try {
            binding.llProgress02.visibility = View.VISIBLE

            val lancamentoService = InfraHelper.apiInventario.create( LancamentoService::class.java )

            lancamentoService.insertLancamento(lancamento).enqueue(object :Callback<LancamentoModel>{
                override fun onResponse(
                    call: Call<LancamentoModel>,
                    response: Response<LancamentoModel>
                ) {
                    if ( response != null ) {
                        binding.llProgress02.visibility = View.GONE
                        if (response.isSuccessful) {

                            val lanca = response.body()

                            clearInventario()

                            binding.editCodigo01.setText("")

                            formulario(false)

                            Toast.makeText(
                                applicationContext,
                                "Ativo Inventariado Com Sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (id_imobilizado != 0){
                                if (lanca != null)  finalizaOK(lanca)
                            }
                        } else {
                            binding.llProgress02.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409){
                                    showToast("Ativo Não Encontrado!",Toast.LENGTH_SHORT)
                                } else {
                                    showToast(message.getMessage().toString(),Toast.LENGTH_SHORT)
                                }

                        }
                    } else {
                        binding.llProgress02.visibility = View.GONE
                        showToast("Falha Na Requisição!",Toast.LENGTH_LONG)
                    }
                }


                override fun onFailure(call: Call<LancamentoModel>, t: Throwable) {
                    binding.llProgress02.visibility = View.GONE
                    showToast(t.message.toString(),Toast.LENGTH_LONG)
                }

            })

        }catch (e: Exception){
            binding.llProgress02.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }
    }
    private fun updateApontamento(lancamento:LancamentoModel){
        binding.llProgress02.visibility = View.VISIBLE
        try {
            val lancamentoService = InfraHelper.apiInventario.create( LancamentoService::class.java )
            lancamentoService.editLancamento(lancamento).enqueue(object :Callback<LancamentoModel>{
                override fun onResponse(
                    call: Call<LancamentoModel>,
                    response: Response<LancamentoModel>
                ) {
                    binding.llProgress02.visibility = View.GONE
                    if ( response != null ) {

                        if (response.isSuccessful)
                        {
                            var lanca = response.body()

                            clearInventario()

                            binding.editCodigo01.setText("")

                            formulario(false)

                            Toast.makeText(
                                applicationContext,
                                "Ativo Alterado Com Sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (id_imobilizado != 0){
                                if (lanca != null)  finalizaOK(lanca)
                            }
                        } else
                        {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Ativo Não Encontrado!",Toast.LENGTH_SHORT)
                            } else {
                                showToast(message.getMessage().toString(),Toast.LENGTH_SHORT)
                            }
                        }
                    } else {
                        showToast("Falha Na Requisição!",Toast.LENGTH_LONG)
                    }
                }

                override fun onFailure(call: Call<LancamentoModel>, t: Throwable) {
                    binding.llProgress02.visibility = View.GONE
                    showToast(t.message.toString(),Toast.LENGTH_LONG)
                }

            })

        }catch (e: Exception){
            binding.llProgress02.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }


    }
    private fun deleteApontamento(lancamento:LancamentoModel){
        binding.llProgress02.visibility = View.VISIBLE
        try {
            val lancamentoService = InfraHelper.apiInventario.create( LancamentoService::class.java )
            lancamentoService.deleteLancamento(lancamento.id_empresa,lancamento.id_filial,lancamento.id_inventario,lancamento.id_imobilizado)
                .enqueue(object : Callback<JsonObject>{
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        binding.llProgress02.visibility = View.GONE
                        if ( response != null ) {

                            if (response.isSuccessful) {
                                var lanca = response.body()

                                clearInventario()

                                binding.editCodigo01.setText("")

                                formulario(false)

                                Toast.makeText(
                                    applicationContext,
                                    "Lançamento De Inventário, EXCLUÍDO Com Sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (id_imobilizado != 0){
                                    finalizaOK(lancamento)
                                }
                            } else {
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409){
                                    showToast("Ativo Não Encontrado!",Toast.LENGTH_SHORT)
                                } else {
                                    showToast(message.getMessage().toString(),Toast.LENGTH_SHORT)
                                }
                            }
                        }
                        else
                        {
                            showToast("Falha Na Requisição!",Toast.LENGTH_LONG)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        binding.llProgress02.visibility = View.GONE
                        showToast(t.message.toString(),Toast.LENGTH_LONG)
                    }
                })

        }catch (e: Exception){
            binding.llProgress02.visibility = View.GONE
            showToast("${e.message.toString()}",Toast.LENGTH_LONG)
        }
    }
    fun loadLancamento():LancamentoModel{
        try {
            val codigo = binding.editCodigoNovo02.text.toString().toInt()
            inventario.new_codigo = codigo
        } catch ( e : NumberFormatException ){
            inventario.new_codigo = 0
        }
        try {
            val codigo = binding.editNroLanc02.text.toString().toInt()
            inventario.id_lanca = codigo
        } catch ( e : NumberFormatException ){
            inventario.id_lanca = 0
        }
        inventario.lanc_estado = if (binding.cbSituacao.isChecked()) { 5 } else {0}
        inventario.lanc_dt_lanca = getHoje()
        inventario.lanc_obs = binding.editObs02.text.toString()
        val lancamento = LancamentoModel(
            inventario.id_empresa,
            inventario.id_filial,
            inventario.id_inventario,
            inventario.id_imobilizado,
            ParametroGlobal.Dados.usuario.id,
            inventario.id_lanca,
            inventario.lanc_obs,
            inventario.lanc_dt_lanca,
            inventario.lanc_estado,
            inventario.new_codigo,
            inventario.new_cc,
            if (inventario.id_lanca !== 0)   inventario.user_insert else ParametroGlobal.Dados.usuario.id,
            if (inventario.id_lanca !== 0)  ParametroGlobal.Dados.usuario.id else 0,
            0,
            "",
            "",
            0,
            "",
            ""
        )
        return lancamento
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
    private fun formulario(show:Boolean){
       if (show){
           binding.txtViewSituacao02.setText("lançamento de Inventário")
       }
       binding.txtViewSituacao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaSituacao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaData02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCodigoAtual02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCodigoNovo02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaDescricao02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCCNovol02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.llLinhaCCOriginal02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.txtInputObs02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       if (inventario.id_lanca == 0){
           binding.btExcluir02.visibility = View.GONE
           binding.btGravar02.text = "Inventariar"
       } else {
           binding.btExcluir02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
           binding.btGravar02.text = "Alterar"
       }
       binding.btCancelar02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       binding.btGravar02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
       pesquisa(!show)
   }
    private fun pesquisa(show:Boolean){
        binding.llCodigo02.visibility = if (!show) { View.GONE} else {View.VISIBLE}
    }
    private fun clearInventario() {
        inventario =ImobilizadoinventarioModel(0,
            0,
            0,
            0,
            0,
            0,
            0,
            "",
            0,
            0,
            "",
            "",
            0,
            "",
            "",
            "" ,
            "",
            "",
            0,
            "",
            "",
            0,
            "",
            "" )
    }

    private fun finalizaOK(lancamento: LancamentoModel){
        val returnIntent: Intent = Intent()
        returnIntent.putExtra("id_imobilizado",lancamento.id_imobilizado)
        returnIntent.putExtra("idx",idx)
        setResult(Activity.RESULT_OK,returnIntent)
        finish()
    }

}

