package com.simionato.inventarioweb.models

data class CentroCustoModel(
    var id_empresa: Int ,
    var id_filial:Int ,
    var codigo:String ,
    var descricao:String ,
    var user_insert:Int ,
    var user_update:Int
)