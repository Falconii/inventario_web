package com.simionato.inventarioweb.parametros

data class ParametroPrincipal01(
var id_empresa:Int ,
var id_filial:Int ,
var codigo:Int,
var descricao:String,
var pagina:Int ,
var tamPagina:Int ,
var contador:String,
var orderby:String,
var sharp:Boolean
){
    constructor():this(
        1,
        0,
        0,
        "",
        0,
        50,
        "N",
        "Descrição",
        false
    )
}
