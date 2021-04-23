# sunflowercopy
Sunflower copy is an android app that I use to investigate features of Android programming that I learn.

It is based on the sunflower demo provided by google, initially as a way to test some of my skills.

I am currently working on implementing some tests using skills learnt in the Advanced Android in Kotlin codelabs.

Updates:
10/07
-> speed up build a bit by adding some things to gradle.properties
09/30
-> changed the themes a little, created a base theme AppThemeBase that AppTheme inherits. In this way AppTheme can inherit attributes that may not be needed to change when you go from day to night.
-> fixed html binding adapter to have links clickable.
-> made info window on map look better in dark mode

09/21
-> changed garden plant's ids to be auto generated, this meant changing them to Long and a number of other changes propagated through the code. May be some issues.

09/21
-> cleaned up code a bit (changed string concatenation style to use interpolation)
-> added a bit more databinding

09/15
-> updated dependencies based on AndroidStudio recommendations

2020/09/11
-> renamed PlantInformation2 to Plant
-> added more defaults to Plant (only id and name needed now)
-> removed unused files
-> replaced all Log calls with Timber
-> changed so that using Room's coroutine functionality suspend functions
-> changed so that database is only read from file once using a worker
