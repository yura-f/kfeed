package com.yuraf.kfeed.common

import androidx.recyclerview.widget.DiffUtil

/**
 * @author Yura F (yura-f.github.io)
 */
class UniqueIdDiffCallback<T : UniqueId> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.uniqueId == newItem.uniqueId
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.equals(newItem)
    }
}