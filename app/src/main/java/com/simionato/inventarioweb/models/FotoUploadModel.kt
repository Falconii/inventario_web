package com.simionato.inventarioweb.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FotoUploadModel(
    var id: Int,
    var idEmpresa: Int,
    var idLocal: Int,
    var idInventario: Int,
    var idImobilizado: Int,
    var idPasta: String,
    var idFile: String,
    var fileName: String,
    var fileNameOriginal: String,
    var idUsuario: Int,
    var data: String, // Pode ser LocalDate se estiver usando Java 8+
    var destaque: String, // CHAR(1), pode ser tratado como Boolean
    var obs: String,
    var userInsert: Int,
    var userUpdate: Int?
): Parcelable {
    constructor() : this(
        1,
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
        "N",
        "",
        0,
        0
    )

}