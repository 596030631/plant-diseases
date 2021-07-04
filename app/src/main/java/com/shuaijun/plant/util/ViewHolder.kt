package com.shuaijun.plant.util

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ViewHolder<T : ViewBinding>(var binding: T) : RecyclerView.ViewHolder(binding.root)