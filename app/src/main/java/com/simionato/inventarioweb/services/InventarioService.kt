package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.parametros.ParametroInventario01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InventarioService {
    @GET("inventario/{id_empresa}/{id_filial}/{codigo}")
    fun getInventarios(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("codigo") codigo: Int,
    ) : Call<InventarioModel>

    @POST("inventarios")
    fun getInventarios(
        @Body params: ParametroInventario01
    ): Call<List<InventarioModel>>
}