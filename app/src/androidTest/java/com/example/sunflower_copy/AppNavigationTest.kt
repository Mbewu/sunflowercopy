package com.example.sunflower_copy

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository
import com.example.sunflower_copy.util.DataBindingIdlingResource
import com.example.sunflower_copy.util.EspressoIdlingResource
import com.example.sunflower_copy.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    private lateinit var gardenRepository: GardenRepository
    private lateinit var plantRepository: PlantRepository

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Before
    fun init() {
        gardenRepository = ServiceLocator.provideGardenRepository(
            ApplicationProvider.getApplicationContext())
        plantRepository = ServiceLocator.providePlantRepository(
            ApplicationProvider.getApplicationContext())

        runBlocking {
            gardenRepository.clearGarden()
            plantRepository.clearPlants()
        }

    }


    @After
    fun reset() {
        ServiceLocator.resetGardenRepository()
        ServiceLocator.resetPlantRepository()
    }


    @Test
    fun titleScreen_clickOnStartButton_opensMyGarden() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Check that left drawer is closed at startup.
        Espresso.onView(withId(R.id.drawer_layout))
            .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer is closed.

        // 2. Open drawer by clicking drawer icon.
        Espresso.onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(ViewActions.click())

        // 3. Check if drawer is open.
        Espresso.onView(withId(R.id.drawer_layout))
            .check(ViewAssertions.matches(DrawerMatchers.isOpen(Gravity.START))) // Left drawer is open.


        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }



    fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
            : String {
        var description = ""
        onActivity {
            description =
                it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
        }
        return description
    }
}