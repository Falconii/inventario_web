package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.models.UsuarioQuery01Model
import com.simionato.inventarioweb.parametros.ParametroUsuario01
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioService {
    @GET("usuario/{id_empresa}/{id}")
    suspend fun getUsuario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id") id: Int
    ) : Response<UsuarioModel>

    @POST("usuarios")
    suspend fun getUsuarios(
        @Body params: ParametroUsuario01
    ): Response<List<UsuarioQuery01Model>>
}