package com.simionato.inventarioweb.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ImobilizadoinventarioModel(
    var id_empresa:Int ,
    var id_filial:Int ,
    var id_inventario:Int ,
    var id_imobilizado:Int ,
    var id_lanca:Int ,
    var status:Int ,
    var new_codigo:Int ,
    var new_cc:String ,
    var user_insert:Int ,
    var user_update:Int ,
    var imo_descricao:String ,
    var imo_cod_cc:String ,
    var imo_cod_grupo:Int ,
    var imo_nfe:String ,
    var imo_serie:String ,
    var imo_item:String ,
    var cc_descricao:String ,
    var grupo_descricao:String ,
    var lanc_id_usuario:Int ,
    var lanc_dt_lanca:String ,
    var lanc_obs:String ,
    var lanc_estado:Int ,
    var usu_razao:String ,
    var new_cc_descricao:String
) : Parcelable

