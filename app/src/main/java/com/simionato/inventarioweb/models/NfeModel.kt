package com.simionato.inventarioweb.models

data class NfeModel(
    var id_empresa:Int ,
    var id_filial:Int ,
    var cnpj_fornecedor:String ,
    var razao_fornecedor:String ,
    var id_imobilizado:Int ,
    var nfe:String ,
    var serie:String ,
    var item:String ,
    var chavee:String ,
    var dtemissao:String ,
    var dtlancamento:String ,
    var qtd:Double ,
    var punit:Double ,
    var totalitem:Double ,
    var vlrcontabil:Double ,
    var baseicms:Double ,
    var percicms:Double ,
    var vlrcicms:Double ,
    var user_insert:Int ,
    var user_update:Int ,
    var imo_descricao:String
){
    constructor() : this(
        1,
        8,
        "6290121000184",
        "MARIA MADALENA",
        12,
        "909089",
        "03",
        "900",
        "12345678901234567890123456789012345678901234",
        "27/12/2020",
        "29/12/2020",
        1.00,
        890.90,
        890.90,
        890.90,
        890.90,
        18.00,
        120.00,
        1,
        0,
        "AMAMMAMAMAMMAMAMAMMAMAM"
    )
}
