package com.edapp.habittracker.ui
import android.os.Build
import android.util.Log
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    // Tags currently used to FILTER the main habit list (independent from the
    // per-habit tag selection used while editing a habit — see addOrRemoveTag).
    private val _selectedTags = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTags: StateFlow<Set<Long>> = _selectedTags

    // True until the habits list has emitted its first real value from the database.
    // Lets the UI tell "still loading" apart from "loaded and genuinely empty".
    private val _isLoadingHabits = MutableStateFlow(true)
    val isLoadingHabits: StateFlow<Boolean> = _isLoadingHabits

    @RequiresApi(Build.VERSION_CODES.O)
    val habits = _selectedTags
        .flatMapLatest { tags ->
            repository.getAllHabits(tags.toList())
        }
        .onEach { _isLoadingHabits.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _updateHabit  = MutableStateFlow<UpdateHabit>(UpdateHabit())
    private val _allTags  = MutableStateFlow<List<HabitTag>>(emptyList())

    val editOrAddHabit = _updateHabit
    val allTags = _allTags

    init {
        viewModelScope.launch {
//            repository.insertDummyHabits(3)
//            repository.insertDummyHabitsWithLogs(5,30)
        }
        getAllTag()
    }

    fun setTags(tags: List<HabitTag>) {
        _allTags.value = tags
    }

    /** Toggle a tag in the main-list FILTER selection. */
    fun toggleTag(tagId: Long) {
        _selectedTags.value = _selectedTags.value.toMutableSet().apply {
            if (contains(tagId)) {
                remove(tagId)
            } else {
                add(tagId)
            }
        }
    }

    fun clearSelection() {
        _selectedTags.value = emptySet()
    }

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
        uncheckedColorValue: Color? = null,
        isArchived: Boolean? = null,
        isLocked: Boolean? = null
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

        isArchived?.let {
            _updateHabit.value = _updateHabit.value.copy(isArchived = it)
        }

        isLocked?.let {
            _updateHabit.value = _updateHabit.value.copy(isLocked = it)
        }
    }

    fun saveHabit() {
        viewModelScope.launch {
            repository.insertOrUpdateHabit(_updateHabit.value)
            _updateHabit.value = UpdateHabit()
        }
    }


    fun getAllTag(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllHabitsTag().collect {
                try{ _allTags.value = it ?: emptyList() }
                catch (e: Exception){
                    Log.d("12345678", "getAllTag: Error fetching tags: ${e.message}")
                }
            }
        }
    }

    /** Toggle a tag on the habit currently being created/edited (per-habit selection). */
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

    fun setHabitArchived(habitId: Long, isArchived: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setHabitArchived(habitId, isArchived)
        }
    }

    fun updateTodayProgress(habitStatus: HabitStatusEnum, habitOwnerId: Long ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val todayEpochDat = LocalDate.now().toEpochDay()
                repository.updateHabitStatus(HabitLog(habitOwnerId, todayEpochDat, habitStatus))
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

    private val _isRowView: MutableStateFlow<Boolean> = MutableStateFlow(PreferenceUtil.isRowView())
    val isRowView: StateFlow<Boolean> = _isRowView


    fun setIsRowView(isRowView: Boolean) {
        PreferenceUtil.setIsRowView(isRowView)
        _isRowView.value = PreferenceUtil.isRowView()
    }

    fun setAsLocked(habitId: Long, passKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setHabitLocked(habitId, true, passKey)
        }
    }

    fun loadHabitForEditing(habitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val habitEntity = repository.getHabitById(habitId)
            habitEntity?.let {
                val updateHabit = UpdateHabit(
                    title = it.title,
                    description = it.description,
                    selectedHabitConsistencyIcon = it.consistencyIconName ?: "Star",
                    selectedHabitIcon = it.iconName ?: "Work",
                    reminderList = it.reminders,
                    oldHabitDbPrimaryKey = it.habitId,
                    isNewHabit = false,
                    color = Color(it.colorValue.toULong()),
                    uncheckedColorValue = Color(it.uncheckedColorValue.toULong()),
                    tagIds = it.tagId?.toSet() ?: emptySet(),
                    isArchived = it.isArchived,
                    isLocked = it.isLocked
                )
                _updateHabit.value = updateHabit
            }
        }
    }

    fun resetHabitEditState() {
        _updateHabit.value = UpdateHabit()
    }

    fun deleteTag(tag: HabitTag) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTag(tag.tagId)
                // Refresh tags list after deletion
                getAllTag()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
