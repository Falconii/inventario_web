package com.simionato.inventarioweb

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import com.simionato.inventarioweb.databinding.ActivityProdutoBinding

class InventarioActivity : AppCompatActivity() {
    private var requestCamara:ActivityResultLauncher <String>? = null
    private val binding by lazy {
        ActivityInventarioBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestCamara = registerForActivityResult(ActivityResultContracts.RequestPermission(),){
            if (it){
                val intent = Intent(this,ScanBarCodeActivity::class.java)
                getResult.launch(intent)
            } else {
                Toast.makeText(this,"Permissão Negada",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btScannear.setOnClickListener({
            requestCamara?.launch(android.Manifest.permission.CAMERA)
        })
    }

    //Receptor
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val value = it.data?.getStringExtra("codigo")
                binding.txtResult.setText(value)
            } else {
                binding.txtResult.setText("")
            }
        }
}