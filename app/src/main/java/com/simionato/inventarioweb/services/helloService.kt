package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.mensagemModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface HelloService {
    //
    @GET("hello")
    fun hellol() : Call<mensagemModel>

    @GET("splash")
    fun splash(): Call<mensagemModel>
}