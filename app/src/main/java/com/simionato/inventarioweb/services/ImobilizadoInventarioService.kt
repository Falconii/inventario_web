package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ImobilizadoInventarioService {
    @GET("imobilizadoinventario/{id_empresa}/{id_filial}/{id_inventario}/{id_imobilizado}")
    suspend fun getUsuario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_inventario") id_inventario: Int,
        @Path("id_imobilizado") id_imobilizado: Int
    ) : Response<ImobilizadoinventarioModel>

    @POST("imobilizadosinventarios")
    suspend fun getImobilizadosInventarios(
        @Body params: ParametroImobilizadoInventario01
    ): Response<List<ImobilizadoinventarioModel>>
}