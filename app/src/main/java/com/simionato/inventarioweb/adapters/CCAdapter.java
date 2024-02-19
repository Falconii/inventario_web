package com.simionato.inventarioweb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.simionato.inventarioweb.R;
import com.simionato.inventarioweb.models.CentroCustoModel;

import java.util.List;

public class CCAdapter extends ArrayAdapter {

    private int escolha = -1;

    private String status = "";

    private Boolean visible   = false;

    private boolean connected = false;

    private Context context;

    private List<CentroCustoModel> custos;

    public CCAdapter(Context context, int textViewResourceId, List<CentroCustoModel> objects) {

        super(context, textViewResourceId, objects);

        this.context = context;

        custos = objects;

    }

    public void setEscolha(int escolha) {

        this.escolha = escolha;

        notifyDataSetChanged();

    }

    public int getEscolha() {
        return escolha;
    }


    public View getOpcoesView(final int position, View convertView, ViewGroup parent) {

        // Infla layout customizado
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.item_cc_opcoes, parent, false);

        TextView tvCodigo = (TextView) layout.findViewById(R.id.txtCodGenerico100);

        TextView tvDescricao = (TextView) layout.findViewById(R.id.txtDescricaoGenerico100);

        tvCodigo.setText("Código: "+custos.get(position).getCodigo());

        tvDescricao.setText(custos.get(position).getDescricao());

        return layout;
    }


    public View getEscolhaView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.item_cc_escolha, parent, false);

        TextView tvDescricao = (TextView) layout.findViewById(R.id.txtDescricaoGenerico100);

        tvDescricao.setText(custos.get(position).getDescricao());

        return layout;
    }

    // Mostra as Opções
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        return getOpcoesView(position, convertView, parent);

    }

    // Mostra o item selecionado
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getEscolhaView(position, convertView, parent);

    }
}
