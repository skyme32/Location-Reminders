package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        private const val TAG = "ReminderDescription"
        internal const val ACTION_GEOFENCE_EVENT = "locationreminders.action.ACTION_GEOFENCE_EVENT"

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

    // Version of ANDROID Q
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_reminder_description
        )
        binding.viewModelDescription = _viewModel
        binding.lifecycleOwner = this


        val bundle: Bundle? = intent.extras
        reminder = if (bundle?.getSerializable(EXTRA_ReminderDataItem) != null) {
            bundle.getSerializable(EXTRA_ReminderDataItem) as ReminderDataItem
        } else {
            ReminderDescriptionActivityArgs.fromBundle(bundle!!).remindItem
        }

        binding.reminderDataItem = reminder
        binding.button2.setOnClickListener {
            removeGeofences()
        }

        supportActionBar?.title = getString(R.string.reminder_details)
    }


    private fun ondelete() {
        _viewModel.deleteReminder(reminder)
    }

    
    private fun onback() {
        finish()
        val intent = Intent(this, RemindersActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /*
    *  Determines whether the app has the appropriate permissions across Android 10+ and all other
    *  Android versions.
    */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
    }


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved())
            return


        val geofencingClient = LocationServices.getGeofencingClient(this)
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, getString(R.string.delete))
                ondelete()
                onback()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofence_not_available))
                ondelete()
                onback()
            }
        }
    }

}
