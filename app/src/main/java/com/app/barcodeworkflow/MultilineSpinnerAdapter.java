package com.app.barcodeworkflow;
//23.05.25

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MultilineSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public MultilineSpinnerAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // View do item selecionado (simple_spinner_item)
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(items.get(position));
        textView.setSingleLine(false);  // Permite múltiplas linhas
        textView.setMaxLines(2);        // Limite para o item selecionado

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // View dos itens do dropdown (simple_spinner_dropdown_item)
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(items.get(position));
        textView.setSingleLine(false);  // Permite múltiplas linhas
        textView.setMaxLines(3);        // Limite para os itens do dropdown

        return convertView;
    }
}