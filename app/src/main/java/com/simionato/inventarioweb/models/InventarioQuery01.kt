package com.simionato.inventarioweb.models

data class InventarioQuery01(var id_empresa: Int ,
                             var id: Int ,
                             var id_cliente: Int ,
                             var id_responsavel: Int ,
                             var data_inicial: String ,
                             var data_final: String ,
                             var data_encerra: String ,
                             var descricao: String ,
                             var user_insert: Int ,
                             var user_update: Int ,
                             var _razao_cliente: String ,
                             var _nome_responsavel: String )
