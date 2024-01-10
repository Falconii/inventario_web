package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.UsuarioModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EmpresaAPI {
    @GET("empresa/{id}")
    suspend fun getEmpresa(
        @Path("id") id: Int,
    ) : Response<EmpresaModel>
}