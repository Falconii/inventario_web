package com.simionato.inventarioweb.parametros

data class ParametroNfe02(
    var id_empresa:Int ,
    var id_filial:Int ,
    var cnpj_fornecedor:String ,
    var razao_fornecedor:String ,
    var id_imobilizado:Int ,
    var nfe:String ,
    var serie:String ,
    var item:String ,
){
    constructor() : this(
        0,
        0,
        "",
        "",
        0,
        "",
        "",
        ""
    )
}
