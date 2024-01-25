package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.models.UsuarioQuery01Model
import com.simionato.inventarioweb.parametros.ParametroUsuario01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioService {
    @GET("usuario/{id_empresa}/{id}")
    fun getUsuario(
        @Path("id_empresa") id_empresa: Int,
        @Path("id") id: Int
    ) : Call<UsuarioModel>

    @POST("usuarios")
    fun getUsuarios(
        @Body params: ParametroUsuario01
    ): Call<List<UsuarioQuery01Model>>
}