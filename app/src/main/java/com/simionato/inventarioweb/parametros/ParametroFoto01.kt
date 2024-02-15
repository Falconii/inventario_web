package com.simionato.inventarioweb.parametros

data class ParametroFoto01(
    var id_empresa:Int ,
    var id_local:Int ,
    var id_inventario:Int ,
    var id_imobilizado:Int ,
    var id_pasta:String ,
    var id_file:String ,
    var file_name:String ,
    var destaque:String ,
    var pagina:Int ,
    var tamPagina:Int,
    var contador:String ,
    var orderby:String ,
    var sharp:Boolean
){
    constructor() : this(
        0,
        0,
        0,
        0,
        "",
        "",
        "",
        "N",
        0,
        50,
        "N",
        "",
        false
    )
}
