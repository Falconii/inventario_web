package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simionato.inventarioweb.databinding.ActivityFotoWebBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class FotoWebActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFotoWebBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        configurarWebView()

        val idFile = intent.getStringExtra("id_file")
        val localizacao = intent.getStringExtra("localizacao")

        if (idFile.isNullOrEmpty()) {
            encerrarComErro("Não foi informado o ID da foto!")
            return
        }

        if (localizacao.isNullOrEmpty()) {
            encerrarComErro("Não foi informada a localização da foto!")
            return
        }

        if (localizacao == "D") {
            carregarImagemLocal(idFile)
        } else {
            carregarImagemWeb(idFile)
        }
    }

    private fun inicializarToolbar() = with(binding.ToolBar35) {
        title = "Controle De Ativos"
        subtitle = ParametroGlobal.Dados.Inventario.descricao
        setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        setSubtitleTextColor(ContextCompat.getColor(context, R.color.white))
        inflateMenu(R.menu.menu_login)

        setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.item_cancel) {
                setResult(Activity.RESULT_OK, Intent())
                finish()
            }
            true
        }
    }

    private fun configurarWebView() = with(binding.webView35) {
        settings.apply {
            builtInZoomControls = true
            allowFileAccess = true
            allowContentAccess = true
        }
        setInitialScale(-1)
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.llProgress35.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.llProgress35.visibility = View.GONE
            }
        }
    }

    private fun carregarImagemLocal(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            val html = """
                <html>
                <body style="margin:0;padding:0;">
                    <img src="data:image/jpeg;base64,$base64" style="width:100%;height:auto;" />
                </body>
                </html>
            """.trimIndent()

            binding.webView35.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            showToast("Erro ao carregar imagem local: ${e.message}")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun carregarImagemWeb(idFile: String) {
        val url = "https://drive.google.com/uc?export=view&id=$idFile"
        binding.webView35.loadUrl(url)
    }

    private fun encerrarComErro(mensagem: String) {
        showToast(mensagem)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun showToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }
}