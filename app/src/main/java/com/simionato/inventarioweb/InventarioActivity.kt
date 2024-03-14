package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.adapters.ImoInventarioAdapter
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.PaginasAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.empresa
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.paramImoInventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.ParametroModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.ParametroService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InventarioActivity : AppCompatActivity() {

    private val adapter = ImoInventarioAdapter() { imoInventario, idAcao, idx ->
        if (idAcao == CadastrosAcoes.Lancamento) {
            chamaLancamento(imoInventario, idx)
        }
        if (idAcao == CadastrosAcoes.Foto) {
            chamaShowFotos(imoInventario)
        }
        if (idAcao == CadastrosAcoes.Consulta) {
            chamaShowNfe(imoInventario)
        }

    }

    private lateinit var imobilizadoinventarios: MutableList<ImobilizadoinventarioModel>

    private val binding by lazy {
        ActivityInventarioBinding.inflate(layoutInflater)
    }

    private var totalPaginas: Int = 1

    private var paginaAtual: Int = 0

    private var pesquisaString: String = ""

    private val tamPagina: Int = 50

    private var isLoading: Boolean = false

    private var first: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress40.visibility = View.GONE
        if (ParametroGlobal.Ambiente.itsOK()) {
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }

        inicializar()
    }


    private fun inicializar() {

        inicializarTooBar()

        binding.svPesquisa40.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                pesquisaString = if (p0 != null) p0 else ""

                getInventariosContador()

                binding.svPesquisa40.setQuery("", false)
                binding.svPesquisa40.clearFocus()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {



                return false
            }

        })
        //limpezaPesquisa()
        binding.rvLista40.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                var tam = adapter.itemCount
                if (tam <= 50){
                    tam = -1
                    binding.llProgress40.visibility = View.GONE
                }
                if (!isLoading) {
                    if (adapter.getLastEmpresa() == 0 && linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == tam - 1) {
                        Log.i("zyzz","Pagina AtuL ${paginaAtual}")
                        atualizaPagina(PaginasAcoes.Avancar)
                        getInventarios(false)
                    }
                }
            }

        })

        getParametro()

    }


    private fun inicializarTooBar() {
        binding.ToolBar40.title = "Controle De Ativos"
        binding.ToolBar40.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar40.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar40.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar40.inflateMenu(R.menu.menu_inventario)
        binding.ToolBar40.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_inventario_sair -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_inventario_filtro -> {
                    chamaFiltro()
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun getParametro() {
        try {
            val parametroService = InfraHelper.apiInventario.create(ParametroService::class.java)
            binding.llProgress40.visibility = View.VISIBLE
            parametroService.getParametro(
                empresa.id,
                "inventariomobile",
                "V1.00 29/02/24",
                usuario.id
            )
                .enqueue(object : Callback<ParametroModel> {
                    override fun onResponse(
                        call: Call<ParametroModel>,
                        response: Response<ParametroModel>
                    ) {
                        binding.llProgress40.visibility = View.GONE
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
                                binding.llProgress40.visibility = View.GONE
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
                            getInventariosContador()
                        } else {
                            binding.llProgress40.visibility = View.GONE
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
                            getInventariosContador()
                        }
                    }

                    override fun onFailure(call: Call<ParametroModel>, t: Throwable) {
                        binding.llProgress40.visibility = View.GONE
                        showToast(t.message.toString())
                        getInventariosContador()
                    }
                })

        } catch (e: Exception) {
            binding.llProgress40.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            getInventariosContador()
        }

    }

    private fun getInventarios(unico: Boolean, id: Int = 0, idx: Int = 0) {
        var params: ParametroImobilizadoInventario01 = paramImoInventario

        if (unico) {
            params = ParametroImobilizadoInventario01()
            params.id_imobilizado = id
            params.pagina = 0
            params.tamPagina = tamPagina
        } else {
            if (totalPaginas == 0) {
                imobilizadoinventarios = mutableListOf<ImobilizadoinventarioModel>()
                montaLista(imobilizadoinventarios)
                isLoading = false
                return
            } else {
                params.pagina = paginaAtual
                params.tamPagina = tamPagina
                params.contador = "N"
            }
        }
        Log.i("zyzz", "Buscando pagina ${paramImoInventario.pagina}")
        try {
            val imobilizadoInventarioService =
                InfraHelper.apiInventario.create(ImobilizadoInventarioService::class.java)
            if (!unico && paginaAtual == 1) {
                binding.textViewProgress40.setText("Carregando Página ${paginaAtual}/${totalPaginas}")
                if (unico) binding.textViewProgress40.setText("Atualizando Registro")
                else binding.textViewProgress40.setText("Busncado Página ${paginaAtual}/${totalPaginas}")
                binding.llProgress40.visibility = View.VISIBLE
            }
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :
                Callback<List<ImobilizadoinventarioModel>> {
                override fun onResponse(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    response: Response<List<ImobilizadoinventarioModel>>
                ) {
                    if (!unico && paginaAtual == 1) binding.llProgress40.visibility = View.GONE
                    isLoading = false
                    if (response != null) {
                        if (response.isSuccessful) {
                            if (unico) {
                                var imos = response.body()!!
                                if (imos != null) {
                                    adapter.updateData(imos[0], idx)
                                }
                            } else {

                                val imobilizadoinventarios = response.body()!!.toMutableList()

                                Log.i(
                                    "zyzz",
                                    "paginaAtual ${paginaAtual} ${totalPaginas}  ${imobilizadoinventarios.size}"
                                )
                                if (imobilizadoinventarios !== null) {
                                    Log.i("zyzz","totalPaginas ${totalPaginas} Pagina Atual ${paginaAtual} ${params}")
                                    if ((totalPaginas > 1) && (paginaAtual < totalPaginas)) {
                                        var fimConsulta: ImobilizadoinventarioModel =
                                            ImobilizadoinventarioModel()
                                        imobilizadoinventarios.add(fimConsulta)
                                    }
                                    montaLista(imobilizadoinventarios);
                                } else {
                                    showToast("Falha No Retorno Da Requisição!")
                                }
                            }
                        } else {
                            if (!unico && paginaAtual == 1) binding.llProgress40.visibility =
                                View.GONE
                            isLoading = false
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast("QUERY Tabela De Imobilizados Vazia")
                                imobilizadoinventarios = mutableListOf<ImobilizadoinventarioModel>()
                                paginaAtual = 1
                                montaLista(imobilizadoinventarios)
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        }
                    } else {
                        if (!unico && paginaAtual == 1) binding.llProgress40.visibility = View.GONE
                        isLoading = false
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    t: Throwable
                ) {
                    if (!unico && paginaAtual == 1) binding.llProgress40.visibility = View.GONE
                    isLoading = false
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            if (!unico && paginaAtual == 1) binding.llProgress40.visibility = View.GONE
            isLoading = false
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun getInventariosContador() {


        var params: ParametroImobilizadoInventario01 = paramImoInventario

        params.new_codigo = 0
        params.id_imobilizado = 0
        params.descricao = ""
        when (paramImoInventario._searchIndex) {
            0 -> {
                if (pesquisaString.isEmpty()) {
                    params.id_imobilizado = 0
                } else {
                    try {
                        var chave = pesquisaString.toInt()
                        params.id_imobilizado = chave
                    } catch (e: NumberFormatException) {
                        params.id_imobilizado = 0
                    }
                }
            }

            1 -> {
                if (pesquisaString.isEmpty()) {
                    params.new_codigo = 0
                } else {
                    try {
                        var chave = pesquisaString.toInt()
                        params.new_codigo = chave
                    } catch (e: NumberFormatException) {
                        params.new_codigo = 0
                    }
                }
            }

            2 -> {
                params.descricao = pesquisaString.trim()
            }
        }
        pesquisaString = ""
        params.contador = "S"

        try {
            val imobilizadoInventarioService =
                InfraHelper.apiInventario.create(ImobilizadoInventarioService::class.java)
            binding.textViewProgress40.setText("Recuperando Total De Registros")
            binding.llProgress40.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventariosContador(params)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        binding.llProgress40.visibility = View.GONE
                        if (response != null) {
                            if (response.isSuccessful) {

                                val res = response.body()!!

                                if (res !== null) {

                                    try {
                                        var contador =
                                            res?.get("total").toString().replace("\"", "").toInt()
                                        Log.i(
                                            "zyzz",
                                            "Total de Registros: ${contador} tamPagina ${tamPagina} Total Paginas ${contador / tamPagina} Resto: ${
                                                contador.rem(tamPagina)
                                            }"
                                        )
                                        if (contador <= tamPagina) {
                                            totalPaginas = 1
                                        } else {
                                            totalPaginas = contador / tamPagina
                                            val resto = contador.rem(tamPagina)
                                            totalPaginas =
                                                if (resto == 0) totalPaginas else totalPaginas + 1
                                        }
                                        paginaAtual = 1
                                        Log.i("zyzz","contador ${contador} paginaAtual ${paginaAtual} totalPaginas ${totalPaginas} ")
                                        getInventarios(false)
                                    } catch (e: NumberFormatException) {
                                        totalPaginas = 0
                                        paginaAtual = 1
                                        getInventarios(false)
                                    }
                                } else {
                                    showToast("Falha No Retorno Da Requisição!")
                                    totalPaginas = 0
                                    paginaAtual = 1
                                }

                            } else {
                                binding.llProgress40.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    showToast(" CONTADOR Tabela Vazia")
                                    totalPaginas = 0
                                    paginaAtual = 1
                                } else {
                                    showToast(message.getMessage().toString())
                                    totalPaginas = 0
                                    paginaAtual = 1
                                }
                            }
                        } else {
                            binding.llProgress40.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                            totalPaginas = 0
                            paginaAtual = 1
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        binding.llProgress40.visibility = View.GONE
                        showToast(t.message.toString())
                        totalPaginas = 0
                    }
                })

        } catch (e: Exception) {
            binding.llProgress40.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun montaLista(imobilizados: MutableList<ImobilizadoinventarioModel>) {
        adapter.setTotalPaginas(totalPaginas)
        adapter.setPaginaAtual(paginaAtual)
        if (paginaAtual == 1) {
                adapter.loadData(imobilizados)
            binding.rvLista40.adapter = adapter
            binding.rvLista40.layoutManager =
                LinearLayoutManager(binding.rvLista40.context)
            binding.rvLista40.addItemDecoration(
                DividerItemDecoration(
                    binding.rvLista40.context,
                    RecyclerView.VERTICAL
                )
            )
        }
        else {
            adapter.anexarData(imobilizados)
        }
    }

    private val getRetornoLancamento =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if ((it.resultCode == Activity.RESULT_OK) && (it.data?.extras != null)) {
                try {
                    var bundle = it.data!!.extras
                    var id_imobilizado = bundle?.getInt("id_imobilizado", 0)
                    var idx = bundle?.getInt("idx", 0)
                    getInventarios(true, id_imobilizado!!, idx!!)
                } catch (error: Exception) {
                    showToast("Erro No Retorno: ${error.message}")
                    finish()
                }
            }
        }

    private fun chamaLancamento(imobilizadoInventario: ImobilizadoinventarioModel, idx: Int) {
        val intent = Intent(this, LancamentoActivity::class.java)
        intent.putExtra("id_imobilizado", imobilizadoInventario.id_imobilizado)
        intent.putExtra("descricao", imobilizadoInventario.imo_descricao)
        intent.putExtra("idx", idx)
        getRetornoLancamento.launch(intent)
    }

    private fun chamaShowFotos(imoInventario: ImobilizadoinventarioModel) {
        val intent = Intent(this, ShowFotosActivity::class.java)
        intent.putExtra("ImoInventario", imoInventario)
        getRetornoShowFotos.launch(intent)
    }

    private val getRetornoShowFotos =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
            }
        }

    private fun chamaShowNfe(imoInventario: ImobilizadoinventarioModel) {
        val intent = Intent(this, ShowNfeValoresActivity::class.java)
        intent.putExtra("ImoInventario", imoInventario)
        getRetornoShowNfe.launch(intent)
    }

    private val getRetornoShowNfe =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
            }
        }

    private fun chamaFiltro() {
        val intent = Intent(this, FiltroInventarioActivity::class.java)
        getRetornoChamaFiltro.launch(intent)
    }

    private val getRetornoChamaFiltro =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.svPesquisa40.setQuery("", false)
                binding.svPesquisa40.clearFocus()
                paramImoInventario.new_codigo = 0
                paramImoInventario.id_imobilizado = 0
                paramImoInventario.descricao = ""

                when (paramImoInventario._searchIndex) {
                    0 -> {
                        binding.svPesquisa40.queryHint = "Busca Por Código Atual"
                        binding.svPesquisa40.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
                    }

                    1 -> {
                        binding.svPesquisa40.queryHint = "Busca Por Código Novo"
                        binding.svPesquisa40.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
                    }

                    2 -> {
                        binding.svPesquisa40.queryHint = "Busca Pela Descrição"
                        binding.svPesquisa40.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                    }

                    else -> {
                        binding.svPesquisa40.queryHint = "Busca Por Código Atual"
                        binding.svPesquisa40.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
                    }
                }

                getInventariosContador()
            }
        }

    fun atualizaPagina(idAcao: PaginasAcoes) {
        when (idAcao) {
            PaginasAcoes.Inicio -> paginaAtual = 1
            PaginasAcoes.Retroceder -> {
                paginaAtual--
                if (paginaAtual < 1) {
                    paginaAtual = 1
                }
            }

            PaginasAcoes.Avancar -> {
                paginaAtual++
                if (paginaAtual > totalPaginas) {
                    paginaAtual = totalPaginas
                }
            }

            PaginasAcoes.ExclFinalusao -> paginaAtual = totalPaginas
        }
    }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }
}

