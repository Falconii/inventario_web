package com.simionato.inventarioweb.parametros

data class ParametroNfe01(
    var id_empresa:Int ,
    var id_filial:Int ,
    var cnpj_fornecedor:String ,
    var razao_fornecedor:String ,
    var id_imobilizado:Int ,
    var nfe:String ,
    var serie:String ,
    var chavee:String ,
    var pagina:Int ,
    var tamPagina:Int ,
    var contador:String ,
    var orderby:String ,
    var sharp:Boolean
){ constructor() : this(
    0,
    0,
    "",
    "",
    0,
    "",
    "N",
    "",
    0,
    50,
    "N",
    "Código",
    false
)}
