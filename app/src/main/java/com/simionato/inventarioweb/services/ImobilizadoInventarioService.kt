package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.models.RetornoUpload
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImobilizadoInventarioService {
    @GET("imobilizadoinventario/{id_empresa}/{id_filial}/{id_inventario}/{id_imobilizado}")
    fun getImobilizadoInventario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id_filial") id_filial: Int,
        @Path("id_inventario") id_inventario: Int,
        @Path("id_imobilizado") id_imobilizado: Int
    ) : Call<ImobilizadoinventarioModel>

    @POST("imobilizadosinventarios")
    fun getImobilizadosInventarios(
        @Body params: ParametroImobilizadoInventario01
    ): Call<List<ImobilizadoinventarioModel>>

    @Multipart
    @POST("imobilizadoinventariofoto")
    fun postImobilizadoInventarioFoto(
        @Part("id_empresa") id_empresa: RequestBody,
        @Part("id_local") id_local: RequestBody,
        @Part("id_inventario") id_inventario: RequestBody,
        @Part("id_usuario") id_ususario: RequestBody,
        @Part file: MultipartBody.Part,

        ): Call<RetornoUpload>
}