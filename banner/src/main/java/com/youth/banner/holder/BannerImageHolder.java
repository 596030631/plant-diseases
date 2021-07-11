package com.youth.banner.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public
class BannerImageHolder<T extends ViewBinding> extends RecyclerView.ViewHolder {
    public T binding;

    public BannerImageHolder(@NonNull T view) {
        super(view.getRoot());
        this.binding = view;
    }
}
