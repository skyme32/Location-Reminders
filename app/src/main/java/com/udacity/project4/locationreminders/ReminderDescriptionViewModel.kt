package com.udacity.project4.locationreminders

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(
        val app: Application,
        private val dataSource: ReminderDataSource)
    : BaseViewModel(app) {

    /**
     * Save the reminder to the data source
     */
    fun deleteReminder(reminderData: ReminderDataItem) {
        viewModelScope.launch {
            dataSource.deleteReminder(
                    ReminderDTO(
                            reminderData.title,
                            reminderData.description,
                            reminderData.location,
                            reminderData.latitude,
                            reminderData.longitude,
                            reminderData.id
                    )
            )
        }
    }

    // Add var and methods to support loading URLs
    val urlIntent = MutableLiveData<String>()
    fun loadingURLs(uri: ReminderDataItem?) {
        urlIntent.value = convertAddress(uri)
    }

    private fun convertAddress(address: ReminderDataItem?): String {
        val strAddress = StringBuilder()
        strAddress.append("geo:0,0?q=")
        if (!address?.latitude.toString().isNullOrBlank()) strAddress.append("${address?.latitude},")
        if (!address?.longitude.toString().isNullOrBlank()) strAddress.append("${address?.longitude}")
        return strAddress.toString()
    }


}