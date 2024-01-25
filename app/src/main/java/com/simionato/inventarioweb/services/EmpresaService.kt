package com.simionato.inventarioweb.services

import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.LancamentoModel
import com.simionato.inventarioweb.parametros.ParametroEmpresa01
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EmpresaService {
    @GET("empresa/{id}")
    fun getEmpresa(
        @Path("id") id: Int,
    ) : Call<EmpresaModel>

    @POST("empresas")
    fun getEmpresas(
        @Body params: ParametroEmpresa01
    ) : Call<List<EmpresaModel>>
}



