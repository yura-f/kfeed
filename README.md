# kfeed
The MVVM app that enables you to track your walk with images.

![kfeed_screen](https://user-images.githubusercontent.com/229530/173307748-3a426c23-cf4c-4fa3-9b34-8f924c394fd7.jpg)

**The task:**

The user opens the app and presses the start button. After that the user puts their phone into their pocket and starts walking. The app requests a photo from the public flickr photo search api for his location every 100 meters to add to the stream. New pictures are added on top. Whenever the user takes a look at their phone, they see the most recent picture and can scroll through a stream of pictures which shows where the user has been. It should work for at least a two-hour walk. The user interface should be simple as shown on the left of this page.

**Used software stack:**

 - Kotlin;
 - [Koin](https://github.com/InsertKoinIO/koin);
 - RxJava;
 - Retrofit;
 - [Coil](https://github.com/coil-kt/coil);
