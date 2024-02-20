package com.simionato.inventarioweb.global


    public enum class CadastrosAcoes(idAcao:Int) {
        Inclusao(1),
        Edicao(2),
        Consulta(3),
        Exclusao( 4),
        Atualizacao(5),
        Gravacao(6),
        Copy_Estrutura(7),
        Sub_Conta(8),
        Visualizar_Estrtura(9),
        EdicaoMultipla(10),
        None(99);

    }