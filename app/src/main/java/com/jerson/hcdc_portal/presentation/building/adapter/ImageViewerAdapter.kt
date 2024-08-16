package com.jerson.hcdc_portal.presentation.building.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import coil.ImageLoader
import coil.request.ImageRequest
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ItemImageViewBinding
import com.jerson.hcdc_portal.util.Constants

class ImageViewerAdapter(private val list: List<String>) : PagerAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding =
            ItemImageViewBinding.inflate(LayoutInflater.from(container.context), container, false)
        loadImage(list[position], binding.root.context, binding.image,binding.progressBar)
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    private fun loadImage(url: String, context: Context, imageView: SubsamplingScaleImageView, progress: ProgressBar) {
        progress.visibility = View.VISIBLE

        val request = ImageRequest.Builder(context)
            .data("${Constants.githubContent}$url")
            .target(
                onSuccess = { drawable ->
                    progress.visibility = View.GONE
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    imageView.setImage(ImageSource.Bitmap(bitmap))
                },
                onError = { _ ->
                    progress.visibility = View.GONE
                    imageView.setImage(ImageSource.Resource(R.drawable.ic_broken_image))
                }
            )
            .build()
        ImageLoader(context).enqueue(request)
    }
}