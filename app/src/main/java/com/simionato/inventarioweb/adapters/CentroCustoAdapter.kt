package com.simionato.inventarioweb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.simionato.inventarioweb.R
import com.simionato.inventarioweb.models.CentroCustoModel


class CentroCustoAdapter(context: Context, ccLista: List<CentroCustoModel>):
    ArrayAdapter<CentroCustoModel>(context,0,ccLista){
        var layoutInflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view:View = layoutInflater.inflate(R.layout.item_cc_opcoes,parent,false)

        val  cc:CentroCustoModel = getItem(position) ?:  return view

        val txtCodigo =  view.findViewById<TextView>(R.id.txtCodGenerico100)
        val txtDescricao = view.findViewById<TextView>(R.id.txtDescricaoGenerico100)

        txtCodigo?.setText(cc.codigo)
        txtDescricao?.setText(cc.descricao)

        return view
        //
        //return view(view,position)
    }

   /* override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

        var row: View? = null

        if (position === 0) {
            row = super.getDropDownView(position, convertView, parent)
            row.setBackgroundColor(Color.LTGRAY)
        } else {
            row = super.getDropDownView(position, null, parent)
        }
        return row
    }
    private fun view(view:View, position:Int):View {

        val  cc:CentroCustoModel = getItem(position) ?:  return view

        val txtCodigo =  view.findViewById<TextView>(R.id.txtCodCC100)
        val txtDescricao = view.findViewById<TextView>(R.id.txtDescricao100)

        txtCodigo?.setText(cc.codigo)
        txtDescricao?.setText(cc.descricao)
        return view
    }*/
    }