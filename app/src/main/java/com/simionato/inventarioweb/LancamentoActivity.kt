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
    }
}