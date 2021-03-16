package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // Create a fake data source to act as a double to the real data source
    //(var tasks: MutableList<Task>? = mutableListOf()) : TasksDataSource
    private var reminders: MutableList<ReminderDTO>? = mutableListOf()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // Return the reminders
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Tasks not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminderDTO = reminders?.firstOrNull {
                it.id == id
        }
        reminderDTO?.let { return Result.Success(it) }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteReminder(reminder: ReminderDTO) {
        reminders?.remove(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}