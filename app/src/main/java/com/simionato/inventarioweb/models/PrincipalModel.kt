    package com.simionato.inventarioweb.models

    data class PrincipalModel(var id_empresa:Int ,
      var id_filial:Int ,
      var codigo:Int ,
      var descricao:String,
      var user_insert:Int ,
      var user_update:Int
    ){ constructor() : this(
        0,
        0,
        0,
        "",
        0,
        0
    )}
