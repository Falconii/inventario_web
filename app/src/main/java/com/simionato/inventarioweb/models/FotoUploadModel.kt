package com.simionato.inventarioweb.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FotoUploadModel(
    val id: Int,
    val idEmpresa: Int,
    val idLocal: Int,
    val idInventario: Int,
    val idImobilizado: Int,
    val idPasta: String,
    val idFile: String,
    val fileName: String,
    val fileNameOriginal: String,
    val idUsuario: Int,
    val data: String, // Pode ser LocalDate se estiver usando Java 8+
    val destaque: String, // CHAR(1), pode ser tratado como Boolean
    val obs: String?,
    val userInsert: Int,
    val userUpdate: Int?
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