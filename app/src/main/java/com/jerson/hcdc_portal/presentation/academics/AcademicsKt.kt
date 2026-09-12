package com.jerson.hcdc_portal.presentation.academics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentAcademicsKtBinding
import com.jerson.hcdc_portal.presentation.enrollhistory.EnrollHistoryKt
import com.jerson.hcdc_portal.presentation.grade.GradesKt
import com.jerson.hcdc_portal.presentation.prospectus.ProspectusFragment
import com.jerson.hcdc_portal.presentation.search.GlobalSearchActivity
import com.jerson.hcdc_portal.presentation.subjects.SubjectKt
import com.jerson.hcdc_portal.presentation.subjects_offered.SubjectOfferedFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcademicsKt : Fragment() {

    private var _binding: FragmentAcademicsKtBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcademicsKtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabTitles = listOf(
            getString(R.string.tab_schedule),
            getString(R.string.tab_grades),
            getString(R.string.tab_history),
            getString(R.string.tab_evaluation),
            getString(R.string.tab_offerings)
        )

        val pagerAdapter = AcademicsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(context, GlobalSearchActivity::class.java))
        }
    }

    inner class AcademicsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SubjectKt()
                1 -> GradesKt()
                2 -> EnrollHistoryKt()
                3 -> ProspectusFragment()
                4 -> SubjectOfferedFragment()
                else -> SubjectKt()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
