package com.simionato.inventarioweb.global

import android.Manifest
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.UsuarioModel
import com.simionato.inventarioweb.parametros.ParametroImobilizadoInventario01
import com.simionato.inventarioweb.parametros.ParametroInventario01

class ParametroGlobal {
    class Dados {
        companion object {
            var usuario:UsuarioModel = UsuarioModel()
            var empresa:EmpresaModel = EmpresaModel()
            var local:LocalModel = LocalModel()
            var Inventario: InventarioModel = InventarioModel()
            var paramImoInventario:ParametroImobilizadoInventario01 = ParametroImobilizadoInventario01()
        }
    }

    class Situacoes {
        companion object {
            public fun getSituacao(idx: Int): String {
                var retorno: String = "";

                when (idx) {
                    0 -> {
                        retorno = "Não Inventariado"
                    }
                    1 -> {
                        retorno = "Inventariado"
                    }

                    2 -> {
                        retorno = "Inven.Troca Código"
                    }

                    3 -> {
                        retorno = "Inven. Troca CC"
                    }

                    4 -> {
                        retorno = "Inven. Ambos Alterados"
                    }

                    5 -> {
                        retorno = "Inven. Como 'Não Encontrado'"
                    }

                    else -> {
                        retorno = "Não Inventariado"
                    }
                }

                return retorno
            }
        }
    }

    class Permissoes{
        companion object{
            val PERMISSAO_GALERIA  = Manifest.permission.READ_MEDIA_IMAGES
        }
    }

    class Ambiente {
        companion object {
            public fun itsOK(): Boolean {
                //Validando Paramentros
                if ((ParametroGlobal.Dados.usuario.id == 0) ||
                    (ParametroGlobal.Dados.local.id == 0)   ||
                    (ParametroGlobal.Dados.Inventario.codigo == 0)){
                    return true
                }
                return false
            }
        }
    }

    class Origem{

        companion object {
            public fun getOrigem(value:String):String{
                when (value) {
                    "P"  -> {return "PLANILHA"}
                    "M"  -> {return "DIGITAÇÃO"}
                    else -> {return "PLANILHA"}
                }
            }
        }
    }

    class prettyText {

        companion object {
            private val corTitulo: String = "#D5150C"
            private val corDescricao: String = "#0713D0"
            public fun tituloDescricao(titulo:String, descricao:String, quebra:Boolean = false) : Spanned {
               var retorno:String = ""
               retorno = "<font color=${corTitulo}>${titulo}</font>${if(quebra) "<br/>" else ""}<font color=${corDescricao}>${descricao}</font>"
               return Html.fromHtml(retorno, Html.FROM_HTML_MODE_LEGACY)!!
            }
            public fun tituloDescricaoDois(titulo1:String, descricao1:String,titulo2:String, descricao2:String) : Spanned {
                var retorno:String = ""
                retorno  = "<font color=${corTitulo}>${titulo1}</font><font color=${corDescricao}>${descricao1}</font>&nbsp;&nbsp;&nbsp;"
                retorno += "<font color=${corTitulo}>${titulo2}</font><font color=${corDescricao}>${descricao2}</font>"
                return Html.fromHtml(retorno, Html.FROM_HTML_MODE_LEGACY)!!
            }

            public fun tituloDescricaotres(titulo1:String, descricao1:String,
                                           titulo2:String, descricao2:String,
                                           titulo3:String, descricao3:String,
                                           quebra:Boolean = false) : Spanned {
                var retorno:String = ""
                retorno  = "<font color=${corTitulo}>${titulo1}</font><font color=${corDescricao}>${descricao1}</font>${if(quebra) "<br/>" else "&nbsp;&nbsp;"}"
                retorno += "<font color=${corTitulo}>${titulo2}</font><font color=${corDescricao}>${descricao2}</font>${if(quebra) "<br/>" else "&nbsp;&nbsp;"}"
                retorno += "<font color=${corTitulo}>${titulo3}</font><font color=${corDescricao}>${descricao3}</font>"
                return Html.fromHtml(retorno, Html.FROM_HTML_MODE_LEGACY)!!
            }
            public fun tituloDescricao4x2(titulo1:String, descricao1:String,
                                          titulo2:String, descricao2:String,
                                          titulo3:String, descricao3:String,
                                          titulo4:String, descricao4:String,) : Spanned {
                var retorno:String = ""
                retorno  = "<font color=${corTitulo}>${titulo1}</font><font color=${corDescricao}>${descricao1}</font>&nbsp;&nbsp;&nbsp;"
                retorno += "<font color=${corTitulo}>${titulo2}</font><font color=${corDescricao}>${descricao2}</font><br/>"
                retorno += "<font color=${corTitulo}>${titulo3}</font><font color=${corDescricao}>${descricao3}</font>&nbsp;&nbsp;&nbsp;"
                retorno += "<font color=${corTitulo}>${titulo4}</font><font color=${corDescricao}>${descricao4}</font>"
                return Html.fromHtml(retorno, Html.FROM_HTML_MODE_LEGACY)!!
            }
            public fun ambiente_produto(id_produto:Int,descricao:String ) : Spanned {
                var retorno:String = ""
                var titulo1:String = "LOCAL: "
                var descricao1:String = Inventario.local_razao
                var titulo2:String = "INVENTÁRIO: "
                var descricao2:String = Inventario.descricao
                var titulo3:String = "PLAQUETA: "
                var descricao3:String = id_produto.toString().padStart(6,'0')
                var titulo4:String = "DESCRIÇÃO: "
                var descricao4:String = descricao

                retorno  = "<font color=${corTitulo}>${titulo1}</font><font color=${corDescricao}>${descricao1}</font><br/>"
                retorno += "<font color=${corTitulo}>${titulo2}</font><font color=${corDescricao}>${descricao2}</font><br/>"
                retorno += "<font color=${corTitulo}>${titulo3}</font><font color=${corDescricao}>${descricao3}</font><br/>"
                retorno += "<font color=${corTitulo}>${titulo4}</font><font color=${corDescricao}>${descricao4}</font><br/>"
                return Html.fromHtml(retorno, Html.FROM_HTML_MODE_LEGACY)!!
            }
        }
    }

}