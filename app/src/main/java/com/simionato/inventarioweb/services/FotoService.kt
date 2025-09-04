    package com.simionato.inventarioweb.services

    import com.google.gson.JsonObject
    import com.simionato.inventarioweb.models.CentroCustoModel
    import com.simionato.inventarioweb.models.FotoModel
    import com.simionato.inventarioweb.models.RetornoUpload
    import com.simionato.inventarioweb.parametros.ParametroFoto01
    import okhttp3.MultipartBody
    import okhttp3.RequestBody
    import retrofit2.Call
    import retrofit2.Response
    import retrofit2.http.Body
    import retrofit2.http.DELETE
    import retrofit2.http.GET
    import retrofit2.http.Multipart
    import retrofit2.http.POST
    import retrofit2.http.PUT
    import retrofit2.http.Part
    import retrofit2.http.Path

    interface FotoService {
        @POST("fotos")
        fun getFotos(
            @Body params: ParametroFoto01
        ): Call<List<FotoModel>>

        @DELETE("foto/{id_empresa}/{id_local}/{id_inventario}/{id_imobilizado}/{id_pasta}/{id_file}/{file_name}")
        fun deleteFotoDB(
            @Path("id_empresa") id_empresa: Int,
            @Path("id_local") id_local: Int,
            @Path("id_inventario") id_inventario: Int,
            @Path("id_imobilizado") id_imobilizado: Int,
            @Path("id_pasta") id_pasta: String,
            @Path("id_file") id_file: String,
            @Path("file_name") file_name: String
        ) : Call<JsonObject>


        @POST("foto")
        fun InsertFoto(
            @Body params: FotoModel
        ): Call<FotoModel>

        @POST("deleteuploadfotov5")
        fun DeleteFoto(
            @Body params: FotoModel
        ): Call<JsonObject>

        @PUT("foto")
        fun putFoto(
            @Body params: FotoModel
        ): Call<JsonObject>

        @Multipart
        @POST("uploadfotov5")
        fun postUploadFoto(
            @Part("id_empresa") id_empresa: RequestBody,
            @Part("id_local") id_local: RequestBody,
            @Part("id_inventario") id_inventario: RequestBody,
            @Part("id_imobilizado") id_imobilizado: RequestBody,
            @Part("id_pasta") id_pasta: RequestBody,
            @Part("id_file") id_file: RequestBody,
            @Part("old_name") old_name: RequestBody,
            @Part("id_usuario") id_usuario: RequestBody,
            @Part("data") data: RequestBody,
            @Part("destaque") destaque: RequestBody,
            @Part("obs") obs: RequestBody,
            @Part("localizacao") localizacao: RequestBody,
            @Part file: MultipartBody.Part,

            ): Call<RetornoUpload>

            @Multipart
            @POST("uploadfotov5_2_disp")
            suspend fun uploadfotov5_2_disp(
                @Part("id_empresa") idEmpresa: RequestBody,
                @Part("id_local") idLocal: RequestBody,
                @Part("id_inventario") idInventario: RequestBody,
                @Part("id_imobilizado") idImobilizado: RequestBody,
                @Part("id_pasta") idPasta: RequestBody,
                @Part("id_file") idFile: RequestBody,
                @Part("file_name") fileNameOriginal: RequestBody,
                @Part("id_usuario") idUsuario: RequestBody,
                @Part("data") data: RequestBody,
                @Part("destaque") destaque: RequestBody,
                @Part("obs") obs: RequestBody,
                @Part("localizacao") localizacao: RequestBody,
                @Part file: MultipartBody.Part
            ): Response<RetornoUpload>

        @Multipart
        @POST("uploadfotov5_2_web")
        suspend fun uploadfotov5_2_web(
            @Part("id_empresa") idEmpresa: RequestBody,
            @Part("id_local") idLocal: RequestBody,
            @Part("id_inventario") idInventario: RequestBody,
            @Part("id_imobilizado") idImobilizado: RequestBody,
            @Part("id_pasta") idPasta: RequestBody,
            @Part("id_file") idFile: RequestBody,
            @Part("file_name") fileNameOriginal: RequestBody,
            @Part("id_usuario") idUsuario: RequestBody,
            @Part("data") data: RequestBody,
            @Part("destaque") destaque: RequestBody,
            @Part("obs") obs: RequestBody,
            @Part("localizacao") localizacao: RequestBody,
            @Part file: MultipartBody.Part
        ): Response<RetornoUpload>
    }