package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //provide testing to the SaveReminderView and its live data objects
    // Subject under test
    private lateinit var reminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderRepository: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        // We initialise the tasks to 3, with one active and two completed
        reminderRepository = FakeDataSource()
        reminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderRepository
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun addNewReminder_setNewReminder() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = "title", description = "description",
            location = "location", latitude = 2.2, longitude = 12.12
        )

        // Then the new task event is triggered
        reminderViewModel.validateAndSaveReminder(reminder)
        assertThat(reminderRepository.getReminder(reminder.id), not(nullValue()))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun completeReminder_dataAndToast() = mainCoroutineRule.runBlockingTest {
        // With a repository that has an active task
        val reminder = ReminderDataItem(
            title = "title", description = "description",
            location = "location", latitude = 2.2, longitude = 12.12
        )

        reminderViewModel.validateAndSaveReminder(reminder)

        // The showToast is save it
        val toastText =  reminderViewModel.showToast.getOrAwaitValue()
        assertEquals(toastText, (ApplicationProvider.getApplicationContext() as Application).getString(R.string.reminder_saved))

        val navigation =  reminderViewModel.navigationCommand.getOrAwaitValue()
        assertEquals(navigation, NavigationCommand.Back)
    }


}

