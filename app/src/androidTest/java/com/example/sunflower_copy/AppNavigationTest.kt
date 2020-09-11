package com.example.sunflower_copy

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.sunflower_copy.util.*

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
        onView(withId(R.id.drawer_layout))
            .check(matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer is closed.

        // 2. Open drawer by clicking drawer icon.
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // 3. Check if drawer is open.
        onView(withId(R.id.drawer_layout))
            .check(matches(DrawerMatchers.isOpen(Gravity.START))) // Left drawer is open.


        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }



    @Test
    fun navigateToPlantList_checkPlantListLoaded() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Navigate to plant list
        navigateToPlantList()

        // 2. Check whether all 17 plants have loaded and displayed in the title
        onView(withId(R.id.photos_grid_search))
            .check(matches(atPosition(0, hasDescendant(withText("17")))))

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }

    private fun navigateToPlantList() {

        // 1. Click the start button
        onView(withId(R.id.button_start)).perform(click())

        // 2. Click the Plants tab
        onView(withId(R.id.tabs)).perform(selectTabAtPosition(1))
        //R.string.tab_text_1
        //onView(ViewMatchers.withText("PLANT LIST")).perform(click())

    }



    @Test
    fun navigateToPlantList_selectTomatoNoScroll() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Navigate to plant list
        navigateToPlantList()

        // 2. Select Tomato
        onView(withId(R.id.photos_grid_search))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Tomato")), click()))

        // 3. check Tomato detail fragment has opened
        onView(withId(R.id.title)).check(matches(withText("Tomato")))

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }



    @Test
    fun navigateToPlantList_selectHibiscusWithScroll() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Navigate to plant list
        navigateToPlantList()

        // 2. Select Tomato
        onView(withId(R.id.photos_grid_search))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Hibiscus")), click()))

        // 3. check Tomato detail fragment has opened
        onView(withId(R.id.title)).check(matches(withText("Hibiscus")))

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }


    @Test
    fun navigateToPlantList_selectHibiscusWithScrollAndAdd_navigateToMapFrag() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Navigate to plant list
        navigateToPlantList()

        // 2. Select Tomato
        onView(withId(R.id.photos_grid_search))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Hibiscus")), click()))

        // 3. check Hibiscus detail fragment has opened
        onView(withId(R.id.title)).check(matches(withText("Hibiscus")))

        // 4. select add plant frag
        onView(withId(R.id.add_plant_fab)).perform(click())


        // 5. select yes in dialog
        onView(withId(R.id.button_positive)).perform(click())

        // 6. check we are in the map fragment, should have instructions
        onView(withId(R.id.instructions_title)).check(matches(withText("Place plant on the map")))



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