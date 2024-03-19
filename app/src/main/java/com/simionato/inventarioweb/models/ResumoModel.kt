package com.simionato.inventarioweb.models

data class ResumoModel(
    var descricao    : String ,
    var responsavel  : String ,
    var situacao     : String ,
    var total_ativos : Int ,
    var total_inventariados: Int ,
    var situacao_0 : Int ,
    var situacao_1 : Int ,
    var situacao_2 : Int ,
    var situacao_3 : Int ,
    var situacao_4 : Int ,
    var situacao_5 : Int ,
    var fotos      : Int
){
    constructor() : this(
        "",
        "",
        "",
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
    )
}

