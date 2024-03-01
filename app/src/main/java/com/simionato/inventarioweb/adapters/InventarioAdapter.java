package com.simionato.inventarioweb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.simionato.inventarioweb.R;
import com.simionato.inventarioweb.models.InventarioModel;
import com.simionato.inventarioweb.models.InventarioModel;

import java.util.List;

public class InventarioAdapter extends ArrayAdapter {

    private int escolha = -1;

    private String status = "";

    private Boolean visible   = false;

    private boolean connected = false;

    private Context context;

    private List<InventarioModel> inventarios;

    public InventarioAdapter(Context context, int textViewResourceId, List<InventarioModel> objects) {

        super(context, textViewResourceId, objects);

        this.context = context;

        inventarios = objects;

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

        View layout = inflater.inflate(R.layout.item_spinner_opcoes, parent, false);

        TextView tvCodigo = (TextView) layout.findViewById(R.id.txtCodGenerico100);

        TextView tvDescricao = (TextView) layout.findViewById(R.id.txtDescricaoGenerico100);

        tvCodigo.setText("Código: "+inventarios.get(position).getCodigo());
        tvDescricao.setText(inventarios.get(position).getDescricao());

        return layout;
    }


    public View getEscolhaView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.item_spinner_escolha, parent, false);

        TextView tvDescricao = (TextView) layout.findViewById(R.id.txtDescricaoGenerico100);

        tvDescricao.setText(inventarios.get(position).getDescricao());

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
