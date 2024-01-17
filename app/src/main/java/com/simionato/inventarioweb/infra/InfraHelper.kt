package com.simionato.inventarioweb.infra

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InfraHelper {
    companion object {
        val apiInventario = Retrofit.Builder()
            .baseUrl("https://controle-inventario-backend-production.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()

        val apiTimer = Retrofit.Builder()
            .baseUrl("https://controle-inventario-backend-production.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()
    }
}