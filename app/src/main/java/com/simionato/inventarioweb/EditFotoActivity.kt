package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.databinding.ActivityEditFotoBinding
import com.simionato.inventarioweb.databinding.ActivityShowFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.services.ImobilizadoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.Date
import java.util.UUID

class EditFotoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditFotoBinding.inflate(layoutInflater)
    }
    
    private var foto:FotoModel = FotoModel()


    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        if (it) {
            binding.imView42.setImageURI(imageUri)
            binding.llTrocaImagem.visibility = View.VISIBLE
        }
    }
    lateinit var imageUri : Uri

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
            binding.imView42.setImageBitmap(bitMap)
            uri = result.data?.data!!
            binding.llTrocaImagem.visibility = View.VISIBLE
        }
    }

    private fun createImageUri() : Uri? {
        try {
            val image = File(applicationContext.filesDir, "camera_foto.png")
            return FileProvider.getUriForFile(
                applicationContext,
                "com.simionato.inventarioweb.fileProvider",
                image
            )
        } catch (error:Exception){
            showToast("Falha: createImageUri ${error.message} ")
        }
        return null
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


    private val isExternalStorageAvailable: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            true
        } else{
            false
        }
    }

    private val isExternalStorageReadOnly: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            true
        } else {
            false
        }
    }

    private lateinit var  dialog: AlertDialog

    private lateinit var uri: Uri

    private var origem:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.llProgress42.visibility = View.GONE
        try {
            val bundle = intent.extras
            Log.i("zyzz", "Cheguei fotoedicao ${bundle}")
            if (bundle != null) {
                foto = if (Build.VERSION.SDK_INT >= 33) bundle.getParcelable(
                    "foto",
                    FotoModel::class.java
                )!!
                else bundle.getParcelable("foto")!!
            } else {
                showToast("Parâmetro Da Foto Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }

        Log.i("zyzz","${foto}")
        iniciar()
    }

    private fun iniciar(){

        inicializarTooBar()
        binding.llTrocaImagem.visibility = View.GONE
        imageUri = createImageUri()!!
        binding.txtViewSituacao42.setText(ParametroGlobal.prettyText.ambiente_produto(foto.id_imobilizado,foto.imo_descricao))
        binding.editUsuario42.setText(ParametroGlobal.Dados.usuario.razao)
        try {
            val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")

            // val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")
            val thumbnailUrl = URL("https://drive.google.com/thumbnail?id=${foto.id_file}&sz=w300")
            Glide.with(applicationContext)
                .load(thumbnailUrl)
                .placeholder(R.drawable.no_foto)
                .into(binding.imView42);

        } catch (e:Exception){
            Log.i("zyzz","Erro-> ${e.message}")
        }
        binding.txtInputObs.filters += InputFilter.AllCaps()
        binding.swDestaque42.isChecked = if (foto.destaque == "S") true else false
        binding.swDestaque42.setText(if (binding.swDestaque42.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        binding.swDestaque42.setOnClickListener {
            binding.swDestaque42.setText(if (binding.swDestaque42.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        }
        binding.txtInputObs.setText(foto.obs)
        binding.ivTrocaImagem42.setOnClickListener {
            try {
               // val url = URL("https://drive.google.com/uc?export=view&id=${foto.id_file}")
                val thumbnailUrl = URL("https://drive.google.com/thumbnail?id=${foto.id_file}&sz=w300")
                Glide.with(applicationContext)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.no_foto)
                    .into(binding.imView42);
                binding.llTrocaImagem.visibility = View.GONE
            } catch (e:Exception){
                Log.i("zyzz","Erro-> ${e.message}")
            }
        }
        binding.btGravar42.setOnClickListener {
            if (binding.llTrocaImagem.visibility == View.VISIBLE) {
                uploadFoto()
            } else {
                updateFoto(foto)
            }
        }
        binding.btCancelar42.setOnClickListener {
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
        }
       
    }

    private fun inicializarTooBar(){
        binding.ToolBar42.title = "Controle De Ativos"
        binding.ToolBar42.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar42.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar42.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar42.inflateMenu(R.menu.menu_galeria)
        binding.ToolBar42.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){
                R.id.item_galeria_exit -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_galeria_camera -> {
                    origem = "CAMERA"
                    contract.launch(imageUri)
                    return@setOnMenuItemClickListener true
                }
                R.id.item_galeria_galeria -> {
                    origem = "GALERIA"
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
        if (origem == "GALERIA"){
            uploadFoto_galeria()
        } else {
            uploadFoto_camera()
        }
    }
    private fun uploadFoto_galeria(){
        try {

            val filesDir = applicationContext.filesDir

            val uriName = displayName(uri)

            val file = File(filesDir, uriName)

            val inputStream = contentResolver.openInputStream(uri)

            val outPutStream = FileOutputStream(file)

            inputStream!!.copyTo(outPutStream)

            Log.i("zyzz","Nome do arquivo enviado! ${file.name}")

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.empresa.id.toString())

            val id_local = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.Inventario.codigo.toString())

            val id_imobilizado = RequestBody.create(MultipartBody.FORM,foto.id_imobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM,"")

            val id_file = RequestBody.create(MultipartBody.FORM,"")

            val old_name = RequestBody.create(MultipartBody.FORM,foto.file_name_original)

            val id_usuario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM,getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM,if(binding.swDestaque42.isChecked) "S" else "N")

            val obs = RequestBody.create(MultipartBody.FORM,binding.txtInputObs.text.toString())

            binding.llProgress42.visibility = View.VISIBLE

            try {
                val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

                fotoService.postUploadFoto(
                    id_empresa
                    ,id_local
                    ,id_inventario
                    ,id_imobilizado
                    ,id_pasta
                    ,id_file
                    ,old_name
                    ,id_usuario
                    ,data
                    ,destaque
                    ,obs
                    ,body)
                    .enqueue(object : Callback<RetornoUpload> {
                        override fun onResponse(
                            call: Call<RetornoUpload>,
                            response: Response<RetornoUpload>
                        ) {
                            binding.llProgress42.visibility = View.GONE

                            if (response != null) {
                                if (response.isSuccessful) {

                                    var mensagem = response.body()

                                    if (mensagem !== null) {

                                        showToast("Foto ALTERADA!")

                                        deleteFoto(foto)

                                    } else {
                                        showToast("Falha No Retorno Da Requisição!")
                                    }

                                }
                                else {
                                    binding.llProgress42.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)

                                }
                            }
                            else {
                                binding.llProgress42.visibility = View.GONE
                                showToast("Não Foi Possivel Inserir A Fota Na Nuvem")
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress42.visibility = View.GONE
                            showToast("${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress42.visibility = View.GONE
                showToast("${e.message.toString()}", Toast.LENGTH_LONG)
            }


        } catch (error:Exception){
            showToast("Falha Ao Preparar A Foto Para Transmissão!")
        }
    }
    private fun uploadFoto_camera(){
        try {

            //val filesDir = applicationContext.filesDir

            //val uriName = displayName(imageUri)

            //val file = File(filesDir, uriName)

            Log.i("file_name_original",foto.file_name_original)

            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${ParametroGlobal.Dados.Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${ParametroGlobal.Dados.Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${ParametroGlobal.Dados.Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${foto.id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"

            var file = saveLocal(fileName)

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.empresa.id.toString())

            val id_local = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM, ParametroGlobal.Dados.Inventario.codigo.toString())

            val id_imobilizado = RequestBody.create(MultipartBody.FORM,foto.id_imobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM,foto.id_pasta)

            val id_file = RequestBody.create(MultipartBody.FORM,foto.id_file)

            val old_name = RequestBody.create(MultipartBody.FORM,foto.file_name_original)

            val id_usuario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM,getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM,if(binding.swDestaque42.isChecked) "S" else "N")

            val obs = RequestBody.create(MultipartBody.FORM,binding.txtInputObs.text.toString())

            binding.llProgress42.visibility = View.VISIBLE

            try {
                val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

                fotoService.postUploadFoto(
                    id_empresa
                    ,id_local
                    ,id_inventario
                    ,id_imobilizado
                    ,id_pasta
                    ,id_file
                    ,old_name
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
                            binding.llProgress42.visibility = View.GONE

                            if (response != null) {

                                if (response.isSuccessful) {

                                    var mensagem = response.body()

                                    if (mensagem !== null) {

                                        try {
                                            file.delete()
                                        } catch (e: Exception) {
                                            showToast("Falha Na Exclusão Da Foto!", Toast.LENGTH_LONG)
                                        }
                                        showToast("${mensagem.message}")

                                        val returnIntent: Intent = Intent()

                                        setResult(Activity.RESULT_OK,returnIntent)

                                        finish()

                                    } else {
                                        showToast("Falha No Retorno Da Requisição!")
                                    }

                                }
                                else {
                                    binding.llProgress42.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast("${message.getMessage().toString()}",Toast.LENGTH_SHORT)

                                }
                            }
                            else {
                                binding.llProgress42.visibility = View.GONE
                                showToast("Não Foi Possivel Inserir A Fota Na Nuvem")
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress42.visibility = View.GONE
                            showToast("override ${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress42.visibility = View.GONE
                showToast("catch ${e.message.toString()}", Toast.LENGTH_LONG)
            }


        } catch (error:Exception){
            showToast("Falha Ao Preparar A Foto Para Transmissão!")
        }
    }

    private fun displayName(uri: Uri): String? {
        val mCursor = applicationContext.contentResolver.query(uri, null, null, null, null)
        val indexedname = mCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        mCursor.moveToFirst()
        val filename = mCursor.getString(indexedname)
        mCursor.close()
        return filename
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

    private fun deleteFoto(foto:FotoModel){
        try {
            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

            fotoService.DeleteFoto(foto).enqueue( object  : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress42.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var retorno = response.body()

                            if (retorno !== null) {

                                val returnIntent: Intent = Intent()

                                setResult(Activity.RESULT_OK,returnIntent)

                                finish()

                            } else {

                                showToast("Falha No Retorno Da Requisição!")

                            }

                        }
                        else {
                            binding.llProgress42.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Tabela De Fotos Vazia")
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress42.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress42.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

            binding.llProgress42.visibility = View.VISIBLE
        }catch (e: Exception){
            binding.llProgress42.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun updateFoto(foto:FotoModel) {
        foto.destaque = if (binding.swDestaque42.isChecked) "S" else "N"
        foto.obs = binding.txtInputObs.text.toString()
        try {

            val fotoService =
                InfraHelper.apiInventario.create(FotoService::class.java)
            fotoService.putFoto(foto).enqueue(object :Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress42.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {
                            var fotox = response.body()
                            if (fotox != null) {
                                showToast("Foto Atualizada!")
                                val returnIntent: Intent = Intent()
                                setResult(Activity.RESULT_OK,returnIntent)
                                finish()
                            } else {
                                showToast("Falha Na Atualização Da Foto")
                            }
                        } else {
                            binding.llProgress42.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                        }
                    } else {
                        binding.llProgress42.visibility = View.GONE
                        showToast("Sem retorno Da Requisição!")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress42.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        } catch (e: Exception) {
            binding.llProgress42.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun saveLocal(name:String): File {

        //Busca a orientação da foto

        val filesDir = applicationContext.filesDir

        val uriName = displayName(imageUri)

        val file = File(filesDir, uriName)

        val oldExif = ExifInterface(file)

        val exifOrientation: String? = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)

        val filepath = "Fotos"

        var fotoExternalFile: File?=null

        fotoExternalFile = File(getExternalFilesDir(filepath), name)

        try {
            val fos: FileOutputStream = FileOutputStream(fotoExternalFile)
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);

            if (exifOrientation != null) {
                val newExif: ExifInterface = ExifInterface(fotoExternalFile)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                newExif.saveAttributes()
            }

        } catch (e: IOException) {
            showToast("Falha Na Correção Da Orientação Da Foto!!");
        }


        return fotoExternalFile
    }
    private fun storageItsOk():Boolean{
        if (!isExternalStorageAvailable || isExternalStorageReadOnly) {
            return false
        }

        return true;
    }

}