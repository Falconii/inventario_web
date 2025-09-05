package com.simionato.inventarioweb.global

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun getHoje():String{

    try {

        val date = Date()

        val format = android.icu.text.SimpleDateFormat("dd/MM/yyyy")

        val data = format.format(date)

        return data

    } catch (e:Exception)
    {
        return ""
    }

}


fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun formatarMoeda(valor: Double, simbolo: String = "R$"): String {
    return "$simbolo ${"%,.2f".format(valor).replace(",", "X").replace(".", ",").replace("X", ".")}"
}


fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Não foi possível abrir o InputStream do URI")

        val fileName = getFileName(context, uri) ?: "temp_file_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)

        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = it.getString(index)
            }
        }
    }

    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }

    return result
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


fun apagarFotoGaleria(context: Context, fotoUri: Uri) {
    try {
        val deletados = context.contentResolver.delete(fotoUri, null, null)
        if (deletados <= 0 ){
            throw Exception("Foto Não Excluída Da Galeria!")
        }
    } catch (e: Exception) {
        throw  Exception(e.message);
    }
}


fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val currentTime = Calendar.getInstance().time
    return dateFormat.format(currentTime)
}