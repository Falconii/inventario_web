package com.simionato.inventarioweb.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FotoUpload(
    var id:Int ,
    var name:String ,
    var path:String ,
    var status:Int
): Parcelable {
    constructor() : this(
        1,
        "",
        "",
        0
    )

}