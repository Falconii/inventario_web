package com.simionato.inventarioweb.models

data class FotoModel(
    val id_empresa:Int ,
    val id_local:Int ,
    val id_inventario:Int ,
    val id_imobilizado:Int ,
    val id_pasta:String ,
    val id_file:String ,
    val file_name:String ,
    val file_name_original: String,
    val id_usuario:Int ,
    val data:String ,
    val destaque:String ,
    val obs:String ,
    val user_insert:Int ,
    val user_update:Int
)
