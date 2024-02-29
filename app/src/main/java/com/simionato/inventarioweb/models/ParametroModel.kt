package com.simionato.inventarioweb.models

data class ParametroModel(
    var id_empresa: Int,
    var modulo: String,
    var assinatura: String,
    var id_usuario: Int,
    var parametro: String,
    var user_insert: Int,
    var user_update: Int
)
