package com.simionato.inventarioweb.parametros

data class ParametroImobilizadoInventario01(
    var id_empresa:Int,
    var id_filial:Int,
    var id_inventario:Int,
    var id_imobilizado:Int,
    var id_cc:String,
    var id_grupo:Int,
    var descricao:String,
    var status:Int,
    var new_cc:String,
    var new_codigo:Int,
    var id_usuario:Int,
    var pagina:Int,
    var tamPagina:Int,
    var contador:String,
    var orderby:String,
    var sharp: Boolean
)
