package com.jerson.hcdc_portal.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ViewSettingOptionBinding

class SettingOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ViewSettingOptionBinding =
        ViewSettingOptionBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingOptionView, 0, 0)

        try {
            val icon = a.getResourceId(R.styleable.SettingOptionView_settingIcon, R.drawable.ic_settings)
            val title = a.getString(R.styleable.SettingOptionView_settingTitle)
            val subtitle = a.getString(R.styleable.SettingOptionView_settingSubtitle)

            binding.icon.setImageResource(icon)
            binding.title.text = title
            binding.subtitle.text = subtitle
        } finally {
            a.recycle()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    fun setIcon(resource: Int) {
        binding.icon.setImageResource(resource)
    }
}