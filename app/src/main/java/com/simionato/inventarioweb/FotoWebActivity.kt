package com.simionato.inventarioweb

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.simionato.inventarioweb.databinding.ActivityFotoWebBinding
import com.simionato.inventarioweb.databinding.ActivityInventarioBinding
import java.net.URL

class FotoWebActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFotoWebBinding.inflate(layoutInflater)
    }
    private val url: String = "https://drive.google.com/uc?export=view&id=1QSfT3hO40mN-9znlqq7hmdHWQRBe2kRi"
    private val stringHtml = "<!DOCTYPE html> "+
                              "<html> " +
                                "<head> " +
                                "<meta charset=\"UTF-8\"/> " +
                                "<title>FOTO</title> " +
                                "</head> " +
                                "<body> " +
                                "<img src=\"https://drive.google.com/uc?export=view&id=1QSfT3hO40mN-9znlqq7hmdHWQRBe2kRi\" alt=\"Ativo Imobilizado\" style=\"width:auto;height:auto;\"> " +
                                "</body> " +
                               "</html> "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.webView35.getSettings().setBuiltInZoomControls(true);
        setWebViewClient(binding.webView35)
        //binding.webView35.loadUrl(url)
        binding.webView35.loadDataWithBaseURL(null,stringHtml,"text/html","utf-8",null)
    }

    private fun setWebViewClient(webView: WebView?) {
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Ativa o PreogressBar
                //progress.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Desativa o PreogressBar
                //progress.visibility = View.INVISIBLE
            }
        }
    }
}