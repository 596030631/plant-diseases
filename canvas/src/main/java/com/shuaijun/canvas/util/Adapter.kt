package com.shuaijun.plant.util

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
class ViewHolder<T : ViewBinding>(var binding: T) : RecyclerView.ViewHolder(binding.root)
class Adapter<T : Any, V : ViewBinding>(
    var data: MutableList<T>,
    var call: (binding: V, position: Int) -> Unit,
    var vh: (parent: ViewGroup) -> ViewHolder<V>
) : RecyclerView.Adapter<ViewHolder<V>>() {

    override fun onBindViewHolder(holder: ViewHolder<V>, position: Int) {
        call(holder.binding, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<V> {
        return vh(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}