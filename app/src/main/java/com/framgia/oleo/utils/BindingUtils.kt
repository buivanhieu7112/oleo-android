package com.framgia.oleo.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framgia.oleo.R

class BindingUtils {

    companion object {
        @BindingAdapter("android:imageUrl")
        @JvmStatic
        fun setImageUrl(image: ImageView, uri: String?) {
            Glide.with(image.context).load(uri)
                .apply(RequestOptions().circleCrop().placeholder(R.drawable.ic_user_circle))
                .into(image)
        }

        @BindingAdapter("android:adapter")
        @JvmStatic
        fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
            recyclerView.adapter = adapter
        }
    }
}
