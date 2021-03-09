package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private val _viewModel: ReminderDescriptionViewModel by inject()
    private lateinit var reminder: ReminderDataItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_reminder_description
        )
        binding.viewModelDescription = _viewModel


        val bundle: Bundle? = intent.extras
        if (bundle?.getSerializable(EXTRA_ReminderDataItem) != null) {
            _viewModel.isGeofence.value = false
            reminder = bundle.getSerializable(EXTRA_ReminderDataItem) as ReminderDataItem
            ondelete()
        } else {
            _viewModel.isGeofence.value = true
            reminder = ReminderDescriptionActivityArgs.fromBundle(bundle!!).remindItem
        }


        binding.reminderDataItem = reminder
        binding.button2.setOnClickListener {
            ondelete()
            onback()
        }


        supportActionBar?.title = getString(R.string.reminder_details)
    }


    private fun ondelete() {
        _viewModel.deleteReminder(reminder)
    }

    
    private fun onback() {
        finish()
        val intent = Intent(this, RemindersActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
