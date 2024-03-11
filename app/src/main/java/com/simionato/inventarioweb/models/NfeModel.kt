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
    var qtd:Int ,
    var punit:Int ,
    var totalitem:Int ,
    var vlrcontabil:Int ,
    var baseicms:Int ,
    var percicms:Int ,
    var vlrcicms:Int ,
    var user_insert:Int ,
    var user_update:Int ,
    var imo_descricao:String
)
