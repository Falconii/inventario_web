package com.simionato.inventarioweb.parametros

data class ParametroUsuario01(val id_empresa: Int,
                              val id: Int,
                              val ativo: String,
                              val razao: String,
                              val cnpj_cpf: String,
                              val grupo: Array<Int>,
                              val timer: String,
                              val ticket: String,
                              val flag_ponte: String,
                              val data: String,
                              val pagina: Int,
                              val tamPagina:Int,
                              val contador:String,
                              val orderby: String,
                              val sharp: Boolean)
