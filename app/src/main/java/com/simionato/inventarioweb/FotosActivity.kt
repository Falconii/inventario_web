package com.simionato.inventarioweb
/*
   pegar a oritentação da foto
   https://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
 */
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.simionato.inventarioweb.dao.daoFotoUpload
import com.simionato.inventarioweb.databinding.ActivityFotosBinding
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.global.ParametroGlobal.Dados
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.global.ParametroGlobal.EstadoUpload
import com.simionato.inventarioweb.global.getFileFromUri
import com.simionato.inventarioweb.global.getHoje
import com.simionato.inventarioweb.global.showToast
import com.simionato.inventarioweb.infra.DatabaseHelper
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.UUID


class FotosActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFotosBinding.inflate(layoutInflater)
    }

    private val daoFoto by lazy {
        daoFotoUpload(DatabaseHelper(applicationContext));
    }

    private val isExternalStorageReadOnly: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            true
        } else {
            false
        }
    }

    private val isExternalStorageAvailable: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            true
        } else{
            false
        }
    }

    private var requestCamara: ActivityResultLauncher<String>? = null

    private val  resultFoto = registerForActivityResult(ActivityResultContracts.TakePicture()){
        if (it) {
            try {
                Log.i("zyzz","Trocando a Imagem Da Tela...")
                binding.imView20.setImageURI(imageUri)
            } catch (error:Exception){
                showToast(applicationContext,"Erro Ao Mostrar A Foto!: ${error.message}")
                finish()
            }
            showFormulario(true)
        } else {
            binding.imView20.setImageURI(null)
            showFormulario(false)
        }
    }
    lateinit var imageUri : Uri

    private val requestGaleria = registerForActivityResult(ActivityResultContracts.RequestPermission()){ permissao ->
        if (permissao){
                resultGaleria.launch(Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        } else {
           showDialogPermissao()        }
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

    private var descricao:String = ""

    private var origem:String = ""

    private var save_local:Boolean = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ParametroGlobal.Ambiente.itsOK()){
            showToast(applicationContext,"Ambiente Incorreto!!")
            finish()
            return
        }
        try {
            val bundle = intent.extras
            if (bundle != null) {
                id_imobilizado = bundle.getInt("id_imobilizado", 0)
                descricao = bundle.getString("descricao")!!
            } else {
                showToast(applicationContext,"Parâmetro Foto Incorreto!!")
                finish()
            }
        }  catch (error:Exception){
            showToast(applicationContext,"Erro Nos Parametros: ${error.message}")
            finish()
        }
        setContentView(binding.root)
        if (id_imobilizado == 0){
            showToast(applicationContext,"Não Foi Informado O Código Do Imobilizado!");
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
            return
        }

        binding.llProgress20.visibility = View.GONE
        showFormulario(false)
        iniciar()
    }
    private fun createImageUri() : Uri? {
        try {
            val image = File(applicationContext.filesDir, "camera_foto.jpg")
            return FileProvider.getUriForFile(
                applicationContext,
                "com.simionato.inventarioweb.fileProvider",
                image
            )
        } catch (error:Exception){
            showToast(applicationContext,"Falha: createImageUri ${error.message} ")
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
            .setMessage("Precisamos Do Acesso A Gakeria Do Dispositvo. Deseja Liberar Agora ?")
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
        requestCamara = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                resultFoto.launch(imageUri)
            } else {
                Toast.makeText(this,"Permissão Negada",Toast.LENGTH_SHORT).show()
            }
        }
        inicializarTooBar()
        imageUri = createImageUri()!!
        binding.txtViewSituacao20.setText(ParametroGlobal.prettyText.ambiente_produto(id_imobilizado,descricao))
        binding.editUsuario20.setText(ParametroGlobal.Dados.usuario.razao)
        binding.textAjudaGaleria20.setText(ParametroGlobal.prettyText.tituloDescricao("Atenção!","Utilize O Botão Acima Para Importar As Fotos Da Galeria Do Celular",true))
        binding.textAjudaCamera20.setText(ParametroGlobal.prettyText.tituloDescricao("Atenção!","Utilize O Botão Acima Para Importar As Fotos Da Câmera Do Celular",true))

        binding.txtInputObs.filters += InputFilter.AllCaps()
        binding.swDestaque20.isChecked = false
        binding.swDestaque20.setText("Foto Não Está Em Destaque")
        binding.swDestaque20.setOnClickListener {
            binding.swDestaque20.setText(if (binding.swDestaque20.isChecked) "Foto Está Em Destaque" else "Foto Não Está Em Destaque" )
        }

        binding.btGravarNuvem20.setOnClickListener {
            uploadFotoNuvem()

        }
        binding.btGravarLocal20.setOnClickListener {
            registroFotoUpload()
        }
        binding.btCancelar20.setOnClickListener {
            val returnIntent: Intent = Intent()
            setResult(Activity.RESULT_CANCELED,returnIntent)
            finish()
        }


        storageItsOk();

    }
    private fun inicializarTooBar(){
        binding.ToolBar20.title = "Controle De Ativos"
        binding.ToolBar20.subtitle = Inventario.descricao
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
                    origem = "CAMERA"
                    binding.imView20.setImageURI(null)
                    requestCamara?.launch(android.Manifest.permission.CAMERA)
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
    private fun uploadFotoNuvem(){
        binding.btGravarNuvem20.setEnabled(false)
        binding.btGravarLocal20.setEnabled(false)
        binding.btCancelar20.setEnabled(false)
        if (origem == "GALERIA"){
            if (!save_local){
                uploadFoto_galeriaV2()
            } else {
                showToast(applicationContext,"Aparentemente Esta Foto Já Foi Gravada!!! Verifique")
                binding.btCancelar20.setEnabled(true)
            }
        } else {
            if (!save_local) {
                uploadFoto_camera_v2()
            } else {
                showToast(applicationContext,"Aparentemente Esta Foto Já Foi Gravada!!! Verifique")
                binding.btCancelar20.setEnabled(true)
            }
        }
    }
/*
    private fun uploadFotoLocal(){
        binding.btGravarNuvem20.setEnabled(false)
        binding.btGravarLocal20.setEnabled(false)
        binding.btCancelar20.setEnabled(false)
        if (origem == "GALERIA"){
            if (!save_local){
                uploadFoto_galeriaV2()
            } else {
                showToast(applicationContext,"Aparentemente Esta Foto Já Foi Gravada!!! Verifique")
                binding.btCancelar20.setEnabled(true)
            }
        } else {
            if (!save_local) {
                //uploadFoto_camera()
            } else {
                showToast(applicationContext,"Aparentemente Esta Foto Já Foi Gravada!!! Verifique")
                binding.btCancelar20.setEnabled(true)
            }
        }
    }



    private fun uploadFoto_galeria(){
        try {
            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"

            var file = saveLocalGaleria(fileName)

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.empresa.id.toString())

            val id_local = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM, Inventario.codigo.toString())

            val id_imobilizado = RequestBody.create(MultipartBody.FORM,id_imobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM,"")

            val id_file = RequestBody.create(MultipartBody.FORM,"")

            val old_name = RequestBody.create(MultipartBody.FORM,"")

            val id_usuario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM,getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM,if(binding.swDestaque20.isChecked) "S" else "N")

            val obs = RequestBody.create(MultipartBody.FORM,binding.txtInputObs.text.toString())

            val localizacao = RequestBody.create(MultipartBody.FORM,"N")

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
                    ,old_name
                    ,id_usuario
                    ,data
                    ,destaque
                    ,obs
                    ,localizacao
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

                                        showToast(applicationContext,"${mensagem.message}")

                                        val returnIntent: Intent = Intent()

                                        setResult(Activity.RESULT_OK,returnIntent)

                                        finish()


                                    } else {
                                        showToast(applicationContext,"Falha No Retorno Da Requisição!")

                                        binding.btGravarNuvem20.setEnabled(true)
                                        binding.btGravarLocal20.setEnabled(true)
                                        binding.btCancelar20.setEnabled(true)
                                    }

                                }
                                else {
                                    binding.llProgress20.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast(applicationContext,"${message.getMessage().toString()}",Toast.LENGTH_SHORT)

                                    binding.btGravarNuvem20.setEnabled(true)
                                    binding.btGravarLocal20.setEnabled(true)
                                    binding.btCancelar20.setEnabled(true)
                                }
                            }
                            else {
                                binding.llProgress20.visibility = View.GONE
                                showToast(applicationContext,"Não Foi Possivel Inserir A Foto Na Nuvem")
                                binding.btGravarNuvem20.setEnabled(true)
                                binding.btGravarLocal20.setEnabled(true)
                                binding.btCancelar20.setEnabled(true)
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress20.visibility = View.GONE
                            showToast(applicationContext,"${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress20.visibility = View.GONE
                showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
            }

        } catch (error:Exception){
            Log.e("ww","${error.message}")
            showToast(applicationContext,"Falha Ao Preparar A Foto Para Transmissão!")
        }
    }

*/
    private fun uploadFoto_galeriaV2(){
        try {
            //grava a foto na galeria\simionato

            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"
            val newImageUri = saveCompressedImageToGallery(this,uri, fileName)

            if (newImageUri == null) {
                showToast(applicationContext,"Falha Na Gravação Da Foto Na Galeria Simionato!")
                return
            }

            //val file = getFileFromUri(applicationContext, newImageUri) ?: throw Exception("Arquivo não encontrado.")
            /*
            val file = File(newImageUri?.path!!) // Obtém o caminho completo
            val filePath = file.parent ?: "" // Caminho da pasta onde o arquivo está
            val fileNameOriginal = file.name // Nome original do arquivo
            */

            val filePath = "Pictures/Simionato"
            val fileNameOriginal = fileName // já definido antes


            //Prepara registro para api nuvem
            var fotoNuvem = FotoModel()
            fotoNuvem.id_empresa			= ParametroGlobal.Dados.empresa.id
            fotoNuvem.id_local			    = ParametroGlobal.Dados.local.id
            fotoNuvem.id_inventario		    = ParametroGlobal.Dados.Inventario.codigo
            fotoNuvem.id_imobilizado		= id_imobilizado
            fotoNuvem.id_pasta			    = filePath
            fotoNuvem.id_file				= newImageUri.toString()
            fotoNuvem.file_name			    = fileNameOriginal
            fotoNuvem.file_name_original	= fileNameOriginal
            fotoNuvem.id_usuario		 	= ParametroGlobal.Dados.usuario.id
            fotoNuvem.data			 	    = getHoje()
            fotoNuvem.destaque           	= if(binding.swDestaque20.isChecked) "S" else "N"
            fotoNuvem.obs             	    = binding.txtInputObs.text.toString()
            fotoNuvem.localizacao           = "N"
            fotoNuvem.user_insert      	    = ParametroGlobal.Dados.usuario.id
            fotoNuvem.user_update     	    = 0

            lifecycleScope.launch {
                enviarFotoFlow(fotoNuvem,newImageUri).collect { estado ->
                    when (estado) {
                        is EstadoUpload.Carregando -> {
                            binding.llProgress20.visibility = View.VISIBLE
                        }
                        is EstadoUpload.Sucesso -> {
                            binding.llProgress20.visibility = View.GONE

                            showToast(applicationContext,"Foto enviada com sucesso!", Toast.LENGTH_SHORT)

                            val returnIntent: Intent = Intent()

                            setResult(Activity.RESULT_OK, returnIntent)

                            finish()
                        }
                        is EstadoUpload.Falha -> {
                            binding.llProgress20.visibility = View.GONE
                            showToast(applicationContext,estado.mensagem, Toast.LENGTH_LONG)
                        }
                    }
                }
            }



        } catch (e: Exception){
            binding.llProgress20.visibility = View.GONE
            showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }

        return
    }



    private fun showFormulario(value:Boolean){
        binding.llAjuda20.visibility = if (!value) View.VISIBLE else View.GONE
        binding.llCadastro20.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun displayName(uri: Uri): String? {
        val mCursor = applicationContext.contentResolver.query(uri, null, null, null, null)
        val indexedname = mCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        mCursor.moveToFirst()
        val filename = mCursor.getString(indexedname)
        mCursor.close()
        return filename
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);

            if (exifOrientation != null) {
                val newExif: ExifInterface = ExifInterface(fotoExternalFile)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                newExif.saveAttributes()
            }

        } catch (e: IOException) {
            showToast(applicationContext,"Falha Na Correção Da Orientação Da Foto!!");
        }

        save_local = true;
        return fotoExternalFile
    }

    private fun saveLocalGaleria(name:String): File {

        //Busca a orientação da foto

        val filesDir = applicationContext.filesDir

        val uriName = displayName(uri)

        val file = File(filesDir, uriName)

        val inputStream = contentResolver.openInputStream(uri)

        val outPutStream = FileOutputStream(file)

        inputStream!!.copyTo(outPutStream)

        Log.i("zyzz","Nome do arquivo enviado! ${file.name}")

        val oldExif = ExifInterface(file)

        val exifOrientation: String? = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)

        val filepath = "Fotos"

        var fotoExternalFile: File?=null

        fotoExternalFile = File(getExternalFilesDir(filepath), name)

        try {
            val fos: FileOutputStream = FileOutputStream(fotoExternalFile)
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);

            if (exifOrientation != null) {
                val newExif: ExifInterface = ExifInterface(fotoExternalFile)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                newExif.saveAttributes()
            }

        } catch (e: IOException) {
            showToast(applicationContext,"Falha Na Correção Da Orientação Da Foto!!");
        }

        save_local = true;

        return fotoExternalFile

    }

    private fun storageItsOk():Boolean{
        if (!isExternalStorageAvailable || isExternalStorageReadOnly) {
            return false
        }

        return true;
    }

    fun saveCompressedImageToGallery(context: Context, imageUri: Uri, fileName: String): Uri? {
        val resolver = context.contentResolver

        /* refatorado
        // Obtém o bitmap original
        val inputStream = resolver.openInputStream(imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Verifica e corrige a orientação da imagem
        val exif = resolver.openInputStream(imageUri)?.use { ExifInterface(it) }

         */

        val inputStream = resolver.openInputStream(imageUri) ?: return null
        val byteArray = inputStream.readBytes()
        inputStream.close()

        val originalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val exif = ExifInterface(ByteArrayInputStream(byteArray))

        val rotationDegrees = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        // Redimensiona a imagem para largura máxima de 1280px (mantendo proporção)
        val maxWidth = 1280
        val scaleFactor = if (rotatedBitmap.width > maxWidth) {
            maxWidth.toFloat() / rotatedBitmap.width
        } else {
            1f // não redimensiona se já for menor
        }

        val resizedBitmap = Bitmap.createScaledBitmap(
            rotatedBitmap,
            (rotatedBitmap.width * scaleFactor).toInt(),
            (rotatedBitmap.height * scaleFactor).toInt(),
            true
        )

        // Compacta a imagem redimensionada
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) // compressão moderada

        // Define os metadados para salvar na galeria
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Simionato")
        }

        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUriSaved = resolver.insert(imageCollection, contentValues) ?: return null

        // Salva a imagem compactada na galeria
        resolver.openOutputStream(imageUriSaved).use { output ->
            output?.write(outputStream.toByteArray())
        }

        return imageUriSaved
    }

    fun saveCompressedImage(context: Context, imageUri: Uri, fileName: String): File? {
        val resolver = context.contentResolver

        // Obtém o bitmap original
        val inputStream = resolver.openInputStream(imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Verifica e corrige a orientação da imagem
        val exif = resolver.openInputStream(imageUri)?.use { ExifInterface(it) }
        val rotationDegrees = when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        // Compacta a imagem uma única vez
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)

        // Define os metadados para salvar na galeria
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Simionato")
        }

        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUriSaved = resolver.insert(imageCollection, contentValues) ?: return null

        // Salva a imagem já compactada na galeria
        resolver.openOutputStream(imageUriSaved).use { output ->
            output?.write(outputStream.toByteArray()) // Agora salvamos diretamente os bytes compactados
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/Simionato/${fileName}")
        val uriNova = FileProvider.getUriForFile(context, "com.simionato.inventarioweb.fileProvider", file)


        save_local = true;

        return file
    }

    /* oficial até 04/09/2025 */
    private fun uploadFoto_camera(){
        try {

            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"

            //var file = saveCompressedImage(applicationContext,imageUri, fileName)
            //    ?:  saveLocal(fileName)

            var file = saveLocal(fileName)

            val requestFile = RequestBody.create(MultipartBody.FORM, file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val id_empresa = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.empresa.id.toString())

            val id_local = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.local.id.toString())

            val id_inventario = RequestBody.create(MultipartBody.FORM, Inventario.codigo.toString())

            val id_imobilizado = RequestBody.create(MultipartBody.FORM,id_imobilizado.toString())

            val id_pasta = RequestBody.create(MultipartBody.FORM,"")

            val id_file = RequestBody.create(MultipartBody.FORM,"")

            val old_name = RequestBody.create(MultipartBody.FORM,"")

            val id_usuario = RequestBody.create(MultipartBody.FORM,ParametroGlobal.Dados.usuario.id.toString())

            val data = RequestBody.create(MultipartBody.FORM,getHoje())

            val destaque = RequestBody.create(MultipartBody.FORM,if(binding.swDestaque20.isChecked) "S" else "N")

            val obs = RequestBody.create(MultipartBody.FORM,binding.txtInputObs.text.toString())

            val localizacao = RequestBody.create(MultipartBody.FORM,"N")

            binding.llProgress20.visibility = View.VISIBLE

            try {
                val fotoService = InfraHelper.apiInventario.create(FotoService::class.java)

                fotoService.postUploadFoto(
                    id_empresa,
                    id_local,
                    id_inventario,
                    id_imobilizado,
                    id_pasta,
                    id_file,
                    old_name,
                    id_usuario,
                    data,
                    destaque,
                    obs,
                    localizacao,
                    body
                )
                    .enqueue(object : Callback<RetornoUpload> {
                        override fun onResponse(
                            call: Call<RetornoUpload>,
                            response: Response<RetornoUpload>
                        ) {
                            binding.llProgress20.visibility = View.GONE

                            if (response != null) {

                                if (response.isSuccessful) {

                                    var mensagem = response.body()

                                    if (mensagem !== null) {

                                        try {
                                            file.delete()
                                        } catch (e: Exception) {
                                            showToast(applicationContext,
                                                "Falha Na Exclusão Da Foto!",
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        showToast(applicationContext,"${mensagem.message}")

                                        val returnIntent: Intent = Intent()

                                        setResult(Activity.RESULT_OK, returnIntent)

                                        finish()

                                    } else {
                                        showToast(applicationContext,"Falha No Retorno Da Requisição!")
                                        binding.btGravarNuvem20.setEnabled(true)
                                        binding.btGravarLocal20.setEnabled(true)
                                        binding.btCancelar20.setEnabled(true)
                                    }

                                } else {
                                    binding.llProgress20.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast(applicationContext,
                                        "${message.getMessage().toString()}",
                                        Toast.LENGTH_SHORT
                                    )
                                    binding.btGravarNuvem20.setEnabled(true)
                                    binding.btGravarLocal20.setEnabled(true)
                                    binding.btCancelar20.setEnabled(true)
                                }
                            } else {
                                binding.llProgress20.visibility = View.GONE
                                showToast(applicationContext,"Não Foi Possivel Inserir A Foto Na Nuvem")
                                binding.btGravarNuvem20.setEnabled(false)
                                binding.btGravarLocal20.setEnabled(false)
                                binding.btCancelar20.setEnabled(true)
                            }
                        }

                        override fun onFailure(call: Call<RetornoUpload>, t: Throwable) {
                            binding.llProgress20.visibility = View.GONE
                            if (t.message.toString() == "timeout") {
                                showToast(applicationContext,
                                    "Excedeu O Tempo De Espera!\nCancele E Atualize A Tela Anterior",
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                showToast(applicationContext,"${t.message.toString()}", Toast.LENGTH_LONG)
                            }
                            binding.btGravarNuvem20.setEnabled(true)
                            binding.btGravarLocal20.setEnabled(true)
                            binding.btCancelar20.setEnabled(true)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress20.visibility = View.GONE
                showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
            }


        } catch (error:Exception){
            showToast(applicationContext,"Falha Ao Preparar A Foto Para Transmissão!")
        }
    }

    private fun uploadFoto_camera_v2(){
        try {
            //grava a foto na galeria\simionato

            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"
            val newImageUri = saveCompressedImageToGallery(this, imageUri, fileName)

            if (newImageUri == null) {
                showToast(applicationContext,"Falha Na Gravação Da Foto Na Galeria Simionato!")
                return
            }

            //val file = getFileFromUri(applicationContext, newImageUri) ?: throw Exception("Arquivo não encontrado.")
            /*
            val file = File(newImageUri?.path!!) // Obtém o caminho completo
            val filePath = file.parent ?: "" // Caminho da pasta onde o arquivo está
            val fileNameOriginal = file.name // Nome original do arquivo
            */

            val filePath = "Pictures/Simionato"
            val fileNameOriginal = fileName // já definido antes


            //Prepara registro para api nuvem
            var fotoNuvem = FotoModel()
            fotoNuvem.id_empresa			= ParametroGlobal.Dados.empresa.id
            fotoNuvem.id_local			    = ParametroGlobal.Dados.local.id
            fotoNuvem.id_inventario		    = ParametroGlobal.Dados.Inventario.codigo
            fotoNuvem.id_imobilizado		= id_imobilizado
            fotoNuvem.id_pasta			    = filePath
            fotoNuvem.id_file				= newImageUri.toString()
            fotoNuvem.file_name			    = fileNameOriginal
            fotoNuvem.file_name_original	= fileNameOriginal
            fotoNuvem.id_usuario		 	= ParametroGlobal.Dados.usuario.id
            fotoNuvem.data			 	    = getHoje()
            fotoNuvem.destaque           	= if(binding.swDestaque20.isChecked) "S" else "N"
            fotoNuvem.obs             	    = binding.txtInputObs.text.toString()
            fotoNuvem.localizacao           = "N"
            fotoNuvem.user_insert      	    = ParametroGlobal.Dados.usuario.id
            fotoNuvem.user_update     	    = 0

            lifecycleScope.launch {
                enviarFotoFlow(fotoNuvem,newImageUri).collect { estado ->
                    when (estado) {
                        is EstadoUpload.Carregando -> {
                            binding.llProgress20.visibility = View.VISIBLE
                        }
                        is EstadoUpload.Sucesso -> {
                            binding.llProgress20.visibility = View.GONE

                            showToast(applicationContext,"Foto enviada com sucesso!", Toast.LENGTH_SHORT)

                            val returnIntent: Intent = Intent()

                            setResult(Activity.RESULT_OK, returnIntent)

                            finish()
                        }
                        is EstadoUpload.Falha -> {
                            binding.llProgress20.visibility = View.GONE
                            showToast(applicationContext,estado.mensagem, Toast.LENGTH_LONG)
                        }
                    }
                }
            }



        } catch (e: Exception){
                binding.llProgress20.visibility = View.GONE
                showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
        }

        return
    }


    private fun registroFotoUpload(){
        try {
            //grava a foto na galeria/simionato

            var uriFile = if(origem == "GALERIA") {uri} else {imageUri}

            val idUuid = UUID.randomUUID()
            val fileUuid = idUuid.toString()
            var fileName: String = "${Inventario.id_empresa.toString().padStart(2,'0')}_" +
                    "${Inventario.id_filial.toString().padStart(6,'0')}_" +
                    "${Inventario.codigo.toString().padStart(6,'0')}_" +
                    "${id_imobilizado.toString().padStart(6,'0')}_${fileUuid}.jpg"


            val newImageUri = saveCompressedImageToGallery(this, uriFile, fileName)

            if (newImageUri == null) {
                showToast(applicationContext,"Falha Na Gravação Da Foto Na Galeria!")
                return
            }

            /*
            val file = File(newImageUri?.path!!) // Obtém o caminho completo
            val filePath = file.parent ?: "" // Caminho da pasta onde o arquivo está
            val fileNameOriginal = file.name // Nome original do arquivo
            */

            val filePath = "Pictures/Simionato"
            val fileNameOriginal = fileName // já definido antes


            //Prepara registro para api nuvem
            var fotoNuvem = FotoModel()
            fotoNuvem.id_empresa			= ParametroGlobal.Dados.empresa.id
            fotoNuvem.id_local			    = ParametroGlobal.Dados.local.id
            fotoNuvem.id_inventario		    = ParametroGlobal.Dados.Inventario.codigo
            fotoNuvem.id_imobilizado		= ParametroGlobal.Dados.empresa.id
            fotoNuvem.id_pasta			    = filePath
            fotoNuvem.id_file				= newImageUri.toString()
            fotoNuvem.file_name			    = fileNameOriginal
            fotoNuvem.file_name_original	= fileNameOriginal
            fotoNuvem.id_usuario		 	= ParametroGlobal.Dados.usuario.id
            fotoNuvem.data			 	    = getHoje()
            fotoNuvem.destaque           	= if(binding.swDestaque20.isChecked) "S" else "N"
            fotoNuvem.obs             	= binding.txtInputObs.text.toString()
            fotoNuvem.localizacao       = "D"
            fotoNuvem.user_insert      	= ParametroGlobal.Dados.usuario.id
            fotoNuvem.user_update     	= 0

            var foto = FotoUploadModel()
            foto.idEmpresa			= ParametroGlobal.Dados.empresa.id
            foto.idLocal			= ParametroGlobal.Dados.local.id
            foto.idInventario		= ParametroGlobal.Dados.Inventario.codigo
            foto.idImobilizado		= ParametroGlobal.Dados.empresa.id
            foto.idPasta			= filePath
            foto.idFile				= newImageUri.toString()
            foto.fileName			= fileNameOriginal
            foto.fileNameOriginal	= fileNameOriginal
            foto.idUsuario		 	= ParametroGlobal.Dados.usuario.id
            foto.data			 	= getHoje()
            foto.destaque        	= if(binding.swDestaque20.isChecked) "S" else "N"
            foto.obs             	= binding.txtInputObs.text.toString()
            foto.localizacao        = "D"
            foto.descricao          = descricao
            foto.razao              =  ParametroGlobal.Dados.usuario.razao
            foto.userInsert      	= ParametroGlobal.Dados.usuario.id
            foto.userUpdate      	= 0

            try {

                val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

                fotoService.InsertFoto(fotoNuvem)
                    .enqueue(object :Callback<FotoModel>{
                        override fun onResponse(
                            call: Call<FotoModel>,
                            response: Response<FotoModel>
                        ) {
                            binding.llProgress20.visibility = View.GONE

                            if (response != null) {
                                if (response.isSuccessful) {

                                    var retorno = response.body()

                                    if (retorno !== null) {


                                        daoFoto.insertPhoto(foto)

                                        val returnIntent = Intent()

                                        setResult(Activity.RESULT_OK,returnIntent)

                                        finish()


                                    } else {
                                        showToast(applicationContext,"Falha No Retorno Da Requisição!")

                                        binding.btGravarNuvem20.setEnabled(true)
                                        binding.btGravarLocal20.setEnabled(true)
                                        binding.btCancelar20.setEnabled(true)
                                    }

                                }
                                else {
                                    binding.llProgress20.visibility = View.GONE
                                    val gson = Gson()
                                    val message = gson.fromJson(
                                        response.errorBody()!!.charStream(),
                                        HttpErrorMessage::class.java
                                    )
                                    showToast(applicationContext,"${message.getMessage().toString()}",Toast.LENGTH_SHORT)

                                    binding.btGravarNuvem20.setEnabled(true)
                                    binding.btGravarLocal20.setEnabled(true)
                                    binding.btCancelar20.setEnabled(true)
                                }
                            }
                            else {
                                binding.llProgress20.visibility = View.GONE
                                showToast(applicationContext,"Não Foi Possivel Inserir A Foto Na Nuvem")
                                binding.btGravarNuvem20.setEnabled(true)
                                binding.btGravarLocal20.setEnabled(true)
                                binding.btCancelar20.setEnabled(true)
                            }
                        }

                        override fun onFailure(call: Call<FotoModel>, t: Throwable) {
                            binding.llProgress20.visibility = View.GONE
                            showToast(applicationContext,"${t.message.toString()}", Toast.LENGTH_LONG)
                        }
                    })

            } catch (e: Exception){
                binding.llProgress20.visibility = View.GONE
                showToast(applicationContext,"${e.message.toString()}", Toast.LENGTH_LONG)
            }


        }catch (error:Exception){
            showToast(applicationContext,"Falha Ao Gravar Foto Localmente!")
        }

    }


private fun enviarFotoFlow(foto: FotoModel,newUri : Uri): Flow<EstadoUpload> = flow {
    emit(EstadoUpload.Carregando)

    try {

        val file = getFileFromUri(applicationContext, newUri) ?: throw Exception("Arquivo não encontrado.")


        val filePart = MultipartBody.Part.createFormData(
            "file", file.name,
            RequestBody.create(MultipartBody.FORM, file)
        )

        val parts = mapOf(
            "id_empresa"     to foto.id_empresa.toString(),
            "id_local"       to foto.id_local.toString(),
            "id_inventario"  to foto.id_inventario.toString(),
            "id_imobilizado" to foto.id_imobilizado.toString(),
            "id_pasta"       to foto.id_pasta,
            "id_file"        to foto.id_file,
            "file_name"       to foto.file_name_original,
            "id_usuario"     to Dados.usuario.id.toString(),
            "data"           to getHoje(),
            "destaque"       to foto.destaque,
            "obs"            to foto.obs,
            "localizacao"    to "N"
        ).mapValues { RequestBody.create(MultipartBody.FORM, it.value) }

        val service = InfraHelper.apiInventario.create(FotoService::class.java)
        val response = service.uploadfotov5_2_web(
            parts["id_empresa"]!!, parts["id_local"]!!, parts["id_inventario"]!!,
            parts["id_imobilizado"]!!, parts["id_pasta"]!!, parts["id_file"]!!,
            parts["file_name"]!!, parts["id_usuario"]!!, parts["data"]!!,
            parts["destaque"]!!, parts["obs"]!!, parts["localizacao"]!!, filePart
        )

        if (response.isSuccessful && response.body() != null) {
            try {
                try {
                    file.delete()
                } catch (e: Exception) {
                    throw Exception("Foto Enviada Com Sucesso. Mas Continua Na Galeria. Sem Problemas!")
                }
                emit(EstadoUpload.Sucesso)
            } catch (e: Exception) {
                emit(EstadoUpload.Falha("Erro ao apagar a foto na galeria: ${response.code()}"))
            }
        } else {
            logHttpError(response)
            emit(EstadoUpload.Falha("Erro ao enviar foto: ${response.code()}"))
        }

    } catch (e: Exception) {
        emit(EstadoUpload.Falha("Exceção: ${e.message}"))
    }
}.flowOn(Dispatchers.IO)

   public fun logHttpError(response: Response<RetornoUpload>): HttpErrorMessage? {
        try {
            val gson = Gson()
            val errorMsg = gson.fromJson(response.errorBody()?.charStream(), HttpErrorMessage::class.java)
            return errorMsg
        } catch (e: Exception) {
            val gson = Gson()
            val errorMsg = gson.fromJson(response.errorBody()?.charStream(), HttpErrorMessage::class.java)
            return errorMsg
        }
    }


}