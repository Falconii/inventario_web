package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01

class InventarioActivity : AppCompatActivity() {

    private var params: ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()

    private lateinit var inventario: ImobilizadoinventarioModel

    private val binding by lazy {
        ActivityInventarioBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress40.visibility = View.GONE
        //Validando Paramentros
        if (ParametroGlobal.Ambiente.itsOK()){
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }
        inicializar()
        }


    private fun inicializar(){

        inicializarTooBar()


    }

    private fun inicializarTooBar(){
        binding.ToolBar40.title = "Controle De Ativos"
        binding.ToolBar40.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar40.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar40.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar40.inflateMenu(R.menu.menu_inventario)
        binding.ToolBar40.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.menu_inventario_sair -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_inventario_filtro -> {

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

