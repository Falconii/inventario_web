package com.simionato.inventarioweb.services

import com.google.gson.JsonObject
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface LancamentoService {
    @GET("lancamento/{id_empresa}/{id_filial}/{id_inventario}/{id_imobilizado}")
     fun getLancamento(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_inventario") id_inventario: Int,
        @Path("id_imobilizado") id_imobilizado: Int
    ) : Call<LancamentoModel>

    @POST("lancamento")
     fun insertLancamento(
        @Body params: LancamentoModel
    ): Call<LancamentoModel>
    @PUT("lancamento")
     fun editLancamento(
        @Body params: LancamentoModel
    ): Call<LancamentoModel>

    @DELETE("lancamento/{id_empresa}/{id_filial}/{id_inventario}/{id_imobilizado}")
    fun deleteLancamento(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_inventario") id_inventario: Int,
        @Path("id_imobilizado") id_imobilizado: Int
    ) : Call<JsonObject>


}