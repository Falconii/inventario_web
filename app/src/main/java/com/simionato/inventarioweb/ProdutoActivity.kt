package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityProdutoBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.ImobilizadoModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import com.simionato.inventarioweb.services.ImobilizadoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProdutoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProdutoBinding.inflate(layoutInflater)
    }

    var requestCamara: ActivityResultLauncher<String>? = null

    var paramCC: ParametroCentroCusto01 = ParametroCentroCusto01()

    var imobilizado: ImobilizadoModel = ImobilizadoModel()

    var idAcao: CadastrosAcoes = CadastrosAcoes.None

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if ((ParametroGlobal.Dados.usuario.id_empresa == 0) ||
            (ParametroGlobal.Dados.usuario.id == 0) ||
            (ParametroGlobal.Dados.local.id == 0) ||
            (ParametroGlobal.Dados.Inventario.codigo == 0)
        ) {
            showToast("Parâmetros Incompletos Para Esta Função!")
            finish()
        }
        binding.llProgress03.visibility = View.GONE

        requestCamara = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val intent = Intent(this, ScanBarCodeActivity::class.java)
                getRetornoCamera.launch(intent)
            } else {
                Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT).show()
            }
        }

        formulario(false)
        inicializar()

    }

    private val getRetornoCamera =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getStringExtra("codigo")
                try {
                    if (value != null) {
                        val codigo = value.toInt()
                        getImobilizado(
                            ParametroGlobal.Dados.empresa.id,
                            ParametroGlobal.Dados.local.id,
                            codigo
                        )
                    } else {
                        Toast.makeText(this, "Código Retornado Inválido!", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Código Inválido!", Toast.LENGTH_SHORT).show()
                }
                binding.editCodigo03.setText(value)
            } else {
                binding.editCodigo03.setText("")
            }
        }

    private fun inicializar() {

        inicializarTooBar()

        binding.imSearch03.setOnClickListener {

            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            // on below line hiding our keyboard.
            inputMethodManager.hideSoftInputFromWindow(binding.editCodigo03.getWindowToken(), 0)

            try {
                val codigo = binding.editCodigo03.text.toString().toInt()
                getImobilizado(
                    ParametroGlobal.Dados.empresa.id,
                    ParametroGlobal.Dados.local.id,
                    codigo
                )
            } catch (e: NumberFormatException) {
                showToast("Código Inválido!")
            }
        }


        binding.editDescricao03.filters += InputFilter.AllCaps()

        binding.ibLimparGrupo03.setOnClickListener {
            imobilizado.cod_grupo = 0
            imobilizado.grupo_descricao = ""
            binding.editGrupo03.setText(R.string.sem_filtro)
        }

        binding.ibLimparCc03.setOnClickListener {
            imobilizado.cod_cc = ""
            imobilizado.cc_descricao = ""
            binding.editcc03.setText(R.string.sem_filtro)
        }

        binding.editGrupo03.setOnClickListener {
            chamaPesquisaGrupo()
        }

        binding.editcc03.setOnClickListener {
            chamaPesquisaCc()
        }

        binding.btExcluir03.setOnClickListener { }

        binding.btCancelar03.setOnClickListener {
            binding.editCodigo03.setText("")
            formulario(false)
        }

        binding.btGravar03.setOnClickListener {
            if (idAcao == CadastrosAcoes.Inclusao) {
                imobilizado.origem = "M"
                imobilizado.descricao = binding.editDescricao03.text.toString();
                if (imobilizado.descricao.trim() == "") {
                    showToast("Descrição Obrigatótoria!")
                    return@setOnClickListener
                }
                if (imobilizado.cod_cc.trim() == "") {
                    showToast("Centro De Custo Obrigatório!")
                    return@setOnClickListener
                }
                if (imobilizado.cod_grupo == 0) {
                    showToast("Grupo Obrigatório!")
                    return@setOnClickListener
                }
                saveImobilizado()
                return@setOnClickListener
            } else {
                imobilizado = ImobilizadoModel()
                idAcao = CadastrosAcoes.None
                binding.editCodigo03.setText("")
                formulario(false)
                return@setOnClickListener
            }
        }
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
                            imobilizado.cod_grupo = codigo
                            imobilizado.grupo_descricao = descricao!!
                            binding.editGrupo03.setText(imobilizado.grupo_descricao)
                        }
                    } catch (e: NumberFormatException) {
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                imobilizado.cod_grupo = 0
                imobilizado.grupo_descricao = ""
                binding.editGrupo03.setText(R.string.sem_filtro)
            }
        }

    private fun chamaPesquisaGrupo() {
        val intent = Intent(this, PesquisaGrupoActivity::class.java)
        getRetornoPequisaGrupo.launch(intent)
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
                            imobilizado.cod_cc = codigo ?: ""
                            imobilizado.cc_descricao = descricao!!
                            binding.editcc03.setText(descricao)
                        }
                    } catch (e: NumberFormatException) {
                        imobilizado.cod_cc = ""
                        imobilizado.cc_descricao = ""
                        binding.editcc03.setText(R.string.sem_filtro)
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if (it.resultCode == 100) {
                imobilizado.cod_cc = ""
                imobilizado.cc_descricao = ""
                binding.editcc03.setText(R.string.sem_filtro)
                showToast("Código Inválido!", Toast.LENGTH_SHORT)
            }

        }

    private fun chamaPesquisaCc() {
        val intent = Intent(this, PesquisaCcActivity::class.java)
        getRetornoPequisaCc.launch(intent)
    }


    private fun inicializarTooBar() {
        binding.ToolBar03.title = "Controle De Ativos"
        binding.ToolBar03.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar03.setTitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
        binding.ToolBar03.setSubtitleTextColor(
            ContextCompat.getColor(this, R.color.white)
        )

        binding.ToolBar03.inflateMenu(R.menu.menu_voltar)
        binding.ToolBar03.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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

    private fun getImobilizado(id_empresa: Int, id_filial: Int, codigo: Int) {
        try {
            val imobilizadoService =
                InfraHelper.apiInventario.create(ImobilizadoService::class.java)
            binding.llProgress03.visibility = View.VISIBLE
            idAcao = CadastrosAcoes.Consulta
            imobilizadoService.getInventario(id_empresa, id_filial, codigo)
                .enqueue(object : Callback<ImobilizadoModel> {
                    override fun onResponse(
                        call: Call<ImobilizadoModel>,
                        response: Response<ImobilizadoModel>
                    ) {
                        binding.llProgress03.visibility = View.GONE


                        if (response != null) {

                            if (response.isSuccessful) {

                                val imo = response.body()

                                imobilizado = (imo ?: ImobilizadoModel()) as ImobilizadoModel

                                if (imobilizado.codigo !== 0) {
                                    showToast(
                                        "Ativo Já Cadastrado!",
                                        Toast.LENGTH_SHORT
                                    )
                                    idAcao = CadastrosAcoes.Consulta
                                } else {
                                    idAcao = CadastrosAcoes.Inclusao
                                }
                                loadFormulario()
                                formulario(true)
                            } else {
                                binding.llProgress03.visibility = View.GONE
                                val gson = Gson()
                                val message = gson.fromJson(
                                    response.errorBody()!!.charStream(),
                                    HttpErrorMessage::class.java
                                )
                                if (response.code() == 409) {
                                    idAcao = CadastrosAcoes.Inclusao
                                    imobilizado = ImobilizadoModel()
                                    imobilizado.id_empresa = Inventario.id_empresa
                                    imobilizado.id_filial  = Inventario.id_filial
                                    imobilizado.codigo = codigo
                                    loadFormulario()
                                    formulario(true)
                                } else {
                                    showToast("${message.getMessage()}")
                                }
                            }
                        } else {
                            binding.llProgress03.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Sem retorno Da Requisição!",
                                Toast.LENGTH_SHORT
                            ).show()
                            idAcao = CadastrosAcoes.None
                        }

                    }

                    override fun onFailure(call: Call<ImobilizadoModel>, t: Throwable) {
                        binding.llProgress03.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Sem retorno Da Requisição!",
                            Toast.LENGTH_SHORT
                        ).show()
                        idAcao = CadastrosAcoes.None
                    }
                })
        } catch (e: Exception) {
            Log.i("zyzz", "${e.message}")
            binding.llProgress03.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            idAcao = CadastrosAcoes.None
        }

    }

    private fun saveImobilizado() {

        try {

            val imobilizadoService =
                InfraHelper.apiInventario.create(ImobilizadoService::class.java)
            imobilizadoService.postImobilizadoInventario(
                imobilizado,
                ParametroGlobal.Dados.Inventario.codigo
            ).enqueue(object : Callback<ImobilizadoModel> {
                override fun onResponse(
                    call: Call<ImobilizadoModel>,
                    response: Response<ImobilizadoModel>
                ) {
                    binding.llProgress03.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            var imo = response.body()
                            if (imo != null) {
                                showToast("Imonilizado Gravado!")
                                imobilizado = ImobilizadoModel()
                                idAcao = CadastrosAcoes.None
                                binding.editCodigo03.setText("")
                                formulario(false)
                            } else {
                                showToast("Falha Na Gravacao Do Imobilizado")
                            }
                        } else {
                            binding.llProgress03.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                        }
                    } else {
                        binding.llProgress03.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<ImobilizadoModel>, t: Throwable) {
                    binding.llProgress03.visibility = View.GONE
                    showToast(t.message.toString())
                }

            })

        } catch (e: Exception) {
            binding.llProgress03.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun formulario(show: Boolean) {
        if (show) {
            if (idAcao == CadastrosAcoes.Consulta) {
                binding.textViewTitulo03.setText("Produto Localizado.\nSOMENTE CONSULTA")
            } else {
                binding.textViewTitulo03.setText("Produto Novo")
            }
        }
        if (idAcao == CadastrosAcoes.Consulta) {
            binding.editDescricao03.setFocusable(false)
            binding.ibLimparCc03.visibility = View.GONE
            binding.ibLimparGrupo03.visibility = View.GONE
        } else {
            binding.editDescricao03.setFocusableInTouchMode(true)
            binding.ibLimparCc03.visibility = View.VISIBLE
            binding.ibLimparGrupo03.visibility = View.VISIBLE
        }
        binding.llLinhaCodigoAtual03.visibility = if (!show) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.llLinhaDescricao03.visibility = if (!show) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.llLinhaGrupo03.visibility = if (!show) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.llLinhaCC03.visibility = if (!show) {
            View.GONE
        } else {
            View.VISIBLE
        }
        if (show) {
            if (idAcao == CadastrosAcoes.Inclusao) {
                binding.btExcluir03.visibility = View.GONE
                binding.btCancelar03.visibility = View.VISIBLE
                binding.btGravar03.visibility = View.VISIBLE
                binding.btGravar03.text = "Gravar"
            } else {
                binding.btExcluir03.visibility = View.GONE
                binding.btCancelar03.visibility = View.GONE
                binding.btGravar03.visibility = View.VISIBLE
                binding.btGravar03.text = "Pesquisar Outro Produto"
            }
        } else {
            binding.btExcluir03.visibility = View.GONE
            binding.btCancelar03.visibility = View.GONE
            binding.btGravar03.visibility = View.GONE
        }

        pesquisa(!show)
    }

    private fun pesquisa(show: Boolean) {
        if (show) {
            binding.textViewTitulo03.setText("PRODUTO NOVO")
        }
        binding.llCodigo03.visibility = if (!show) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun loadFormulario() {

        with(binding) {
            editCodigoAtual03.setText(imobilizado.codigo.toString())
            editDescricao03.setText(imobilizado.descricao)
            if (imobilizado.cod_cc != "") {
                editcc03.setText(imobilizado.cc_descricao)
            } else {
                editcc03.setText(R.string.sem_filtro)
            }
            if (imobilizado.cod_grupo != 0) {
                editGrupo03.setText(imobilizado.grupo_descricao)
            } else {
                editGrupo03.setText(R.string.sem_filtro)
            }
        }

    }

}