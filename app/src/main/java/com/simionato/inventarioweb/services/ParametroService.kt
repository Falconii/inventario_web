package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ParametroModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ParametroService {

    @GET("parametro/{id_empresa}/{modulo}/{assinatura}/{id_usuario}")
    fun getParametro(
        @Path("id_empresa") id_empresa: Int,
        @Path("modulo") modulo: String,
        @Path("assinatura") assinatura: String,
        @Path("id_usuario") id_usuario: Int
    ) : Call<ParametroModel>

    @POST("atualizarparametro")
    fun postParametroAtualiza(
        @Body params: ParametroModel
    ):  Call<ParametroModel>
}
