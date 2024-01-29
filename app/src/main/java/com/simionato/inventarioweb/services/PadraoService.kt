package com.simionato.inventarioweb.services

import com.google.gson.JsonObject
import com.simionato.inventarioweb.models.PadraoModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PadraoService {
    @GET("padrao/{id_empresa}/{id_usuario}")
    fun getPadrao(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_usuario") id_usuario: Int,
    ) : Call<PadraoModel>

    @POST("padrao")
    fun insertPadrao(
        @Body params: PadraoModel
    ): Call<PadraoModel>

    @POST("padraoinsertupdate")
    fun insertUpdatePadrao(
        @Body params: PadraoModel
    ): Call<PadraoModel>

    @PUT("padrao")
    fun editPadrao(
        @Body params: PadraoModel
    ): Call<PadraoModel>

    @DELETE("padrao/{id_empresa}/{id_usuario}")
    fun deletePadrao(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_usuario") id_usuario: Int,
    ) : Call<JsonObject>

}