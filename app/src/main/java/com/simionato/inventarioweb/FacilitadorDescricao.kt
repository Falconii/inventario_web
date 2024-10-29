package com.simionato.inventarioweb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simionato.inventarioweb.databinding.ActivityFacilitadorDescricaoBinding
import com.simionato.inventarioweb.databinding.ActivityFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class FacilitadorDescricao : AppCompatActivity() {

    private val binding by lazy {
        ActivityFacilitadorDescricaoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ParametroGlobal.Ambiente.itsOK()){
            showToast("Ambiente Incorreto!!")
            finish()
            return
        }


    }


    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}