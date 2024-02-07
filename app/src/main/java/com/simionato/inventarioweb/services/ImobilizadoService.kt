package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ImobilizadoModel
import com.simionato.inventarioweb.parametros.ParametroImobilizado01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ImobilizadoService {
    @GET("imobilizado/{id_empresa}/{id_filial}/{codigo}")
    fun getInventario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("codigo") codigo: Int,
    ) : Call<ImobilizadoModel>

    @POST("imobilizado")
    fun postInventario(
        @Body params: ParametroImobilizado01
    ): Call<List<ImobilizadoModel>>

    @POST("imobilizado_inv")
    fun postImobilizadoInventario(
        @Body params: ImobilizadoModel,
        @Query("inventario") inventario:Int
    ): Call<ImobilizadoModel>

}