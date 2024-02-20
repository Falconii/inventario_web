package com.simionato.inventarioweb.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FotoModel(
    var id_empresa:Int ,
    var id_local:Int ,
    var id_inventario:Int ,
    var id_imobilizado:Int ,
    var id_pasta:String ,
    var id_file:String ,
    var file_name:String ,
    var file_name_original: String,
    var id_usuario:Int ,
    var data:String ,
    var destaque:String ,
    var obs:String ,
    var user_insert:Int ,
    var user_update:Int,
    var imo_descricao:String,
    var usu_razao :String) : Parcelable {

    constructor() : this(
        0,
        0,
        0,
        0,
        "",
        "",
        "",
        "",
        0,
        "",
        "",
        "",
        0,
        0,
        "",
        ""
    )
}
