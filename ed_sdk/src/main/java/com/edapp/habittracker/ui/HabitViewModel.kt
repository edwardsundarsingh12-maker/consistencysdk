package com.edapp.habittracker.ui
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edapp.habittracker.data.HabitRepository
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitLog
import com.edapp.habittracker.domain.HabitTag
import com.edapp.habittracker.domain.ReminderData
import com.edapp.habittracker.domain.UpdateHabit
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    val todayEpochDat = LocalDate.now().toEpochDay()
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    init {
        viewModelScope.launch {
//            repository.insertDummyHabits(3)
//            repository.insertDummyHabitsWithLogs(5,30)
        }
        loadHabits()
        getAllTag()
    }

    private val _isRowView: MutableStateFlow<Boolean> = MutableStateFlow(PreferenceUtil.isRowView())
    val isRowView: StateFlow<Boolean> = _isRowView

    private val _updateHabit  = MutableStateFlow<UpdateHabit>(UpdateHabit())
    private val _allTags  = MutableStateFlow<List<HabitTag>>(emptyList())



    val editOrAddHabit = _updateHabit
    val allTags = _allTags

    fun addReminder(reminder: ReminderData) {
        val reminders = (_updateHabit.value.reminderList ?: emptyList()).toMutableList()
        reminders.add(reminder)
        _updateHabit.value = _updateHabit.value.copy(reminderList = reminders)
    }

    fun deleteReminder(reminder: ReminderData) {
        val reminders = (_updateHabit.value.reminderList ?: emptyList()).toMutableList()
        reminders.remove(reminder)
        _updateHabit.value = _updateHabit.value.copy(reminderList = reminders)
    }

    fun updateReminder(newReminder: ReminderData) {
        val reminders = (_updateHabit.value.reminderList ?: emptyList()).toMutableList()
        val index = reminders.indexOfFirst { it.reminderId == newReminder.reminderId }
        if (index != -1) {
            reminders[index] = newReminder
        }
        _updateHabit.value = _updateHabit.value.copy(reminderList = reminders)
    }

    fun updateHabitEditOrNewData(
        title: String? = null,
        description: String? = null,
        selectedHabitConsistencyIcon: String? = null,
        selectedHabitIcon: String? = null,
        color: Color? = null,
        uncheckedColorValue: Color? = null
    ) {
        title?.let {
            _updateHabit.value = _updateHabit.value.copy(title = it)
        }

        description?.let {
            _updateHabit.value = _updateHabit.value.copy(description = it)
        }

        selectedHabitConsistencyIcon?.let {
            _updateHabit.value = _updateHabit.value.copy(selectedHabitConsistencyIcon = it)
        }

        selectedHabitIcon?.let {
            _updateHabit.value = _updateHabit.value.copy(selectedHabitIcon = it)
        }

        color?.let {
            _updateHabit.value = _updateHabit.value.copy(color = it)
        }

        uncheckedColorValue?.let {
            _updateHabit.value = _updateHabit.value.copy(uncheckedColorValue = it)
        }


    }

    fun saveHabit() {
        viewModelScope.launch {
            repository.insertNewHabit(_updateHabit.value)
            _updateHabit.value = UpdateHabit()
        }
    }


    fun getAllTag(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllHabitsTag().collect {
                _allTags.value = it ?: emptyList()
            }
        }
    }

    fun addOrRemoveTag(tagId: Long) {
        _updateHabit.value = _updateHabit.value.copy(
            tagIds = _updateHabit.value.tagIds.toMutableSet().apply {
                if (contains(tagId)) {
                    remove(tagId) // remove if already present
                } else {
                    add(tagId) // add if not present
                }
            }
        )
    }

    fun insertNewHabitTag(newTag: HabitTag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHabitTag(newTag)?.let {
                addOrRemoveTag(newTag.tagId)
            }
        }
    }

    fun loadHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            val habits = repository.getAllHabits()
            habits.collect {
                _habits.value = it
            }
        }
    }

    fun updateTodayProgress(habitStatus: HabitStatusEnum, habitOwnerId: Long ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                repository.updateHabitStatus(HabitLog(habitOwnerId,todayEpochDat, habitStatus))
            }
        }
    }

    fun updateProgressByEpoDay(habitStatus: HabitStatusEnum, habitOwnerId: Long , epochDay: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                repository.updateHabitStatus(HabitLog(habitOwnerId,epochDay, habitStatus))
            }
        }
    }




    fun setIsRowView(isRowView: Boolean) {
        PreferenceUtil.setIsRowView(isRowView)
        _isRowView.value = PreferenceUtil.isRowView()
    }
}
