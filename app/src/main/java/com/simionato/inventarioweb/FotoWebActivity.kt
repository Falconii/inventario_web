package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simionato.inventarioweb.databinding.ActivityFotoWebBinding
import com.simionato.inventarioweb.global.ParametroGlobal

class FotoWebActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFotoWebBinding.inflate(layoutInflater)
    }


    private val stringHtml = "<!DOCTYPE html> "+
                              "<html> " +
                                "<head> " +
                                "<meta charset=\"UTF-8\"/> " +
                                "<title>FOTO</title> " +
                                "</head> " +
                                "<body> " +
                                "<img src=\"https://drive.google.com/thumbnail?id=1QSfT3hO40mN-9znlqq7hmdHWQRBe2kRi&sz=s4000\" alt=\"Ativo Imobilizado\" style=\"width:auto;height:auto;\">" +
                                "</body> " +
                               "</html> "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarTooBar()

        var id_file =  intent.getStringExtra("id_file")
        var localizacao = intent.getStringExtra("localizacao")

        if ( id_file == null || id_file.isEmpty()){
            showToast("Não Foi Informado O ID Da Foto!");
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
            return
        }

        if ( localizacao == null || localizacao.isEmpty()){
            showToast("Não Foi Informada A Localização Da Foto!");
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
            return
        }

        binding.webView35.getSettings().setBuiltInZoomControls(true);
        binding.webView35.setInitialScale(-1);
        setWebViewClient(binding.webView35)
        if (localizacao == "D"){
            val fotoUri = Uri.parse(id_file)
            val inputStream = contentResolver.openInputStream(fotoUri)
            val bytes = inputStream?.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            val html = """
                    <html>
                    <body style="margin:0;padding:0;">
                        <img src="data:image/jpeg;base64,$base64" style="width:100%;height:auto;" />
                    </body>
                    </html>
                """
            binding.webView35.settings.allowFileAccess = true
            binding.webView35.settings.allowContentAccess = true
            binding.webView35.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

        } else {
            val url: String = "https://drive.google.com/uc?export=view&id=${id_file}"
            binding.webView35.loadUrl(url)
        }
    }

    private fun inicializarTooBar(){
        binding.ToolBar35.title = "Controle De Ativos"
        binding.ToolBar35.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar35.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar35.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )

        binding.ToolBar35.inflateMenu(R.menu.menu_login)
        binding.ToolBar35.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_cancel -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_OK,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }

    }
    private fun setWebViewClient(webView: WebView?) {
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.llProgress35.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.llProgress35.visibility = View.GONE
            }
        }
    }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}