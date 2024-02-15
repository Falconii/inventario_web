package com.simionato.inventarioweb.infra

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//.baseUrl("https://simionatoativobackend-production.up.railway.app/api/")

//.baseUrl("192.168.15.13:3000/api/")
class InfraHelper {
    companion object {
        val apiInventario = Retrofit.Builder()
            .baseUrl("https://simionatoativobackend-production.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()

        val apiTimer = Retrofit.Builder()
            .baseUrl("https://controle-inventario-backend-production.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()
    }
}