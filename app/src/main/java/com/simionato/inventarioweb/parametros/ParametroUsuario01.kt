package com.simionato.inventarioweb.parametros

data class ParametroUsuario01(val id_empresa:Int ,
                              val id:Int ,
                              val razao:String ,
                              val cnpj_cpf:String ,
                              val grupo:Int ,
                              val pagina:Int ,
                              val tamPagina:Int ,
                              val contador:String ,
                              val orderby:String ,
                              val sharp:Boolean)
