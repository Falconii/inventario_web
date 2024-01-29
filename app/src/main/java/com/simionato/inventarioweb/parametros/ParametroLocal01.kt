package com.simionato.inventarioweb.parametros

data class ParametroLocal01(
var id_empresa:Int ,
var id:Int ,
var razao:String ,
var cnpj_cpf:String ,
var pagina:Int ,
var tamPagina:Int ,
var contador:String ,
var orderby:String ,
var sharp:Boolean) {
    constructor() : this(
        0,
        0,
        "",
        "",
        0,
        50,
        "N",
        "Código",
        true
    )
}
