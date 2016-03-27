# UnKnock
Unlock your phone with the power of knock! UnKnock protects your phone using a unique secret knock pattern.<br>
Just place your smartphone on a flat surface, wake up the screen, perform the secret knock and you've got access. Great for sharing with friends or just as a fun and unique alternative to smart locks.
# How to use
When you fire up the app for the first time, it's automatically going into record pattern mode, which means that it will listen for knocks and save those as your secret pattern. In case you ever want to change that pattern, there is a button conveniently placed on the bottom of the screen that lets you do exactly that. The app will tell you how many knocks it's registered so you can be sure that your pattern was recorded. After that, the app's service keeps running in the background and automatically comes up every time you wake up your phone. To get access, just perform the secret knock.
# How it's made
This is an Android app, so it was developed completely inside Android Studio. The code was written in Java and the layouts and rules are in XML. The app listens for unusual spikes in vibration and records them along with their timestamp. Using a statistical algorithm, the app compares the proportions in duration between each two consecutive knocks and compares them to the recorder pattern. If they are close enough, the phone is unlocked.
