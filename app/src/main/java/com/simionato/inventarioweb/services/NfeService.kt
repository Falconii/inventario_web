package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.NfeModel
import com.simionato.inventarioweb.parametros.ParametroNfe02
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NfeService {
    @GET("nfebyimobilizado/{id_empresa}/{id_filial}/{id_imobilizado}/{nfe}/{serie}/{item}")
    fun getNfebyimobilizado(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_imobilizado") id_imobilizado: Int,
        @Path("nfe") nfe: String,
        @Path("serie") serie: String,
        @Path("item") item: String,
    ) : Call<List<NfeModel>>

    @POST("nfegetone")
    fun getNfeOne(
        @Body params: ParametroNfe02
    ): Call<NfeModel>
}