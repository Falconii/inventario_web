package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ValorModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ValorService {

    @GET("valor/{id_empresa}/{id_filial}/{id_imobilizado}")
    fun getValor(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_imobilizado") id_imobilizado: Int,
    ) : Call<ValorModel>
}