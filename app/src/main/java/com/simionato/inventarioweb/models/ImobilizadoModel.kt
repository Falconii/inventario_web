package com.simionato.inventarioweb.models

data class ImobilizadoModel(
    var id_empresa:Int ,
    var id_filial:Int ,
    var codigo:Int ,
    var descricao:String ,
    var cod_grupo:Int ,
    var cod_cc:String ,
    var nfe:String ,
    var serie:String ,
    var item:String ,
    var origem:String ,
    var user_insert:Int ,
    var user_update:Int ,
    var grupo_descricao:String ,
    var cc_descricao:String ,
    var forne_razao:String
)
{
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
        "",
        0,
        0,
        "",
        "",
        ""
    )
}
