package com.simionato.inventarioweb.infra

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InfraHelper {
    companion object {
        val apiTimer = Retrofit.Builder()
            .baseUrl("https://backend-controle-time.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()
    }
}