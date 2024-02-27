package com.simionato.inventarioweb.global

import android.Manifest
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
                        retorno = "Inventariado Troca Código"
                    }

                    3 -> {
                        retorno = "IInventariado Troca CC"
                    }

                    4 -> {
                        retorno = "Inventariado Ambos Alterados"
                    }

                    5 -> {
                        retorno = "Inventariado Como 'Não Encontrado'"
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

}