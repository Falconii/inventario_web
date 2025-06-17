package com.simionato.inventarioweb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import java.util.concurrent.Executors
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException


// Para controle da câmera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

// Para interação com toques (gestos de zoom)
import android.view.MotionEvent

// Para verificar permissões e acessar recursos
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

// Para salvar e manipular imagens
import android.net.Uri
import android.os.Environment
import java.io.File

// Para definir rotação correta da imagem
import android.view.Display
import android.view.WindowManager
class FotoTesteActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var cancelButton: Button


    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto_teste)


        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        cancelButton = findViewById(R.id.cancelButton)



        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        captureButton.setOnClickListener { capturePhoto() }
        cancelButton.setOnClickListener { finish() } // Fecha a Activity ao cancelar
    }

    private fun startCamera() {val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                // Inicializa o cameraControl corretamente
                val cameraControl = camera.cameraControl

                // Agora você pode usar cameraControl no evento de toque
                previewView.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            val zoomRatio = event.pointerCount * 0.1f
                            cameraControl.setZoomRatio(
                                zoomRatio.coerceIn(
                                    1.0f,
                                    camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1.0f
                                )
                            )
                        }
                    }
                    true
                }

            } catch (e: Exception) {
                Log.e("CameraX", "Erro ao iniciar a câmera", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val timestamp = System.currentTimeMillis()
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Marcos"
        )
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "photo_$timestamp.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(file)

                    // Atualiza galeria
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

                    // Mostra na ImageView
                    imageView.setImageURI(uri)
                }

                override fun onError(ex: ImageCaptureException) {
                    Log.e("CameraX", "Erro ao capturar foto", ex)
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}