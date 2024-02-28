package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.simionato.inventarioweb.databinding.ActivityFiltroInventarioBinding
import com.simionato.inventarioweb.databinding.ActivityFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class FiltroInventarioActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFiltroInventarioBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        iniciar()
    }


    private fun iniciar(){
        inicializarTooBar()

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

        binding.ibLimparCcOrig35.setOnClickListener {
            ParametroGlobal.Dados.paramImoInventario.id_cc = ""
            binding.editCCOriginal35.setText("Sem Filtro. Toque Para Alterar")
        }

        binding.ibLimparGrupo35.setOnClickListener {
            ParametroGlobal.Dados.paramImoInventario.id_grupo = 0
            binding.editGrupo35.setText("Sem Filtro. Toque Para Alterar")
        }

        binding.ibLimparCcNew35.setOnClickListener {
            ParametroGlobal.Dados.paramImoInventario.new_cc = ""
            binding.editCCNew35.setText("Sem Filtro. Toque Para Alterar")
        }

        binding.ibLimparUsuario35.setOnClickListener {
            ParametroGlobal.Dados.paramImoInventario.id_usuario = 0
            binding.editUsusario35.setText("Sem Filtro. Toque Para Alterar")
        }

    }
    private fun inicializarTooBar(){
        binding.ToolBar35.title = "Controle De Ativos"
        binding.ToolBar35.subtitle = "Filtros Da Pesquisa"
        binding.ToolBar35.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar35.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar35.inflateMenu(R.menu.menu_filtro)
        binding.ToolBar35.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_filtro_cancelar -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_filtro_Limpar -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_filtro_limpar_filtrar -> {
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

    private val getRetornoPequisaCc =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK && it.data != null){
                if(it.resultCode == Activity.RESULT_OK){
                    val codigo = it.data?.getStringExtra("codigo")
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo?.trim() != ""){
                            ParametroGlobal.Dados.paramImoInventario.id_cc = codigo ?: ""
                            binding.editCCOriginal35.setText(descricao)
                        }
                    } catch ( e : NumberFormatException ){
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if(it.resultCode == 100){
                ParametroGlobal.Dados.paramImoInventario.id_cc = ""
                binding.editCCOriginal35.setText("Sem Filtro. Toque Para Alterar!")

            }

        }

    private fun chamaPesquisaCc(){
        val intent = Intent(this,PesquisaCcActivity::class.java)
        getRetornoPequisaCc.launch(intent)
    }

    private val getRetornoPequisaNewCc =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK && it.data != null){
                if(it.resultCode == Activity.RESULT_OK){
                    val codigo = it.data?.getStringExtra("codigo")
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo?.trim() != ""){
                            ParametroGlobal.Dados.paramImoInventario.new_cc = codigo ?: ""
                            binding.editCCNew35.setText(descricao)
                        }
                    } catch ( e : NumberFormatException ){
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if(it.resultCode == 100){
                ParametroGlobal.Dados.paramImoInventario.new_cc = ""
                binding.editCCNew35.setText("Sem Filtro. Toque Para Alterar!")

            }

        }

    private fun chamaPesquisaNewCc(){
        val intent = Intent(this,PesquisaCcActivity::class.java)
        getRetornoPequisaNewCc.launch(intent)
    }

    private val getRetornoPequisaGrupo =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK && it.data != null){
                if(it.resultCode == Activity.RESULT_OK){
                    val codigo = it.data?.getIntExtra("codigo",0)
                    val descricao = it.data?.getStringExtra("descricao")
                    try {
                        if (codigo != null && codigo != 0){
                            ParametroGlobal.Dados.paramImoInventario.id_grupo = codigo
                            binding.editGrupo35.setText(descricao)
                        }
                    } catch ( e : NumberFormatException ){
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if(it.resultCode == 100){
                ParametroGlobal.Dados.paramImoInventario.id_grupo = 0
                binding.editGrupo35.setText("Sem Filtro")

            }
        }

    private fun chamaPesquisaGrupo(){
        val intent = Intent(this,PesquisaGrupoActivity::class.java)
        getRetornoPequisaGrupo.launch(intent)
    }


    private val getRetornoPequisaUsuario =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK && it.data != null){
                if(it.resultCode == Activity.RESULT_OK){
                    val codigo = it.data?.getIntExtra("id_usuario",0)
                    val razao = it.data?.getStringExtra("razao")
                    try {
                        if (codigo != null && codigo != 0){
                            ParametroGlobal.Dados.paramImoInventario.id_usuario = codigo
                            binding.editUsusario35.setText(razao)
                        }
                    } catch ( e : NumberFormatException ){
                        showToast("Código Inválido!", Toast.LENGTH_SHORT)
                    }
                }
            }
            if(it.resultCode == 100){
                ParametroGlobal.Dados.paramImoInventario.id_usuario = 0
                binding.editGrupo35.setText("Sem Filtro")

            }
        }

    private fun chamaPesquisaUsuario(){
        val intent = Intent(this,PesquisaUsuarioActivity::class.java)
        getRetornoPequisaUsuario.launch(intent)
    }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }



}