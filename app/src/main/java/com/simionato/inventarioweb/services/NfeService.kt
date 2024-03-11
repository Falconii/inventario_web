package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.NfeModel
import com.simionato.inventarioweb.parametros.ParametroNfe02
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface NfeService {
    @POST("nfegetone")
    fun getNfeOne(
        @Body params: ParametroNfe02
    ): Call<NfeModel>
}