package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityFiltroInventarioBinding
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.paramImoInventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ParametroModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.services.ParametroService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FiltroInventarioActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFiltroInventarioBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress35.visibility = View.GONE
        iniciar()
    }

    override fun onResume() {
        super.onResume()
        loadParametros()
    }

    private fun iniciar() {
        inicializarTooBar()

        binding.rgCodigos35.setOnCheckedChangeListener{ _, _ ->
            if (binding.rbAntigo35.isChecked) {
                paramImoInventario._searchIndex = 0
            }
            if (binding.rbNovo35.isChecked) {
                paramImoInventario._searchIndex = 1
            }
            if (binding.rbDescricao35.isChecked) {
                paramImoInventario._searchIndex = 2
            }
        }

        binding.editCCOriginal35.setOnClickListener {
            chamaPesquisaCc()
        }

        binding.editGrupo35.setOnClickListener {
            chamaPesquisaGrupo()
        }

        binding.editCCNew35.setOnClickListener {
            chamaPesquisaNewCc()
        }

        binding.editUsusario35.setOnClickListener {
            chamaPesquisaUsuario()
        }

        binding.rgSituacao35.setOnCheckedChangeListener{ _, _ ->
            if (binding.rbSemFiltroSituacao35.isChecked) {
                paramImoInventario.status = -1
            }
            if (binding.rbInventariadoTodos35.isChecked) {
                paramImoInventario.status = 90
            }
            if (binding.rbNaoInventariado35.isChecked) {
                paramImoInventario.status = 0
            }
            if (binding.rbInventariado35.isChecked) {
                paramImoInventario.status = 0
            }
            if (binding.rbInventariadoTrocaCodigo35.isChecked) {
                paramImoInventario.status = 2
            }
            if (binding.rbInventariadoTrocaCC35.isChecked) {
                paramImoInventario.status = 3
            }
            if (binding.rbInventariadoTrocaCodigoECC35.isChecked) {
                paramImoInventario.status = 4
            }
            if (binding.rbInventariadoNaoEncontrado35.isChecked) {
                paramImoInventario.status = 5
            }
        }

        binding.rgOrigem35.setOnCheckedChangeListener{ _, _ ->
            if (binding.rbOrigemTodas35.isChecked) {
                paramImoInventario.origem = ""
            }
            if (binding.rbOrigemPlanilha.isChecked) {
                paramImoInventario.origem = "P"
            }
            if (binding.rbOrigemManual35.isChecked) {
                paramImoInventario.origem = "M"
            }
        }

        binding.rgFoto35.setOnCheckedChangeListener{ _, _ ->
            if (binding.rbFotoTodas35.isChecked) {
                paramImoInventario.foto = 0
            }
            if (binding.rbComFoto35.isChecked) {
                paramImoInventario.foto = 1
            }
            if (binding.rbSemFoto35.isChecked) {
                paramImoInventario.foto = 2
            }
        }

        binding.ibLimparCcOrig35.setOnClickListener {
            paramImoInventario.id_cc = ""
            binding.editCCOriginal35.setText(R.string.sem_filtro)
        }

        binding.ibLimparGrupo35.setOnClickListener {
            paramImoInventario.id_grupo = 0
            paramImoInventario._descricaoGrupo = ""
            binding.editGrupo35.setText(R.string.sem_filtro)
        }

        binding.ibLimparCcNew35.setOnClickListener {
            paramImoInventario.new_cc = ""
            paramImoInventario._descricaoNewCC = ""
            binding.editCCNew35.setText(R.string.sem_filtro)
        }

        binding.ibLimparUsuario35.setOnClickListener {
            paramImoInventario.id_usuario = 0
            paramImoInventario._nomeUsuario = ""
            binding.editUsusario35.setText(R.string.sem_filtro)
        }

    }

    private fun inicializarTooBar() {
        binding.ToolBar35.title = "Controle De Ativos"
        binding.ToolBar35.subtitle = "Filtros Da Pesquisa"
        binding.ToolBar35.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar35.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar35.inflateMenu(R.menu.menu_filtro)
        binding.ToolBar35.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_filtro_cancelar -> {
                   retorno()
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_filtro_Limpar -> {
                    paramImoInventario = ParametroImobilizadoInventario01()
                    loadParametros()
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_filtro_filtrar -> {
                    atualizaParametros()
                    return@setOnMenuItemClickListener true
                }

                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun atualizaParametros() {
        try {
            paramImoInventario.id_imobilizado = 0
            paramImoInventario.new_codigo = 0
            paramImoInventario.descricao = ""
            var parametro = ParametroModel(
                usuario.id_empresa,
                "inventariomobile",
                "V1.00 29/02/24",
                usuario.id,
                "",
                usuario.id,
                0
            )
            val gson = Gson()
            val parString = gson.toJson(paramImoInventario)

            parametro.parametro = parString

            val parametroService = InfraHelper.apiInventario.create(ParametroService::class.java)
            binding.llProgress35.visibility = View.VISIBLE
            parametroService.postParametroAtualiza(parametro)
                .enqueue(object : Callback<ParametroModel> {
                    override fun onResponse(
                        call: Call<ParametroModel>,
                        response: Response<ParametroModel>
                    ) {
                        binding.llProgress35.visibility = View.GONE

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

                                    retorno()
                                }
                            } else {
                                binding.llProgress35.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409){
                                    showToast("Parâmetro Não Atualizado!")
                                } else {
                                    showToast(message.getMessage().toString())
                                }
                                retorno()
                            }
                        } else {
                            binding.llProgress35.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                            retorno()
                        }
                    }

                    override fun onFailure(call: Call<ParametroModel>, t: Throwable) {
                        binding.llProgress35.visibility = View.GONE
                        showToast(t.message.toString())
                        retorno();
                    }
                })
        } catch (e: Exception) {
            binding.llProgress35.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            retorno()
        }
    }

    private val getRetornoPequisaCc =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val codigo = it.data?.getStringExtra("codigo")
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo?.trim() != "") {
                            paramImoInventario.id_cc = codigo ?: ""
                            paramImoInventario._descricaoCC = descricao!!
                            binding.editCCOriginal35.setText(paramImoInventario._descricaoCC)
                        }
                    } catch (e: NumberFormatException) {
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                paramImoInventario.id_cc = ""
                binding.editCCOriginal35.setText("Sem Filtro. Toque Para Alterar!")

            }

        }

    private fun chamaPesquisaCc() {
        val intent = Intent(this, PesquisaCcActivity::class.java)
        getRetornoPequisaCc.launch(intent)
    }

    private val getRetornoPequisaNewCc =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val codigo = it.data?.getStringExtra("codigo")
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo?.trim() != "") {
                            paramImoInventario.new_cc = codigo ?: ""
                            paramImoInventario._descricaoNewCC = descricao!!
                            binding.editCCNew35.setText(paramImoInventario._descricaoNewCC)
                        }
                    } catch (e: NumberFormatException) {
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                paramImoInventario.new_cc = ""
                binding.editCCNew35.setText("Sem Filtro. Toque Para Alterar!")

            }

        }

    private fun chamaPesquisaNewCc() {
        val intent = Intent(this, PesquisaCcActivity::class.java)
        getRetornoPequisaNewCc.launch(intent)
    }

    private val getRetornoPequisaGrupo =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val codigo = it.data?.getIntExtra("codigo", 0)
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo != null && codigo != 0) {
                            paramImoInventario.id_grupo = codigo
                            paramImoInventario._descricaoGrupo = descricao!!
                            binding.editGrupo35.setText(paramImoInventario._descricaoGrupo)
                        }
                    } catch (e: NumberFormatException) {
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                paramImoInventario.id_grupo = 0
                binding.editGrupo35.setText("Sem Filtro")

            }
        }

    private fun chamaPesquisaGrupo() {
        val intent = Intent(this, PesquisaGrupoActivity::class.java)
        getRetornoPequisaGrupo.launch(intent)
    }


    private val getRetornoPequisaUsuario =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val codigo = it.data?.getIntExtra("id_usuario", 0)
                    val razao = it.data?.getStringExtra("razao")
                    try {
                        if (codigo != null && codigo != 0) {
                            paramImoInventario.id_usuario = codigo
                            paramImoInventario._nomeUsuario = razao!!
                            binding.editUsusario35.setText(paramImoInventario._nomeUsuario)
                        }
                    } catch (e: NumberFormatException) {
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                paramImoInventario.id_usuario = 0
                binding.editGrupo35.setText("Sem Filtro")

            }
        }

    private fun chamaPesquisaUsuario() {
        val intent = Intent(this, PesquisaUsuarioActivity::class.java)
        getRetornoPequisaUsuario.launch(intent)
    }

    private fun loadParametros() {
        when (paramImoInventario._searchIndex) {
            0 -> {
                binding.rbAntigo35.isChecked = true
            }

            1 -> {
                binding.rbNovo35.isChecked = true
            }

            2 -> {
                binding.rbDescricao35.isChecked = true
            }

            else -> {
                binding.rbDescricao35.isChecked = true
            }
        }
        when (paramImoInventario.status) {
            -1 -> {
                binding.rbSemFiltroSituacao35.isChecked = true
            }

            0 -> {
                binding.rbNaoInventariado35.isChecked = true
            }

            1 -> {
                binding.rbInventariado35.isChecked = true
            }

            2 -> {
                binding.rbInventariadoTrocaCodigo35.isChecked = true
            }

            3 -> {
                binding.rbInventariadoTrocaCC35.isChecked = true
            }

            4 -> {
                binding.rbInventariadoTrocaCodigoECC35.isChecked = true
            }

            5 -> {
                binding.rbInventariadoNaoEncontrado35.isChecked = true
            }

            90 -> {
                binding.rbInventariadoTodos35.isChecked = true
            }

            else -> {
                binding.rbSemFiltroSituacao35.isChecked = true
            }
        }
        when (paramImoInventario.origem) {
            "" -> {
                binding.rbOrigemTodas35.isChecked = true
            }

            "P" -> {
                binding.rbOrigemPlanilha.isChecked = true
            }

            "M" -> {
                binding.rbOrigemManual35.isChecked = true
            }

            else -> {
                binding.rbOrigemTodas35.isChecked = true
            }
        }
        when (paramImoInventario.foto) {
            0 -> {
                binding.rbFotoTodas35.isChecked = true
            }

            1 -> {
                binding.rbComFoto35.isChecked = true
            }

            2 -> {
                binding.rbSemFoto35.isChecked = true
            }

            else -> {
                binding.rbFotoTodas35.isChecked = true
            }
        }
        try {
            if (paramImoInventario.id_cc != "") {
                binding.editCCOriginal35.setText(paramImoInventario._descricaoCC)
            } else {
                binding.editCCOriginal35.setText(R.string.sem_filtro)
            }
        } catch (e: NumberFormatException) {
            binding.editCCOriginal35.setText(R.string.sem_filtro)
        }
        try {
            if (paramImoInventario.new_cc != "") {
                binding.editCCNew35.setText(paramImoInventario._descricaoNewCC)
            } else {
                binding.editCCNew35.setText(R.string.sem_filtro)
            }
        } catch (e: NumberFormatException) {
            binding.editCCNew35.setText(R.string.sem_filtro)
        }
        try {
            if (paramImoInventario.id_grupo != 0) {
                binding.editGrupo35.setText(paramImoInventario._descricaoGrupo)
            } else {
                binding.editGrupo35.setText(R.string.sem_filtro)
            }
        } catch (e: NumberFormatException) {
            binding.editGrupo35.setText(R.string.sem_filtro)
        }
        try {
            if (paramImoInventario.id_usuario != 0) {
                binding.editUsusario35.setText(paramImoInventario._nomeUsuario)
            } else {
                binding.editUsusario35.setText(R.string.sem_filtro)
            }
        } catch (e: NumberFormatException) {
            binding.editUsusario35.setText(R.string.sem_filtro)
        }
    }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }

    fun retorno(){
        val returnIntent: Intent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

}