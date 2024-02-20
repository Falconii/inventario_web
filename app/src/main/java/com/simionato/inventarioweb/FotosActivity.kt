package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.simionato.inventarioweb.databinding.ActivityFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.services.ImobilizadoInventarioService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class FotosActivity : AppCompatActivity() {



    private val binding by lazy {
        ActivityFotosBinding.inflate(layoutInflater)
    }

    private val requestGaleria = registerForActivityResult(ActivityResultContracts.RequestPermission()){ permissao ->
        if (permissao){
                resultGaleria.launch(Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        } else {
           showDialogPermissao()
        }
    }

    private val resultGaleria = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        if (result.data?.data !== null) {
            val bitMap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    baseContext.contentResolver,
                    result.data?.data
                )
            } else {
                val source = ImageDecoder.createSource(
                    this.contentResolver,
                    result.data?.data!!
                )
                ImageDecoder.decodeBitmap(source)
            }
            binding.imView20.setImageBitmap(bitMap)
            uri = result.data?.data!!
            showFormulario(true)
        }
    }

    private lateinit var  dialog: AlertDialog

    private lateinit var uri:Uri

    private var id_imobilizado:Int = 0

    private var plaqueta:String = ""

    private var descricao:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Validando Paramentros
        if ((ParametroGlobal.Dados.usuario.id == 0) ||
            (ParametroGlobal.Dados.local.id == 0)   ||
            (ParametroGlobal.Dados.Inventario.codigo == 0)){
            showToast("Ambiente Incorreto!!")
            finish()
        }

        val bundle = intent.extras

        if (bundle != null){
            id_imobilizado =  bundle.getInt("id_imobilizado",0)
            descricao =  bundle.getString("descricao")!!
        } else {
            showToast("Parâmetro Foto Incorreto!!")
            finish()
        }
        setContentView(binding.root)
        if (id_imobilizado == 0){
            showToast("Não Foi Informado O Código Do Imobilizado!");
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
            return
        }

        binding.llProgress20.visibility = View.GONE
        showFormulario(false)
        iniciar()
    }

    private fun verificaPermissaoGaleria(){
        val permissaoGaleriaAceita = verficaPermissao(ParametroGlobal.Permissoes.PERMISSAO_GALERIA)
        when {
                permissaoGaleriaAceita -> {
                    resultGaleria.launch(Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }
                shouldShowRequestPermissionRationale(ParametroGlobal.Permissoes.PERMISSAO_GALERIA) -> showDialogPermissao()
                else -> requestGaleria.launch(ParametroGlobal.Permissoes.PERMISSAO_GALERIA)
        }
    }

    private fun verficaPermissao(permissao:String) =
        ContextCompat.checkSelfPermission(this,permissao) == PackageManager.PERMISSION_GRANTED

    private fun showDialogPermissao(){
        val builder = AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Precisamos Do Acesso A Galeria Do Dispositvo. Deseja Liberar Agora ?")
            .setNegativeButton("Não"){_,_  -> dialog.dismiss()
            }
            .setPositiveButton("Sim"){_,_ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package",packageName, null)
                )
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                dialog.dismiss()
            }
        dialog = builder.create()

        dialog.show()
    }

    private fun iniciar(){
        inicializarTooBar()
        binding.txtViewSituacao20.setText("Local: ${ParametroGlobal.Dados.Inventario.local_razao}\nInventário: ${ParametroGlobal.Dados.Inventario.descricao}\nPlaqueta: ${id_imobilizado}\nDescricao: ${descricao}")
        binding.editUsuario20.setText(ParametroGlobal.Dados.usuario.razao)
        binding.txtInputObs.filters += InputFilter.AllCaps()
        binding.swDestaque20.isChecked = false
        binding.swDestaque20.setText("Foto Não Está Em Destaque")
        binding.swDestaque20.setOnClickListener {
            binding.swDestaque20.setText(if (binding.swDestaque20.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        }

        binding.btGravar20.setOnClickListener {
            uploadFoto()
        }
    }

    private fun inicializarTooBar(){
        binding.ToolBar20.title = "Controle De Ativos"
        binding.ToolBar20.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar20.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar20.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar20.inflateMenu(R.menu.menu_galeria)
        binding.ToolBar20.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_galeria_exit -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_galeria_camera -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_galeria_galeria -> {
                    verificaPermissaoGaleria()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun uploadFoto(){
        try {

            val filesDir = applicationContext.filesDir

            val file = File(filesDir, "image.jpg")

            val inputStream = contentResolver.openInputStream(uri)

            val outPutStream = FileOutputStream(file)

            inputStream!!.copyTo(outPutStream)

            Log.i("zyzz","Nome do arquivo enviado! ${file.name}")

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.empresa.id.toString())

            val id_local = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.Inventario.codigo.toString())

            val id_imobilizado = RequestBody.create(MultipartBody.FORM,id_imobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM,"")

            val id_file = RequestBody.create(MultipartBody.FORM,"")

            val id_usuario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM,getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM,if(binding.swDestaque20.isChecked) "S" else "N")

            val obs = RequestBody.create(MultipartBody.FORM,binding.txtInputObs.text.toString())

            binding.llProgress20.visibility = View.VISIBLE

            try {
                val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

                fotoService.postUploadFoto(
                     id_empresa
                    ,id_local
                    ,id_inventario
                    ,id_imobilizado
                    ,id_pasta
                    ,id_file
                    ,id_usuario
                    ,data
                    ,destaque
                    ,obs
                    ,body)
                    .enqueue(object :Callback<RetornoUpload>{
                        override fun onResponse(
                            call: Call<RetornoUpload>,
                            response: Response<RetornoUpload>
                        ) {
                            binding.llProgress20.visibility = View.GONE

                            if (response != null) {
                                if (response.isSuccessful) {

                                    var mensagem = response.body()

                                    if (mensagem !== null) {

                                        showToast("${mensagem.message}")

                                        val returnIntent: Intent = Intent()

                                        setResult(Activity.RESULT_OK,returnIntent)

                                        finish()

                                    } else {
                                        showToast("Falha No Retorno Da Requisição!")
                                    }

                                }
                                else {
                                    binding.llProgress20.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)

                                }
                            }
                            else {
                                binding.llProgress20.visibility = View.GONE
                                showToast("Não Foi Possivel Inserir A Fota Na Nuvem")
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress20.visibility = View.GONE
                            showToast("${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress20.visibility = View.GONE
                showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            }


        } catch (error:Exception){
            showToast("Falha Ao Preparar A Foto Para Transmissão!")
        }
    }

    private fun showFormulario(value:Boolean){
        binding.llCadastro20.visibility = if (value) View.VISIBLE else View.GONE
    }

    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }

    fun getHoje():String{

        try {

            val date = Date()

            val format = SimpleDateFormat("dd/MM/yyyy")

            val data = format.format(date)

            return data

        } catch (e:Exception)
        {
            return ""
        }

    }

}