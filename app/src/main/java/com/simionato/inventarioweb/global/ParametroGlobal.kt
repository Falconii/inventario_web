package com.simionato.inventarioweb.global

class ParametroGlobal {
    class Dados {
        companion object {
            var Id_Empresa: Int = 0
            var RazaoEmpresa: String = ""
            var Id_Local: Int = 0
            var RazaoLocal: String = ""
            var Id_Usuario: Int = 0
            var RazaoUsuario: String = ""
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
                        retorno = "Inv. Troca Código"
                    }

                    3 -> {
                        retorno = "Inv. Troca CC"
                    }

                    4 -> {
                        retorno = "Inv. Ambos Alterados"
                    }

                    5 -> {
                        retorno = "Inv. Não Encontrado"
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