# Project Bumblebee (Sarwar) - Client, Admin and Delivery Side App
User-side, Administration-side and Delivery-sided android app for trust 'SARWAR' - seamless and hassle free way of accepting donations from general users.
## Getting Started
The following instuctions will get you a copy of the project up and running on your local machine for development and testing purpose.
### Prerequisites
What things you need to install the software
```
1. Latest version of android studio should be set-up and running on your system.
2. Add Firebase to your project. (https://firebase.google.com/docs/android/setup)
3. And finally, an android device (or emulator) running on Jelly Bean or higher.
```
### Setting up Firebase - You have to add your own 'google-services.json' file
By now, you would have already downloaded the "google-services.json" file and connected your app to Firebase Server. Finally, to set the server side Authentication, Realtime Database and Storage provided by Google Firebase, follow the steps below:
1. Check for the latest dependencies in app-level gradle file
2. Use the rules, as provided, for the Firebase Database, Cloud Firestore and Firebase Storage
3. Upload the provided server sided code into Firebase Functions
4. For Authentication, "Email/Password", "Phone", "Google Sign In" and "Facebook Sign In" are used
### Installing the App
Import the app to Android Studio, build the project and finally deploy it in a device (or emulator).
##  Build With
* [Google Firebase](https://firebase.google.com/)
* [Glide](https://github.com/bumptech/glide)
## Authors
* **Jyotirmoy Paul** - Initial work - [jyotirmoy-paul](https://github.com/jyotirmoy-paul)
