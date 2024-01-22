package com.simionato.inventarioweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.simionato.inventarioweb.databinding.ActivityParametrosBinding
import com.simionato.inventarioweb.databinding.ActivityProdutoBinding

class ProdutoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProdutoBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}