package com.simionato.inventarioweb.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LancamentoModel(
    val id_empresa:Int ,
    val id_filial:Int ,
    val id_inventario:Int ,
    val id_imobilizado:Int ,
    val id_usuario:Int ,
    val id_lanca:Int ,
    val obs:String ,
    val dtlanca:String ,
    val estado:Int ,
    val new_codigo:Int ,
    val new_cc:String ,
    val user_insert:Int ,
    val user_update:Int ,
    val imo_inv_status:Int ,
    val inv_descricao:String ,
    val imo_cod_cc:String ,
    val imo_cod_grupo:Int ,
    val imo_descricao:String ,
    val usu_razao:String ,
): Parcelable
