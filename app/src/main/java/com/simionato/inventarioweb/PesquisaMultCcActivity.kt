package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.CentroAdapter
import com.simionato.inventarioweb.adapters.CentroMultiAdapter
import com.simionato.inventarioweb.databinding.ActivityPesquisaCcBinding
import com.simionato.inventarioweb.databinding.ActivityPesquisaMultCcBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.empresa
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.paramImoInventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.models.CentroCustoMultiModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.ParametroModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.CentroCustoService
import com.simionato.inventarioweb.services.ParametroService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.internal.notifyAll
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PesquisaMultCcActivity : AppCompatActivity() {

    val params : ParametroCentroCusto01 = ParametroCentroCusto01()

    private var ccs = listOf<CentroCustoModel>()

    private var ccsMuli = mutableListOf<CentroCustoMultiModel>()

    private val binding by lazy {
        ActivityPesquisaMultCcBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: CentroMultiAdapter

    private var filtrosCCs: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pesquisa_mult_cc)

        if (ParametroGlobal.Ambiente.itsOK()) {
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }

        try {
            val bundle = intent.extras
            if (bundle != null) {
                filtrosCCs = if (Build.VERSION.SDK_INT >= 33) bundle.getString(
                    "filtrosCCs",
                    ""
                )
                else bundle.getString("filtrosCCs","")
            } else {
                showToast("Parâmetro Do Filtro De Inventário Está Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(binding.root)
        iniciar()

    }

    private fun iniciar(){
        inicializarTooBar()

        binding.svPesquisa200
            .setOnSearchClickListener {
                val closeButton = binding.svPesquisa200.findViewById<ImageView>(
                    androidx.appcompat.R.id.search_close_btn
                )

            closeButton.visibility = View.VISIBLE
        }
        getCcs()

        binding.swMarcarTudo200.setOnClickListener() {

            if (!(adapter == null)) {

                 binding.swMarcados200.isChecked = binding.swMarcarTudo200.isChecked

                 adapter.setMarcacao(binding.swMarcarTudo200.isChecked)
            }
        }

        binding.swMarcados200.setOnClickListener() {
            if (binding.swMarcados200.isChecked) {
                binding.swMarcarTudo200.isChecked = true
                adapter.filterCheck.filter("T")
            } else {
                binding.svPesquisa200.setQuery("", false)
                binding.svPesquisa200.clearFocus()
                binding.swMarcarTudo200.isChecked = false
                adapter.filterCheck.filter("")
            }
        }
    }
    private fun inicializarTooBar(){
        binding.ToolBar200.title = "Controle De Ativos"
        binding.ToolBar200.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar200.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar200.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar200.inflateMenu(R.menu.menu_pesquisamultipla)
        binding.ToolBar200.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_pesquisa_multipla_sair-> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_pesquisa_multipla_ok -> {
                    val listaCodigos = ccsMuli.filter { it.check }
                    if (listaCodigos.count() == 0){
                        showToast("Escolha Pelo Menos Um Centro De Custo")
                        return@setOnMenuItemClickListener true
                    }
                    val codigo = if (listaCodigos.count() == 1) { listaCodigos.get(0).centro_custo.codigo} else listaCodigos.joinToString(";") { it.centro_custo.codigo}

                    val returnIntent = Intent()
                    returnIntent.putExtra("codigo", codigo)
                    returnIntent.putExtra("descricao", if (listaCodigos.count() == 1) { listaCodigos.get(0).centro_custo.descricao} else "Multiplos Centros De Custos")
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private fun getCcs(){
        try {
            val centroCustoService = InfraHelper.apiInventario.create( CentroCustoService::class.java )
            params.id_empresa = ParametroGlobal.Dados.empresa.id
            params.id_filial = ParametroGlobal.Dados.local.id

            binding.llProgress200.visibility = View.VISIBLE
            centroCustoService.getCentrosCustos(params).enqueue(object :
                Callback<List<CentroCustoModel>> {
                override fun onResponse(
                    call: Call<List<CentroCustoModel>>,
                    response: Response<List<CentroCustoModel>>
                ) {
                    binding.llProgress200.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var ccsResp = response.body()

                            if (ccsResp !== null) {

                                var checked = false

                                ccs = ccsResp

                                ccs.forEach{cc -> ccsMuli.add(CentroCustoMultiModel(isChecked(cc),cc))}

                                adapter = CentroMultiAdapter(ccsMuli, { centro ->
                                    val returnIntent = Intent()
                                    returnIntent.putExtra("codigo", centro.centro_custo.codigo)
                                    returnIntent.putExtra("descricao", centro.centro_custo.descricao)
                                    setResult(Activity.RESULT_OK, returnIntent)
                                    finish()
                                })
                                binding.rvLista200.adapter = adapter
                                binding.rvLista200.layoutManager =
                                    LinearLayoutManager(binding.rvLista200.context)
                                binding.rvLista200.addItemDecoration(
                                    DividerItemDecoration(
                                        binding.rvLista200.context,
                                        RecyclerView.VERTICAL
                                    )
                                )
                                val tudoMarcado = adapter.EstaTudoMarcado()
                                binding.swMarcarTudo200.isChecked = tudoMarcado
                                binding.swMarcados200.isChecked = tudoMarcado
                                binding.svPesquisa200.setOnQueryTextListener(object :
                                    SearchView.OnQueryTextListener,
                                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
                                    override fun onQueryTextSubmit(p0: String?): Boolean {
                                        adapter.setMarcacao(false)
                                        binding.swMarcarTudo200.setChecked(false)
                                        binding.swMarcados200.setChecked(false)
                                        return false
                                    }

                                    override fun onQueryTextChange(newText: String?): Boolean {
                                        adapter.setMarcacao(false)
                                        binding.swMarcarTudo200.setChecked(false)
                                        binding.swMarcados200.setChecked(false)
                                        adapter.filter.filter(newText)

                                        return false
                                    }

                                })
                            } else {
                                showToast("Falha No Retorno Da Requisição!")
                            }

                        }
                        else {
                            binding.llProgress200.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            ccs = listOf()
                            ccsMuli = mutableListOf()
                            if (response.code() == 409){
                                showToast("Tabela De Centro De Custos Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress200.visibility = View.GONE
                        ccs = listOf()
                        ccsMuli = mutableListOf()

                    }
                }

                override fun onFailure(call: Call<List<CentroCustoModel>>, t: Throwable) {
                    binding.llProgress200.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress200.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
/*
    fun getParametro() {

        try {
            val parametroService = InfraHelper.apiInventario.create(ParametroService::class.java)
            binding.llProgress200.visibility = View.VISIBLE
            parametroService.getParametro(
                empresa.id,
                "inventariomobile",
                ParametroGlobal.Dados.parametro_assinatura,
                usuario.id
            )
                .enqueue(object : Callback<ParametroModel> {
                    override fun onResponse(
                        call: Call<ParametroModel>,
                        response: Response<ParametroModel>
                    ) {
                        binding.llProgress200.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                val parametro = response.body()

                                if (parametro !== null) {

                                    val gson = Gson()

                                    val par = gson.fromJson(
                                        parametro.parametro,
                                        ParametroImobilizadoInventario01::class.java
                                    )

                                    paramImoInventario = par

                                } else {
                                    paramImoInventario = ParametroImobilizadoInventario01()

                                }
                            } else {
                                binding.llProgress200.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    paramImoInventario = ParametroImobilizadoInventario01()
                                } else {
                                    showToast(message.getMessage().toString())
                                }

                            }
                            getCcs()
                        } else {
                            binding.llProgress200.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                paramImoInventario = ParametroImobilizadoInventario01()
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<ParametroModel>, t: Throwable) {
                        binding.llProgress200.visibility = View.GONE
                        showToast(t.message.toString())
                    }
                })

        } catch (e: Exception) {
            binding.llProgress200.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
*/
    fun isChecked(cc:CentroCustoModel):Boolean{
        val codigos = filtrosCCs
        ?.split(";")                       // Divide pelos separadores
        ?.map { it.trim() }               // Remove espaços extras de cada código
        ?: emptyList()                    // Garante uma lista vazia se for null

        if (codigos.contains(cc.codigo.trim())) {
            return true
        } else {
            return false
        }
    }
}