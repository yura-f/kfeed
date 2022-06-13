package com.yuraf.kfeed.ui.fragment.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.yuraf.kfeed.R
import com.yuraf.kfeed.common.UniqueIdDiffCallback
import com.yuraf.kfeed.data.SearchPhoto
import com.yuraf.kfeed.databinding.PhotoItemBinding
import org.koin.java.KoinJavaComponent.inject

/**
 * @author Yura F (yura-f.github.io)
 */
class PhotoAdapter(private val onClick: (SearchPhoto) -> Unit) : ListAdapter<SearchPhoto, PhotoAdapter.ViewHolder>(UniqueIdDiffCallback<SearchPhoto>()) {

    private val imageLoader: ImageLoader by inject(ImageLoader::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SearchPhoto) {
            with(binding) {
                val request = ImageRequest.Builder(container.context)
                    .data(data.url)
                    .crossfade(true)
                    .placeholder(R.color.light_grey)
                    .error(R.color.purple_200)
                    .target(image)
                    .build()

                imageLoader.enqueue(request)

                if (!data.isCompleted()) {
                    reloadText.isVisible = true
                    container.setOnClickListener {
                        onClick(data)
                    }
                } else {
                    reloadText.isVisible = false
                    container.setOnClickListener(null)
                }
            }
        }
    }
}