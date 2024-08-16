package com.simionato.inventarioweb.services

import com.google.gson.JsonObject
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.parametros.parametroSendEmail01
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface emailService {
   @POST("enviaremail")
    fun sendEmail(
        @Body params: parametroSendEmail01
    ):Call<JsonObject>
}