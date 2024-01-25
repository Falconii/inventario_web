package com.simionato.inventarioweb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.simionato.inventarioweb.databinding.ActivityMainBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "INVENTARIO_PREFERS")
private val id_empresa_key = intPreferencesKey("id_empresa")
private val id_usuario_key = intPreferencesKey("id_usuario")
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarTooBar()
        lifecycleScope.launch(Dispatchers.IO){
            getUserProfile().collect{
                withContext(Dispatchers.Main){
                    Log.i("zyz","${it}")
                    ParametroGlobal.Dados.empresa.id = it.id_empresa
                    ParametroGlobal.Dados.usuario.id_empresa = it.id_empresa
                    ParametroGlobal.Dados.usuario.id         = it.id_usuario
                    startActivity(
                            Intent(applicationContext, LoginActivity::class.java)
                        )

                }
            }
        }

       // lifecycleScope.launch(Dispatchers.IO) {
       //     saveUserProfile(1,50)
       // }

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

        binding.btnProduto.setOnClickListener({
            lifecycleScope.launch(Dispatchers.IO){
                getUserProfile().collect{
                    withContext(Dispatchers.Main){
                        binding.lblNomeUsuario.setText(it.id_usuario.toString())
                    }
                }
                }
            //startActivity(
            //    Intent(this, ProdutoActivity::class.java)
            //)
        })
    }

    private fun inicializarTooBar(){
        binding.ToolBar00.title = "Controle De Ativos"
        binding.ToolBar00.subtitle = "Tela Principal"
        binding.ToolBar00.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar00.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar00.inflateMenu(R.menu.menu_principal)
        binding.ToolBar00.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_login -> {
                    startActivity(
                        Intent(applicationContext, LoginActivity::class.java)
                    )
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private suspend fun saveUserProfile(id_empresa:Int, id_ususario:Int){
        dataStore.edit { preferences ->
            preferences[id_empresa_key] = id_empresa
            preferences[id_usuario_key] = id_ususario
        }
        Log.i("zyz","Salvo id_empresa ${id_empresa} id_ususario ${id_ususario}")
    }

    private  fun getUserProfile() =  dataStore.data.map { preferences ->
        UserProfile(
            id_empresa = preferences[id_empresa_key] ?: 1,
            id_usuario = preferences[id_usuario_key] ?:1
        )
    }
}