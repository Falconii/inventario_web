package com.simionato.inventarioweb.services

import com.google.gson.JsonObject
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.parametros.ParametroFoto01
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FotoService {
    @POST("fotos")
    fun getFotos(
        @Body params: ParametroFoto01
    ): Call<List<FotoModel>>

    @POST("deleteuploadfoto")
    fun DeleteFoto(
        @Body params: FotoModel
    ): Call<JsonObject>

    @Multipart
    @POST("uploadfoto")
    fun postUploadFoto(
        @Part("id_empresa") id_empresa: RequestBody,
        @Part("id_local") id_local: RequestBody,
        @Part("id_inventario") id_inventario: RequestBody,
        @Part("id_imobilizado") id_imobilizado: RequestBody,
        @Part("id_pasta") id_pasta: RequestBody,
        @Part("id_file") id_file: RequestBody,
        @Part("id_usuario") id_ususario: RequestBody,
        @Part("data") data: RequestBody,
        @Part("destaque") destaque: RequestBody,
        @Part("obs") obs: RequestBody,
        @Part file: MultipartBody.Part,

        ): Call<RetornoUpload>
}