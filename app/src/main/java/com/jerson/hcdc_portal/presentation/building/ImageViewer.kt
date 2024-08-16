package com.jerson.hcdc_portal.presentation.building

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.jerson.hcdc_portal.databinding.ActivityImageViewerBinding
import com.jerson.hcdc_portal.presentation.building.adapter.ImageViewerAdapter

class ImageViewer : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding
    private val list = mutableListOf<String>()
    private lateinit var adapter: ImageViewerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val xList = intent.extras?.getStringArrayList("imgList")

        xList?.let {
            list.addAll(xList)
        }

        adapter = ImageViewerAdapter(list)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = 0

        views()
        binding.btnRight.setOnClickListener{
            binding.viewPager.currentItem += 1
            views()
        }
        binding.btnLeft.setOnClickListener{
            binding.viewPager.currentItem -= 1
            views()
        }

        binding.btnClose.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Handle page scrolled
                views()
            }

            override fun onPageSelected(position: Int) {
                // Handle page selected
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Handle page scroll state changed
            }
        })
    }

    private fun views() {
        if (list.size == 1 && binding.viewPager.currentItem == 0) {
            binding.btnRight.visibility = View.GONE
            binding.btnLeft.visibility = View.GONE
        } else if (list.size - 1 == binding.viewPager.currentItem) {
            // last position
            binding.btnRight.visibility = View.GONE
            binding.btnLeft.visibility = View.VISIBLE
        } else if (binding.viewPager.currentItem == 0) {
            binding.btnLeft.visibility = View.GONE
            binding.btnRight.visibility = View.VISIBLE
        } else {
            binding.btnLeft.visibility = View.VISIBLE
            binding.btnRight.visibility = View.VISIBLE
        }
    }

}