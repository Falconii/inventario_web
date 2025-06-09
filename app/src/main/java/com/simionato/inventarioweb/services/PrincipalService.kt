package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.PrincipalModel
import com.simionato.inventarioweb.parametros.ParametroPrincipal01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PrincipalService {
    @GET("principal/{id_empresa}/{id_filial}/{codigo}")
    fun getPrincipal(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id: Int,
        @Path("codigo") codigo: Int
        ) : Call<PrincipalModel>

    @POST("principais")
    fun getPrincipais(
        @Body params: ParametroPrincipal01
    ): Call<List<PrincipalModel>>
}