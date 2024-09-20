package com.simionato.inventarioweb

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateCheckActivity : AppCompatActivity() {

    private val updateUrl = "https://github.com/Falconii/arquivos_apk/releases/download/apk/app-release.apk"

    private lateinit var progressBar: ProgressBar
    private lateinit var statusMessage: TextView
    private lateinit var restoreButton: Button
    private lateinit var checkUpdateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_check)

        progressBar = findViewById(R.id.progress_bar)
        statusMessage = findViewById(R.id.status_message)
        restoreButton = findViewById(R.id.restore_button)
        checkUpdateButton = findViewById(R.id.check_update_button)

        statusMessage.text = "Você está na versão mais recente."

        restoreButton.setOnClickListener {
            val backupApk = File(cacheDir, "app-backup.apk")
            if (backupApk.exists()) {
                Log.d("UpdateCheckActivity", "Restaurando versão anterior.")
                restorePreviousVersion(backupApk)
            } else {
                Log.d("UpdateCheckActivity", "Backup não encontrado.")
                showToast("Backup não encontrado")
            }
        }

        checkUpdateButton.setOnClickListener {
            Log.d("UpdateCheckActivity", "Botão de verificação de atualizações clicado.")
            checkForUpdates()
        }
    }

    private fun checkForUpdates() {
        showProgressBar(true)
        statusMessage.text = "Fazendo backup do aplicativo atual..."

        val apkBackup = File(cacheDir, "app-backup.apk")
        backupCurrentApk(apkBackup) { success ->
            if (success) {
                Log.d("UpdateCheckActivity", "Backup concluído. Verificando atualizações...")
                checkForNewUpdates()
            } else {
                showProgressBar(false)
                statusMessage.text = "Falha ao fazer backup do aplicativo."
                showToast("Falha ao fazer backup do aplicativo")
            }
        }
    }

    private fun checkForNewUpdates() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(updateUrl)
            .head()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("UpdateCheckActivity", "Falha ao verificar atualizações.", e)
                runOnUiThread {
                    showProgressBar(false)
                    statusMessage.text = "Falha ao verificar atualizações."
                    showToast("Falha ao verificar atualizações")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                Log.d("UpdateCheckActivity", "Resposta recebida com código: ${response.code}")
                if (response.isSuccessful && response.code == 200) {
                    Log.d("UpdateCheckActivity", "Arquivo APK encontrado. Iniciando download...")
                    runOnUiThread {
                        statusMessage.text = "Novo APK encontrado. Iniciando download..."
                        downloadAndInstallApk(updateUrl)
                    }
                } else {
                    Log.d("UpdateCheckActivity", "Arquivo APK não encontrado ou resposta inválida.")
                    runOnUiThread {
                        showProgressBar(false)
                        statusMessage.text = "Você está na versão mais recente."
                        showToast("Atualização não disponível")
                    }
                }
            }
        })
    }

    private fun backupCurrentApk(backupFile: File, callback: (Boolean) -> Unit) {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val apkPath = packageInfo.applicationInfo.sourceDir
            val inputStream = FileInputStream(apkPath)
            val outputStream = FileOutputStream(backupFile)

            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d("UpdateCheckActivity", "Backup do APK concluído.")
            callback(true)
        } catch (e: IOException) {
            Log.e("UpdateCheckActivity", "Falha ao fazer backup do APK.", e)
            callback(false)
        }
    }

    private fun downloadAndInstallApk(apkUrl: String) {
        Log.d("UpdateCheckActivity", "Iniciando download do APK de $apkUrl.")
        Thread {
            try {
                val file = File(cacheDir, "app-debug.apk")
                val url = URL(apkUrl)
                val connection = url.openConnection()
                connection.connect()

                val inputStream: InputStream = connection.getInputStream()
                val outputStream: OutputStream = file.outputStream()

                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                Log.d("UpdateCheckActivity", "Download do APK concluído. Solicitando confirmação para instalação...")
                runOnUiThread {
                    showInstallConfirmationDialog(file)
                }

            } catch (e: IOException) {
                Log.e("UpdateCheckActivity", "Falha no download do APK.", e)
                runOnUiThread {
                    showToast("Falha no download")
                    showProgressBar(false)
                }
            }
        }.start()
    }

    private fun showInstallConfirmationDialog(apkFile: File) {
        val apkName = apkFile.name
        AlertDialog.Builder(this)
            .setTitle("Confirmar Instalação")
            .setMessage("Deseja instalar o APK: $apkName?")
            .setPositiveButton("Instalar") { _, _ ->
                requestInstallPermission(apkFile)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                showToast("Instalação cancelada")
                showProgressBar(false) // Esconde a barra de progresso caso a instalação seja cancelada
            }
            .create()
            .show()
    }

    private fun requestInstallPermission(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                installApk(apkFile)
            } else {
                Log.d("UpdateCheckActivity", "Solicitando permissão para instalar pacotes.")
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, REQUEST_INSTALL_PACKAGES)
            }
        } else {
            installApk(apkFile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INSTALL_PACKAGES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.canRequestPackageInstalls()) {
                installApk(File(cacheDir, "app-debug.apk"))
            } else {
                Log.d("UpdateCheckActivity", "Permissão de instalação negada.")
                showToast("Permissão de instalação negada")
                showProgressBar(false) // Esconde a barra de progresso se a permissão for negada
            }
        }
    }

    private fun installApk(file: File) {
        if (file.exists()) {
            Log.d("UpdateCheckActivity", "Instalando APK de ${file.absolutePath}")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        this@UpdateCheckActivity,
                        "${packageName}.fileprovider",
                        file
                    )
                } else {
                    Uri.fromFile(file)
                }
                setDataAndType(fileUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else {
            Log.e("UpdateCheckActivity", "Arquivo APK não encontrado: ${file.absolutePath}")
            showToast("Arquivo APK não encontrado")
            showProgressBar(false) // Esconde a barra de progresso se o arquivo APK não for encontrado
        }
    }

    private fun restorePreviousVersion(backupApk: File) {
        Log.d("UpdateCheckActivity", "Restaurando versão anterior do APK de ${backupApk.absolutePath}")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this@UpdateCheckActivity,
                    "${packageName}.fileprovider",
                    backupApk
                )
            } else {
                Uri.fromFile(backupApk)
            }
            setDataAndType(fileUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun showProgressBar(visible: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_INSTALL_PACKAGES = 100
    }
}
