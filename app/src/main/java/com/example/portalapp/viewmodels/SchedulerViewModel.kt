package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.SchedulerRepository
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.ClassScheduleItem
import com.example.portalapp.models.LabBooking
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val repo: SchedulerRepository
) : ViewModel() {

    data class UiState(
        val selectedTab: Int = 0, // 0=Lab, 1=Class, 2=Assessments
        val semester: Int = 1,

        val lab: List<LabBooking> = emptyList(),
        val labLoading: Boolean = false,
        val labError: String? = null,

        val classes: List<ClassScheduleItem> = emptyList(),
        val classLoading: Boolean = false,
        val classError: String? = null,

        val assessments: List<Assessment> = emptyList(),
        val assessLoading: Boolean = false,
        val assessError: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        refreshAll()
    }

    fun onTabChange(index: Int) = _ui.update { it.copy(selectedTab = index) }

    fun setSemester(sem: Int) {
        _ui.update { it.copy(semester = sem) }
        refreshClass()
        refreshAssessments()
    }

    fun refreshAll() {
        refreshLab()
        refreshClass()
        refreshAssessments()
    }

    fun refreshLab() = viewModelScope.launch {
        _ui.update { it.copy(labLoading = true, labError = null) }
        when (val res = repo.labBookings()) {
            is Result.Success -> _ui.update { it.copy(lab = res.value, labLoading = false) }
            is Result.Error   -> _ui.update { it.copy(labLoading = false, labError = res.message) }
        }
    }

    fun refreshClass() = viewModelScope.launch {
        val sem = _ui.value.semester
        _ui.update { it.copy(classLoading = true, classError = null) }
        when (val res = repo.classSchedule(sem)) {
            is Result.Success -> _ui.update { it.copy(classes = res.value, classLoading = false) }
            is Result.Error   -> _ui.update { it.copy(classLoading = false, classError = res.message) }
        }
    }

    fun refreshAssessments() = viewModelScope.launch {
        val sem = _ui.value.semester
        _ui.update { it.copy(assessLoading = true, assessError = null) }
        when (val res = repo.assessmentSchedule(sem)) {
            is Result.Success -> _ui.update { it.copy(assessments = res.value, assessLoading = false) }
            is Result.Error   -> _ui.update { it.copy(assessLoading = false, assessError = res.message) }
        }
    }
}
