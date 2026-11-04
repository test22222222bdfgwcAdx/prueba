package com.example.canales;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class CanalAdapter extends RecyclerView.Adapter<CanalAdapter.ViewHolder> {

    Context context;
    List<Canal> lista;

    public CanalAdapter(Context context, List<Canal> lista) {
        this.context = context;
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNombre;

        public ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.img);
            txtNombre = v.findViewById(R.id.txtNombre);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_canal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Canal c = lista.get(position);

        holder.itemView.setFocusable(true);
        holder.itemView.setFocusableInTouchMode(false);

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {

                v.setScaleX(1.10f);
                v.setScaleY(1.10f);

            } else {

                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }
        });

        if (c.imagen == null || c.imagen.isEmpty()) {

            holder.img.setImageDrawable(null);

            holder.txtNombre.setVisibility(View.VISIBLE);
            holder.txtNombre.setText(c.nombre);

        } else {

            holder.txtNombre.setVisibility(View.GONE);

            Glide.with(context)
                    .load(c.imagen)
                    .placeholder(android.R.drawable.ic_media_play)
                    .error(new ColorDrawable(Color.TRANSPARENT))
                    .into(holder.img);
        }

        holder.itemView.setOnClickListener(v -> {

            Intent i = new Intent(context, PlayerActivity.class);

            i.putExtra("url", c.url);
            i.putExtra("tipo", c.tipo);
            i.putExtra("key", c.key);
            i.putExtra("referer", c.referer);
            i.putExtra("origin", c.origin);
            i.putExtra("userAgent", c.userAgent);
            i.putExtra("licencia", c.licencia);

            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}