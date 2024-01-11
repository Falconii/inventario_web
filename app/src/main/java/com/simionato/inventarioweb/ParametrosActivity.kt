package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.databinding.ActivityParametrosBinding

class ParametrosActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityParametrosBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val texto = " A mutação de um tipo usado no DataStore invalida todas as garantias fornecidas e cria bugs potencialmente graves e difíceis de detectar. É altamente recomendável usar buffers de protocolo que ofereçam garantias de imutabilidade, uma API simples e uma serialização eficiente."
        binding.editEmpresa01.setText(texto)
        binding.editEmpresa01.setOnClickListener{
            Log.i("CLICADO","Entrei No Evento")
            Toast.makeText(this,"Olha a Empresa",Toast.LENGTH_LONG).show()
            if (binding.editEmpresa01.text.toString().contains("MARCOS RENATO FALCONI")){
                binding.editEmpresa01.setText("")
            } else {
                binding.editEmpresa01.setText("MARCOS RENATO FALCONI")
            }
            true
        }
    }
}