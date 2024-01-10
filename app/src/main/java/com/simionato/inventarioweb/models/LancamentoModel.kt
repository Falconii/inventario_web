package com.simionato.inventarioweb.models

data class LancamentoModel(
    var id_empresa: Int ,
    var id_filial: Int ,
    var id_inventario: Int ,
    var id_imobilizado: Int ,
    var id_usuario: Int ,
    var id_lanca: Int ,
    var obs: String ,
    var dtlanca: String ,
    var estado: Int ,
    var new_codigo: Int ,
    var new_cc: String ,
    var user_insert: Int ,
    var user_update: Int ,
    var imo_inv_status: Int ,
    var inv_descricao: String ,
    var imo_cod_cc: String ,
    var imo_cod_grupo: Int ,
    var imo_descricao: String ,
    var usu_razao: String
)
