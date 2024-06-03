package com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel

import androidx.lifecycle.ViewModel
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnrollHistoryViewModel @Inject constructor(
    private val enrollHistoryRepository: EnrollHistoryRepository,
    private val db:PortalDB
):ViewModel() {

}