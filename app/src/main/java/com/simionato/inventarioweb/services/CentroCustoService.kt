package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.CentroCustoModel
import com.simionato.inventarioweb.parametros.ParametroCentroCusto01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CentroCustoService {
    @GET("centrocusto/{id_empresa}/{id_filial}/{codigo}")
    fun getCentroCusto(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id: Int,
        @Path("codigo") codigo: Int
        ) : Call<CentroCustoModel>

    @POST("centroscustos")
    fun getCentrosCustos(
        @Body params: ParametroCentroCusto01
    ): Call<List<CentroCustoModel>>
}