package com.emdev.matematicamente.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emdev.matematicamente.Interface.ItemClickListener;
import com.emdev.matematicamente.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ArchivoPrivadoViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    public ImageView doc_imagen, doc_download, doc_compartir, doc_delete;
    public TextView doc_nombre, doc_fecha;
    //public SwitchMaterial compartir;

    private ItemClickListener itemClickListener;

    public ArchivoPrivadoViewHolder(@NonNull View itemView) {
        super(itemView);

        doc_nombre = (TextView)itemView.findViewById(R.id.doc_nombre);
        //doc_materia = (TextView)itemView.findViewById(R.id.doc_materia);
        //doc_curso = itemView.findViewById(R.id.doc_curso);
        //doc_escuela = itemView.findViewById(R.id.doc_escuela);
        doc_fecha = (TextView)itemView.findViewById(R.id.doc_fecha);
        //compartir = itemView.findViewById(R.id.compartir);
        doc_imagen = (ImageView) itemView.findViewById(R.id.doc_imagen);
        doc_download = (ImageView) itemView.findViewById(R.id.descargar);
        doc_delete = (ImageView) itemView.findViewById(R.id.deleteWork);

        //click para cada item
        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
