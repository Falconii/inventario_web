package com.simionato.inventarioweb.parametros

data class ParametroCentroCusto01(
    val id_empresa:Int ,
    val id_filial:Int ,
    val codigo:String,
    val descricao:String,
    val pagina:Int ,
    val tamPagina:Int ,
    val contador:String,
    val orderby:String,
    val sharp:Boolean
)
