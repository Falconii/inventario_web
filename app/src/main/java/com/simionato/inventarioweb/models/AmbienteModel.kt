package com.simionato.inventarioweb.models

data class AmbienteModel(
     var id_retorno: Int,
     var mensa_retorno: String,
     var padrao: PadraoModel?,
     var empresa: EmpresaModel?,
     var local: LocalModel?,
     var inventario: InventarioModel?
){
    constructor() : this(
        0,
        "",
        null,
        null,
        null,
        null
    )
}


