package com.simionato.inventarioweb.parametros

class ParametroImobilizado01(
    var id_empresa:Int ,
    var id_filial:Int ,
    var codigo:Int ,
    var descricao:String ,
    var grupo_cod:Int ,
    var cc_cod:String ,
    var origem:String ,
    var pagina:Int ,
    var tamPagina:Int ,
    var contador:String ,
    var orderby:String ,
    var sharp: Boolean
) {
    constructor() : this(
        0,
        0,
        0,
        "",
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