package com.simionato.inventarioweb.parametros

import android.provider.Settings.Global
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.Inventario
import com.simionato.inventarioweb.global.ParametroGlobal.Dados.Companion.usuario

data class parametroSendEmail01(
    var id_empresa:Int ,
    var destinatario:String ,
    var assunto:String ,
    var mensagem:String
){
    constructor() : this(
         Inventario.id_empresa,
        "marcos.falconi@simionatoauditores.com.br",
        "Relatório Do Inventário. Solicitado Pelo Celular ",
        "Relatório Gerado Por ${usuario.razao}",
    )
}
