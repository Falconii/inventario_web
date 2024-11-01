package com.simionato.inventarioweb.infra

import com.simionato.inventarioweb.global.ParametroGlobal
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//.baseUrl("https://simionatoativobackend-production.up.railway.app/api/")

//.baseUrl("http://192.168.0.134:3000/api/")
class InfraHelper {
    companion object {
        val apiInventario = Retrofit.Builder()
            .baseUrl(ParametroGlobal.Dados.url_local_copper)
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()

        val apiTimer = Retrofit.Builder()
            .baseUrl("https://controle-inventario-backend-production.up.railway.app/api/")
            .addConverterFactory( GsonConverterFactory.create() )//json ou XML
            .build()
    }
}