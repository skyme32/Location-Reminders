package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                TestCoroutineDispatcher()
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminderretrievesReminder() = runBlocking {
        //Create a riminder
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 2.2,
            longitude = 12.12
        )
        localDataSource.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same task is returned
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }


    @Test
    fun deletedByIdReminder() = runBlockingTest {
        //Create a riminder
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 2.2,
            longitude = 12.12
        )

        // GIVEN - insert and delete Reminder
        localDataSource.saveReminder(reminder)
        localDataSource.deleteReminder(reminder)

        // WHEN  - Reminder retrieved by ID
        val result = localDataSource.getReminder(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(result is Result.Success, `is`(false))
    }


    @Test
    fun deletedAllReminder() = runBlockingTest {
        //Create a riminder
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 2.2,
            longitude = 12.12
        )

        // GIVEN - insert a task
        localDataSource.saveReminder(reminder)
        localDataSource.deleteAllReminders()


        // WHEN - Get the task by id from the database
        val result = localDataSource.getReminders()
        result as Result.Success

        // THEN - The loaded data contains the expected values
        assertThat(result.data, `is`(emptyList()))
    }

}