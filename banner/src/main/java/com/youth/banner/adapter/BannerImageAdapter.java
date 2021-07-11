package com.youth.banner.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.youth.banner.databinding.ItemSample1Binding;
import com.youth.banner.holder.BannerImageHolder;

import java.util.List;

/**
 * 默认实现的图片适配器，图片加载需要自己实现
 */
public abstract class BannerImageAdapter<T> extends BannerAdapter<T, BannerImageHolder<ItemSample1Binding>> {

    public BannerImageAdapter(List<T> mData) {
        super(mData);
    }

    @Override
    public BannerImageHolder<ItemSample1Binding> onCreateHolder(ViewGroup parent, int viewType) {
        ItemSample1Binding binding = ItemSample1Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        return new BannerImageHolder<>(binding);
    }

}
