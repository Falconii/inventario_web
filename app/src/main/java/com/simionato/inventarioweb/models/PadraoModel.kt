package com.simionato.inventarioweb.models

data class PadraoModel(
    var id_empresa:Int ,
    var id_usuario:Int ,
    var id_empresa_padrao:Int ,
    var id_local_padrao:Int ,
    var id_inv_padrao:Int ,
    var user_insert:Int ,
    var user_update:Int ,
    var empresa_razao:String ,
    var local_razao:String,
    var inve_descricao:String
){
    constructor() : this(
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        "Empresa Não Definida!",
        "Local Não Definido!",
        "Inventário Não Definido!",
    )
}
