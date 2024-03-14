package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simionato.inventarioweb.databinding.ActivityEditFotoBinding
import com.simionato.inventarioweb.databinding.ActivityShowFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel

class EditFotoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditFotoBinding.inflate(layoutInflater)
    }
    
    private var foto:FotoModel = FotoModel() 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        try {
            val bundle = intent.extras
            Log.i("zyzz", "Cheguei fotoedicao ${bundle}")
            if (bundle != null) {
                foto = if (Build.VERSION.SDK_INT >= 33) bundle.getParcelable(
                    "foto",
                    FotoModel::class.java
                )!!
                else bundle.getParcelable("foto")!!
            } else {
                showToast("Parâmetro Da Foto Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }

        iniciar()
    }

    private fun iniciar(){

        inicializarTooBar()

        binding.txtViewSituacao42.setText(ParametroGlobal.prettyText.ambiente_produto(foto.id_imobilizado,foto.imo_descricao))
        binding.editUsuario42.setText(ParametroGlobal.Dados.usuario.razao)

        binding.txtInputObs.filters += InputFilter.AllCaps()
        binding.swDestaque42.isChecked = if (foto.destaque == "S") true else false
        binding.swDestaque42.setText(if (binding.swDestaque42.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        binding.swDestaque42.setText("Foto Não Está Em Destaque")
        binding.swDestaque42.setOnClickListener {
            binding.swDestaque42.setText(if (binding.swDestaque42.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        }

        binding.btGravar42.setOnClickListener {
            //uploadFoto()
        }
        binding.btCancelar42.setOnClickListener {
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
        }
       
    }

    private fun inicializarTooBar(){
        binding.ToolBar42.title = "Controle De Ativos"
        binding.ToolBar42.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar42.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar42.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar42.inflateMenu(R.menu.menu_login)
        binding.ToolBar42.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
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

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}