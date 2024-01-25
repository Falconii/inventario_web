package com.simionato.inventarioweb.models

data class UsuarioModel(
    var id_empresa:Int ,
    var id:Int ,
    var cnpj_cpf:String ,
    var razao:String ,
    var cadastr:String ,
    var rua:String ,
    var nro:String ,
    var complemento:String ,
    var bairro:String ,
    var cidade:String ,
    var uf:String ,
    var cep:String ,
    var tel1:String ,
    var tel2:String ,
    var email:String ,
    var obs:String ,
    var senha:String ,
    var grupo:Int ,
    var ativo:String ,
    var user_insert:Int ,
    var user_update:Int ,
    var grupo_descricao:String
) {
    constructor() : this(
        0 ,
        0 ,
        "" ,
        "" ,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        "S",
        0,
        0,
        ""
    )

}
