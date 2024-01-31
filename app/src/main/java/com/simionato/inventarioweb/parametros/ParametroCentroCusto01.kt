package com.simionato.inventarioweb.parametros

data class ParametroCentroCusto01(
var id_empresa:Int ,
var id_filial:Int ,
var codigo:String,
var descricao:String,
var pagina:Int ,
var tamPagina:Int ,
var contador:String,
var orderby:String,
var sharp:Boolean
){
    constructor():this(
        0,
        0,
        "",
        "",
        0,
        50,
        "N",
        "",
        false
    )
}
