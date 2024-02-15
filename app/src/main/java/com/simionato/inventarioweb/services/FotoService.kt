package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.parametros.ParametroFoto01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FotoService {
    @POST("fotos")
    fun getFotos(
        @Body params: ParametroFoto01
    ): Call<List<FotoModel>>
}