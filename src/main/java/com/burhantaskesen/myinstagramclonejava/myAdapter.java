package com.burhantaskesen.myinstagramclonejava;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.burhantaskesen.myinstagramclonejava.databinding.RecycleRowBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private ArrayList<Post> arrayList;

    public myAdapter(ArrayList<Post> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecycleRowBinding binding = RecycleRowBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(arrayList.get(position).imageUrl).into(holder.binding.imageView);
        holder.binding.editText.setText(arrayList.get(position).comment);
        holder.binding.textView.setText(arrayList.get(position).email);
        holder.binding.editText.setKeyListener(new KeyListener() {
            @Override
            public int getInputType() {
                return 0;
            }

            @Override
            public boolean onKeyDown(View view, Editable editable, int i, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public boolean onKeyUp(View view, Editable editable, int i, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public boolean onKeyOther(View view, Editable editable, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public void clearMetaKeyState(View view, Editable editable, int i) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RecycleRowBinding binding;
        public ViewHolder(RecycleRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
