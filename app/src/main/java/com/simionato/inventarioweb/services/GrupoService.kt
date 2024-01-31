package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.GrupoModel
import com.simionato.inventarioweb.parametros.ParametroGrupo01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GrupoService {
        @GET("grupo/{id_empresa}/{id_filial}/{codigo}")
        fun getGrupo(
            @Path("id_empresa") id_empresa: Int,
            @Path("id_filial") id: Int,
            @Path("codigo") codigo: Int
        ) : Call<GrupoModel>

        @POST("grupos")
        fun getGrupos(
            @Body params: ParametroGrupo01
        ): Call<List<GrupoModel>>
}