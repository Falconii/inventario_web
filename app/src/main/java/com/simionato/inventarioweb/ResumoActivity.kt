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
import com.google.gson.JsonObject
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.databinding.ActivityResumoBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.PaginasAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.ResumoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.parametros.parametroSendEmail01
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.services.ImobilizadoService
import com.simionato.inventarioweb.services.InventarioService
import com.simionato.inventarioweb.services.emailService
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

        setarRelatorio(0)

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

        binding.btAtualizarRelatorio78.setOnClickListener{
            gerarRelatorio()
            return@setOnClickListener
        }


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
        binding.ToolBar78.inflateMenu(R.menu.menu_resumo)
        binding.ToolBar78.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_resumo_cancel -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_resumo_refresh -> {
                    getResumo(Inventario.id_empresa, Inventario.id_filial, Inventario.codigo)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun getResumo(id_empresa: Int, id_filial: Int, codigo: Int) {
        setarRelatorio(0)
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

    fun gerarRelatorio()
    {

        setarRelatorio(1)
        var params: ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()


        Log.i("zyzz","${params}")

        try {
            val imobilizadoInventarioService =
                InfraHelper.apiInventario.create(ImobilizadoInventarioService::class.java)
            binding.llProgress78.visibility = View.VISIBLE
            imobilizadoInventarioService.getimobilizadosinventariosexcel(params).enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress78.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            val res = response.body()!!

                            if (res !== null) {

                                var mensagem =
                                    res?.get("message").toString()
                                showToast(mensagem)
                                enviarEmail()

                            } else {
                                showToast("Solicitação Sem Retorno")
                                setarRelatorio(0)
                            }
                        } else {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast("Nenhuma Informação Disponivel!")
                            } else {
                                showToast(message.getMessage().toString())
                            }
                            setarRelatorio(0)
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                        setarRelatorio(0)
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress78.visibility = View.GONE
                    showToast(t.message.toString())
                    setarRelatorio(0)
                }

            })
        } catch (e: Exception) {
         showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            setarRelatorio(0)
        }
    }

    fun enviarEmail()
    {

        setarRelatorio(2)
        var params: parametroSendEmail01 = parametroSendEmail01()

        Log.i("zyzz","${params}")

        try {
            val emailService =
                InfraHelper.apiInventario.create(emailService::class.java)
            binding.llProgress78.visibility = View.VISIBLE
            emailService.sendEmail(params).enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress78.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            val res = response.body()!!

                            if (res !== null) {

                                var mensagem =
                                    res?.get("message").toString()
                                showToast(mensagem)
                                setarRelatorio(0)

                            } else {
                                showToast("Solicitação Sem Retorno")
                                setarRelatorio(0)
                            }
                        } else {
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409) {
                                showToast("Nenhuma Informação Disponivel!")
                            } else {
                                showToast(message.getMessage().toString())
                            }
                            setarRelatorio(0)
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                        setarRelatorio(0)
                    }

                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress78.visibility = View.GONE
                    showToast(t.message.toString())
                    setarRelatorio(0)
                }

            })
        } catch (e: Exception) {
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            setarRelatorio(0)
        }
    }

    fun setarRelatorio(op:Int){
        binding.txtViewLabelAcao78.visibility = View.VISIBLE
        when (op) {
            1 -> {
                binding.txtViewLabelAcao78.visibility = View.VISIBLE
                binding.btAtualizarRelatorio78.setEnabled(false);
                binding.txtViewLabelAcao78.text = "Processando Relatorio..."
            }
            2 -> {
                binding.btAtualizarRelatorio78.setEnabled(false);
                binding.txtViewLabelAcao78.text = "Enviando E-Mail..."
            }
            else -> {
                binding.btAtualizarRelatorio78.setEnabled(true);
                binding.txtViewLabelAcao78.text = ""
            }
        }
    }
}