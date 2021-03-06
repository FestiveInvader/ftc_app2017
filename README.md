
# Garfield Robotic's ftc_app2017
Welcome to our code for 2017! This repo contains code for both FTC Team 4042 "Non-Standard Deviation" and for Team 12788 "Ultraviolet".

This is an Android Studio project to create the FTC Robot Controller app.

# General Instructions

These instructions are applicable for nearly any GitHub project, but our project has its own quirks and oddities so please read through the directions carefully.

## Garfield Robotics members

To get a local copy of the repo which can accept your changes, go to [our GitHub page](https://github.com/dawgbotics/ftc_app2017), then click the green **Clone or download** button. Copy the URL which appears ("https://github.com/dawgbotics/ftc_app2017.git").

Download [Git Bash](http://gitforwindows.org/) onto your computer and open it. Use standard Windows Shell commands (`cd`, `dir`, `ls`, etc.) to navigate to a folder you want to put the project in. Use `git clone`, then right click and paste your url and hit enter. You should see something like this:
```
$ git clone https://github.com/dawgbotics/ftc_app2017.git
Cloning into 'ftc_app2017'...
remote: Counting objects: 11354, done.
remote: Compressing objects: 100% (307/307), done.
remote: Total 11354 (delta 286), reused 546 (delta 168), pack-reused 10676
Receiving objects: 100% (11354/11354), 574.00 MiB | 3.40 MiB/s, done.
Resolving deltas: 100% (6038/6038), done.
Checking connectivity... done.
Checking out files: 100% (2246/2246), done.
```
Now you've got the code on your computer! Download and open [Android Studio](https://developer.android.com/studio/index.html). Choose **Open Existing Android Studio Project**. Navigate to `ftc_app2017` on your computer. _DO NOT OPEN FTC_APP2017_, since that doesn't contain the code. Instead, open `ftc_app-master` _inside_ of `ftc_app2017`. Now open this project. You will be prompted to download Build Tools. Click the link to do so.

You will be prompted to update the gradle project. _DO NOT UPDATE THE GRADLE PROJECT._ This is very important, as our gradle is very fragile and doesn't work under the newer versions.

Congratulations! You should now have the project on your computer.

***
_Note:_ As the repo consists of code for two different teams, the code base is split into `Team4042` and `Team12788` folders. The `TeamCode` folder is unused. To choose which team's code to download, navigate to the top of Android Studio. In the toolbar along the top, next to the play button, there's a dropdown menu with the option to select either Team4042 or Team12788. Select your target and hit play.

***

When you next want to interact with GitHub, you can use [the GitHub cheat sheet](https://drive.google.com/file/d/1HvLFKy5sd9aXeU-pLuVbgeNDCWMS_gs4/view?usp=sharing) to get you started.

## Other teams
If you would like to use our code, feel free to fork the repo using the Fork button in the upper right on [our GitHub page](https://github.com/dawgbotics/ftc_app2017). Be sure to tell us how you used it at contact@garfieldrobotics.com!

If you want to contribute to our project, go through the [Garfield Robotics members](#garfield-robotics-members) steps to download a local copy of the repo. Submit a pull request when you're done and we'll consider it!

# Features

A quick summary of the features of each team's code.

## Team 4042

### Autonomous

 - Uses OpenCV and Vuforia to identify the pictograph and opposing alliance's jewel
 - Reads instructions from files
 - Uses an array of IRs and sonars to navigate the field
 - Every movement uses a PID controller
 - Uses a gyroscope to identify when we've driven off the balancing stone
 - Autonomously opens the intakes
 - Autonomously collects glyphs and aligns them

### Teleop
- Glyph placing system is entirely autonomous. The operator selects the target location, then the glyph placer indexes vertically using encoders and horizontally using limit switches, then places the glyph and indexes back to the home position - all automatically.
- Four different glyph placing modes (fast, automated, manual, and reset)
- Four different drive modes (regular, regular tank, extendo, and extendo crawl)
- _The robot gets really long._