package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //provide testing to the RemindersListViewModel and its live data objects
    // Subject under test
    private lateinit var reminderViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderRepository: FakeDataSource


    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        stopKoin()
        // We initialise the tasks to 3, with one active and two completed
        reminderRepository = FakeDataSource()
        reminderRepository.saveReminder(ReminderDTO(title = "title1", description = "description1", location = "location1", latitude = 1.2, longitude = 12.12))
        reminderRepository.saveReminder(ReminderDTO(title = "title2", description = "description2", location = "location2", latitude = 2.2, longitude = 22.12))
        reminderRepository.saveReminder(ReminderDTO(title = "title3", description = "description3", location = "location3", latitude = 3.2, longitude = 32.12))

        reminderViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderRepository
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun showLoading_existData() = mainCoroutineRule.runBlockingTest {
        // With a repository that has an active task
        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.loadReminders()
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun showNodata_listEmpty() = mainCoroutineRule.runBlockingTest {
        reminderRepository.deleteAllReminders()
        reminderViewModel.loadReminders()

        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(reminderViewModel.showNoData.value, `is`(true))
    }


}