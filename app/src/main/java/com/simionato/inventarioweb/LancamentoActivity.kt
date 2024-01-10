package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.simionato.inventarioweb.databinding.ActivityLancamentoBinding
import com.simionato.inventarioweb.databinding.ActivityLoginBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class LancamentoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLancamentoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.editDescricao.setText("Descrição Do Produto")
        binding.editDescricao.setOnLongClickListener{
            Toast.makeText(this, "Descrição", Toast.LENGTH_SHORT).show()
            true
        }
        binding.editCCNovo.setOnLongClickListener{
            Toast.makeText(this, "Long click detected", Toast.LENGTH_SHORT).show()
            true
        }
    }
}