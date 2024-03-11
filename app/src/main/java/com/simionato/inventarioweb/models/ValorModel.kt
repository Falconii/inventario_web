package com.simionato.inventarioweb.models

data class ValorModel(val id_empresa:Int ,
                      val id_filial:Int ,
                      val id_imobilizado:Int ,
                      val dtaquisicao:String,
                      val vlraquisicao:Double ,
                      val totaldepreciado:Double ,
                      val vlrresidual:Double ,
                      val reavalicao:Double ,
                      val deemed:Double ,
                      val vlrconsolidado:Double ,
                      val user_insert:Int ,
                      val user_update:Int ,
                      val imo_descricao:String
)
