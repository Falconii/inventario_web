package com.simionato.inventarioweb.models

data class InventarioModel(
    var id_empresa:Int ,
    var id_filial:Int ,
    var codigo:Int ,
    var descricao:String ,
    var id_responsavel:Int ,
    var data_inicial:String ,
    var data_final:String ,
    var data_encerra:String ,
    var laudo:String ,
    var user_insert:Int ,
    var user_update:Int ,
    var local_razao:String ,
    var resp_razao:String
) {
    constructor() : this(
        0,
        0,
        0,
        "",
        0,
        "",
        "",
        "",
        "",
        0,
        0,
        "",
        ""
    )
}
