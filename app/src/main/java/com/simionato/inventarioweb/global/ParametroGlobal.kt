package com.simionato.inventarioweb.global

import com.simionato.inventarioweb.models.EmpresaModel
import com.simionato.inventarioweb.models.InventarioModel
import com.simionato.inventarioweb.models.LocalModel
import com.simionato.inventarioweb.models.UsuarioModel

class ParametroGlobal {
    class Dados {
        companion object {
            var usuario:UsuarioModel = UsuarioModel()
            var empresa:EmpresaModel = EmpresaModel()
            var local:LocalModel = LocalModel()
            var InventarioModel: InventarioModel = InventarioModel()
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
}