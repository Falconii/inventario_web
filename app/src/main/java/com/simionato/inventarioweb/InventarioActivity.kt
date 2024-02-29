package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.adapters.ImoInventarioAdapter
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
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

    }

    private lateinit var imobilizadoinventarios: MutableList<ImobilizadoinventarioModel>

    private val binding by lazy {
        ActivityInventarioBinding.inflate(layoutInflater)
    }

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
            parametroService.getParametro(empresa.id, "inventariomobile", "V1.00 29/02/24", usuario.id)
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

                            getInventarios(false)
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

                            getInventarios(false)
                        }
                    }
                    override fun onFailure(call: Call<ParametroModel>, t: Throwable) {
                        binding.llProgress40.visibility = View.GONE
                        showToast(t.message.toString())
                        getInventarios(false)
                    }
                })

        }
        catch (e: Exception) {
            binding.llProgress40.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)

            getInventarios(false)
        }

    }

    private fun getInventarios(unico: Boolean, id: Int = 0, idx: Int = 0) {
        var params: ParametroImobilizadoInventario01 = paramImoInventario

        when (paramImoInventario._searchIndex) {
            0 -> {
                binding.svPesquisa40.queryHint = "Busca Por Código Atual"
            }
            1 -> {binding.svPesquisa40.queryHint = "Busca Por Código Novo"}
            2 -> {binding.svPesquisa40.queryHint = "Busca Pela Descrição"}
            else -> {
                binding.svPesquisa40.queryHint = "Busca Por Código Atual"
            }
        }



        if (unico) {
            params = ParametroImobilizadoInventario01()
            params.id_imobilizado = id
            params.pagina = 0
            params.tamPagina = 50
        }

        try {
            val imobilizadoInventarioService =
                InfraHelper.apiInventario.create(ImobilizadoInventarioService::class.java)
            binding.llProgress40.visibility = View.VISIBLE
            imobilizadoInventarioService.getImobilizadosInventarios(params).enqueue(object :
                Callback<List<ImobilizadoinventarioModel>> {
                override fun onResponse(
                    call: Call<List<ImobilizadoinventarioModel>>,
                    response: Response<List<ImobilizadoinventarioModel>>
                ) {
                    binding.llProgress40.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            if (unico) {
                                var imos = response.body()!!
                                if (imos != null) {
                                    adapter.updateData(imos[0], idx)
                                }
                            } else {

                                val imobilizadoinventarios = response.body()!!.toMutableList()

                                if (imobilizadoinventarios !== null) {

                                    montaLista(imobilizadoinventarios);

                                } else {
                                    showToast("Falha No Retorno Da Requisição!")
                                }

                            }
                        } else {
                            binding.llProgress40.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast("Tabela De Fotos Vazia")
                                imobilizadoinventarios = mutableListOf<ImobilizadoinventarioModel>()
                                montaLista(imobilizadoinventarios)
                            } else {
                                showToast(message.getMessage().toString())
                            }
                        }
                    } else {
                        binding.llProgress40.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ImobilizadoinventarioModel>>, t: Throwable) {
                    binding.llProgress40.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            binding.llProgress40.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun montaLista(imobilizados: MutableList<ImobilizadoinventarioModel>) {
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


    private fun updateItem(imobilizado: ImobilizadoinventarioModel, idx: Int) {
        adapter.updateData(imobilizado, idx)
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

    private fun chamaFiltro() {
        val intent = Intent(this, FiltroInventarioActivity::class.java)
        getRetornoChamaFiltro.launch(intent)
    }

    private val getRetornoChamaFiltro =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                getInventarios(false)
            }
        }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }
}

