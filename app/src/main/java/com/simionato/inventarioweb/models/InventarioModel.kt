package com.simionato.inventarioweb.models

data class InventarioModel(
    val id_empresa:Int ,
    val id_filial:Int ,
    val codigo:Int ,
    val descricao:String ,
    val id_responsavel:Int ,
    val data_inicial:String ,
    val data_final:String ,
    val data_encerra:String ,
    val laudo:String ,
    val user_insert:Int ,
    val user_update:Int ,
    val local_razao:String ,
    val resp_razao:String
)
