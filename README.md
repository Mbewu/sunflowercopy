# sunflowercopy

sunflowercopy is an android app that I use to investigate features of Android programming that I learn.

It is based on the sunflower demo provided by google [https://github.com/android/sunflower], initially as a way to test some of my skills. The goal was to recreate the app without looking at the demo code or too much at the app. Then to implement skills learnt in Google codelabs and elsewhere as they are learnt.

Currently looking at neatening things up a bit and including more testing (unit testing and functional testing).

It is close to where I view completion and I may start another app with more real-world use soon (basically to track and identify plant observations in a particular area), so stay tuned.

## Features:

- add plants from a repository to a garden on a Google map
- choose where to plant plant
- need to water plants periodically before harvesting
- notifications sent when ready to water or harvest or just planted
- ability to set up polygon shaped garden overlaid on map
- different profile with different gardens
- settings like music on/off, mapstyle etc saved in profile
- add/remove multiple plants at a time
- ability to take pictures of individual plants to monitor

## Updates:

23/04/2021:
- many changes have been made
- primarily (recently) fixed navigation with the drawerlayout and different styles of toolbar depending on the fragment.

07/10:
- speed up build a bit by adding some things to gradle.properties

30/09:
- changed the themes a little, created a base theme AppThemeBase that AppTheme inherits. In this way AppTheme can inherit attributes that may not be needed to change when you go from day to night.
- fixed html binding adapter to have links clickable.
- made info window on map look better in dark mode

21/09:
- changed garden plant's ids to be auto generated, this meant changing them to Long and a number of other changes propagated through the code. May be some issues.

21/09:
- cleaned up code a bit (changed string concatenation style to use interpolation)
- added a bit more databinding

15/09:
- updated dependencies based on AndroidStudio recommendations

11/09/2020:
- renamed PlantInformation2 to Plant
- added more defaults to Plant (only id and name needed now)
- removed unused files
- replaced all Log calls with Timber
- changed so that using Room's coroutine functionality suspend functions
- changed so that database is only read from file once using a worker


## Known bugs/feature improvements:

23/04:
- sunflower starts spinning every time you press "stop music".
- music can't play unless you're logged in.
- when you log in the first time, it asks you if you want "project-24381741" to be logged into and/or access some stuff. Would be nice to change that name so something better.
- pictures take ages to load. not soo bad, but yeah. maybe a faster type would be better.
- some aren't formatted correctly, e.g. apple. but sometimes they are, some had error loading.
- add plant to garden dialog doesn't look so nice.
- when change from growing to ready to water the dialog box on the map closes.
- apple planted a long time ago that is ready to harvest is shown as still growing.
- notification doesn't go directly to the plant (this not really a bug, just lack of functionality).
- app closes when clicking start after entering via notification while app was visible. okay sometimes it does go to the correct plant maybe?? but then yeah dies.
- layout of text in plant details and planted fragments needs to be improved
- search is for anywhere in the middle of the word, would be nice if that was highlighted to avoid confusion
- only 14 plants are visible on the list, there should be 17 according to the count. sometimes it's fine? not sure about this one
- multiple selection option isn't directed enough perhaps? or maybe not.
- the dialog for adding multiple plants isn't written correctly. the yes button isn't there (visible), but it does work and sets them randomly around the home location (which needs to be changed to be set by the user, or use current location, or use the garden that has been set up)
- after coming back from an image into the plant page the wikipedia link isn't there. it's just regular text.
- the delete fab and plant fab only comes up when you scroll up.
- not loading different gardens and planted plants for different logins (e.g. logged in and logged out)
- music starts again pretty much every time you go back to the start screen.
- going to settings from map options menu doesn't work.
- clock text showing time until next watering is white
- zoom in on garden on map
- have different garden and plant list for different users
- make pins on map show correct order, e.g. when placing pins for garden shape they should go in front of plant pins
