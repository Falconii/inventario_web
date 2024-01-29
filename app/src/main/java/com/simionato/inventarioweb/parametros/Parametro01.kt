package com.simionato.inventarioweb.parametros

data class Parametro01(
    var id_empresa			:Int ,
    var id_usuario			:Int ,
    var pagina				:Int ,
    var tamPagina			:Int ,
    var contador			:String,
    var orderby				:String,
    var sharp				:Boolean
){
    constructor() : this(
        0 ,
        0 ,
        0 ,
        50 ,
        "N" ,
        "Código" ,
        false ,
    )
}
