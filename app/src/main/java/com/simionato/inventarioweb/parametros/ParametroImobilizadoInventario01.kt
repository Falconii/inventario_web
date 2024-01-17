package com.simionato.inventarioweb.parametros

data class ParametroImobilizadoInventario01(
    val id_empresa:Int,
    val id_filial:Int,
    val id_inventario:Int,
    var id_imobilizado:Int,
    val id_cc:String,
    val id_grupo:Int,
    val descricao:String,
    val status:Int,
    val new_cc:String,
    val new_codigo:Int,
    val id_usuario:Int,
    val pagina:Int,
    val tamPagina:Int,
    val contador:String,
    val orderby:String,
    val sharp: Boolean
)
