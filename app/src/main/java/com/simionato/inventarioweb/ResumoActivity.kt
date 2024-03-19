package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.databinding.ActivityResumoBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.PaginasAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.ResumoModel
import com.simionato.inventarioweb.services.ImobilizadoService
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResumoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityResumoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ParametroGlobal.Ambiente.itsOK()) {
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }
        binding.llProgress78.visibility = View.GONE;
        inicializar()
    }

    private fun inicializar() {

        inicializarTooBar()

        binding.textViewTitulo78.setText(ParametroGlobal.prettyText.ambiente())

        binding.txtViewLabelSituacao078.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                0
            )
        }")
        binding.txtViewLabelSituacao178.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                1
            )
        }")
        binding.txtViewLabelSituacao278.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                2
            )
        }")
        binding.txtViewLabelSituacao378.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                3
            )
        }")
        binding.txtViewLabelSituacao478.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                4
            )
        }")
        binding.txtViewLabelSituacao578.setText("${
            ParametroGlobal.Situacoes.getSituacao(
                5
            )
        }")

        getResumo(Inventario.id_empresa, Inventario.id_filial, Inventario.codigo)

    }


    private fun inicializarTooBar() {
        binding.ToolBar78.title = "Controle De Ativos"
        binding.ToolBar78.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar78.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar78.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar78.inflateMenu(R.menu.menu_login)
        binding.ToolBar78.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_cancel -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun getResumo(id_empresa: Int, id_filial: Int, codigo: Int) {
        try {
            val inventarioService =
                InfraHelper.apiInventario.create(InventarioService::class.java)
            binding.llProgress78.visibility = View.VISIBLE
            inventarioService.getResumo(id_empresa, id_filial, codigo)
                .enqueue(object : Callback<ResumoModel> {
                    override fun onResponse(
                        call: Call<ResumoModel>,
                        response: Response<ResumoModel>
                    ) {
                        binding.llProgress78.visibility = View.GONE


                        if (response != null) {

                            if (response.isSuccessful) {

                                val resumo = response.body()

                                var resumoModel = (resumo ?: ResumoModel())!!

                                Log.i("zyzz","${resumoModel}")

                                loadFormulario(resumoModel)

                            } else {
                                binding.llProgress78.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    showToast("${message.getMessage()}")
                                } else {
                                    showToast("${message.getMessage()}")
                                }
                            }
                        } else {
                            binding.llProgress78.visibility = View.GONE
                            showToast(
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResumoModel>, t: Throwable) {
                        binding.llProgress78.visibility = View.GONE
                        showToast(
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        )
                    }
                })

        } catch (e: Exception) {
            Log.i("zyzz", "${e.message}")
            binding.llProgress78.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    fun loadFormulario(resumo:ResumoModel){
        binding.editTotalAtivos78.setText(resumo.total_ativos.toString())
        binding.editTotalAtivosInventariado78.setText(resumo.total_inventariados.toString())
        binding.editSaldoAInventariar78.setText((resumo.total_ativos-resumo.total_inventariados).toString())
        binding.editSituacao078.setText(resumo.situacao_0.toString())
        binding.editSituacao178.setText(resumo.situacao_1.toString())
        binding.editSituacao278.setText(resumo.situacao_2.toString())
        binding.editSituacao378.setText(resumo.situacao_3.toString())
        binding.editSituacao478.setText(resumo.situacao_4.toString())
        binding.editSituacao578.setText(resumo.situacao_5.toString())
        binding.editFotos78.setText(resumo.fotos.toString())
    }
    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }
}