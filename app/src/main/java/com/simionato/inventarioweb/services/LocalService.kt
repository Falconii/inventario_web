package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.models.UsuarioQuery01Model
import com.simionato.inventarioweb.parametros.ParametroLocal01
import com.simionato.inventarioweb.parametros.ParametroUsuario01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LocalService {
    @GET("local/{id_empresa}/{id}")
    suspend fun getLocal(
        @Path("id_empresa") id_empresa: Int,
        @Path("id") id: Int
    ) : Response<LocalModel>

    @POST("locais")
    suspend fun getLocais(
        @Body params: ParametroLocal01
    ): Response<List<LocalModel>>

    //
    @GET("local/{id_empresa}/{id}")
    fun getLocalv2(
        @Path("id_empresa") id_empresa: Int,
        @Path("id") id: Int
    ) : Call<LocalModel>

    @POST("locais")
    fun getLocaisV2(
        @Body params: ParametroLocal01
    ): Call<List<LocalModel>>
}