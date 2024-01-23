package com.simionato.inventarioweb

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.simionato.inventarioweb.databinding.ActivityScanBarCodeBinding
import java.io.IOException

class ScanBarCodeActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityScanBarCodeBinding.inflate(layoutInflater)
    }
    private lateinit var barCodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCancelar04.setOnClickListener({
            val returnIntent:Intent = Intent()
            returnIntent.putExtra("codigo","")
            setResult(Activity.RESULT_OK,returnIntent)
            finish()
        })
    }

    private fun barCodeInit(){
        barCodeDetector = BarcodeDetector
            .Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this,barCodeDetector)
            .setRequestedPreviewSize(1920,1080)
            .setAutoFocusEnabled(true)
            .setFacing(CameraSource.CAMERA_FACING_BACK )
            .build()
        binding.surfaceView04!!.holder.addCallback(object : SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder) {
                try{
                     cameraSource.start(binding.surfaceView04!!.holder)
                } catch (e:IOException){
                    Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_SHORT).show()
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()
            }

        })

        barCodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                //Toast.makeText(applicationContext,"Leitura Cancelada!",Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barCodes = detections.detectedItems;
                if (barCodes.size() !=0){
                    binding.TxtBarCodeValue04!!.post{
                        binding.btnAction04!!.text = "Leitura Concluida!!!"
                        intentData = barCodes.valueAt(0).displayValue
                        binding.TxtBarCodeValue04.setText(intentData)
                        val returnIntent:Intent = Intent()
                        returnIntent.putExtra("codigo",intentData)
                        setResult(Activity.RESULT_OK,returnIntent)
                        finish()
                    }
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }
    override fun onResume() {
        super.onResume()
        barCodeInit()
    }


}