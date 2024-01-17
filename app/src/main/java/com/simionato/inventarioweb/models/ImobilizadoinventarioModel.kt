package com.simionato.inventarioweb.models

data class ImobilizadoinventarioModel(
    val id_empresa:Int ,
    val id_filial:Int ,
    val id_inventario:Int ,
    val id_imobilizado:Int ,
    val id_lanca:Int ,
    val status:Int ,
    val new_codigo:Int ,
    val new_cc:String ,
    val user_insert:Int ,
    val user_update:Int ,
    val imo_descricao:String ,
    val imo_cod_cc:String ,
    val imo_cod_grupo:Int ,
    val imo_nfe:String ,
    val imo_serie:String ,
    val imo_item:Int ,
    val cc_descricao:String ,
    val grupo_descricao:String ,
    val lanc_id_usuario:Int ,
    val lanc_dt_lanca:String ,
    val lanc_obs:String ,
    val lanc_estado:Int ,
    val usu_razao:String ,
    val new_cc_descricao:String
)

