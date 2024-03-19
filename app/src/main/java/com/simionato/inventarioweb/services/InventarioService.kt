package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.ResumoModel
import com.simionato.inventarioweb.parametros.ParametroInventario01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InventarioService {
    @GET("inventario/{id_empresa}/{id_filial}/{codigo}")
    fun getInventario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("codigo") codigo: Int,
    ) : Call<InventarioModel>

    @GET("resumo/{id_empresa}/{id_filial}/{codigo}")
    fun getResumo(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("codigo") codigo: Int,
    ) : Call<ResumoModel>

    @POST("inventarios")
    fun getInventarios(
        @Body params: ParametroInventario01
    ): Call<List<InventarioModel>>
}