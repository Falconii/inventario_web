package com.simionato.inventarioweb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener{
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }

        binding.btnLancamento.setOnClickListener{
            startActivity(
                Intent(this, LancamentoActivity::class.java)
            )
        }

        binding.btnInventario.setOnClickListener{
            startActivity(
                Intent(this, InventarioActivity::class.java)
            )
        }

        binding.btnPesquisa.setOnClickListener{
            startActivity(
                Intent(this, PesquisaActivity::class.java)
            )
        }

        binding.btnParametros.setOnClickListener{
            startActivity(
                Intent(this, ParametrosActivity::class.java)
            )
        }
    }


}