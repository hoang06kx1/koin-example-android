# nbl-survey
> Just another endless surveys...

This is a demo application demonstrating how a good covering test could help you in writing less error-prone code.
Written in Kotlin. (**Java**? sorry, I'm tired of null check)

## Tools
 - DI: Koin (*should stick back to Dagger2..explain below*)
 - Viewpager2 
 - Circle indicator
 - Android Architecture Components (*LiveData, Viewmodel*)
 - Retrofit, OkHttp, Glide
 - RxJava, RxBinding, RxAndroid
 - Leak canavary (*for auto detecting leaks*)
 - Customactivityoncrash (*let's give user a bug when their app crash instead of boring messages*)
 - Toasty (*more fancy Toast*)
 - KProgressHUD
 
## Test
 - AndroidX Test (*Deserve to be the next big thing in Anroid developers struggle life, I believe*)
 - Espresso 
 - Robolectric 
 - Specially thanks to: [**Kluent**](https://github.com/MarkusAmshove/Kluent) and [**Barista**](https://github.com/SchibstedSpain/Barista) - *the guy who serve great Espresso* saved me from nightmare of **Hamcrest**

## Technical issues

### Koin

Work pretty well, add and wire dependencies smooth and intuitive at first sight. When come to writing test, everything just collapses suddenly. Can not figure out how to override some dependency in test environment plus with lacking and ambiguous document makes me feel regret about choosing it over Dagger2.

And one important thing: Dagger2 error is notified at compile-time, but Koin is at runtime. With Koin, you need to check your app more carefully.

And circular dependency seems a problem. I used a *Holder* as a workaround.

### KProgressHUD
More beautiful and easier to implement compared to Shimmer.
BUT, makes all the UI test fail continuously. Maybe it still left on the screen after the dismiss function called. I have to use **CoutingIdlingResource**.
And...don't sure whether it need to test the dialog? What's best way to do?

### Viewpager2
Now you can use RecyclerView.Adapter for Viewpager. Awesome. Google should have shipped this feature with Viewpager on early days.

### AndroidX Test
At last, 	unified API between unit test and UI test (integration-test). Test codes (unit & UI) can be put in a shared folder now. Robolectric works out of the box. Nitrogen project is worth waiting for.
BUT, ScenarioActivity.onActivity{} callback never called in my test, the test just keep running forever.
Googled give no clues, may I the only one?
I have to use a static variable to hold a *Weak Reference* to MainActivy for testing, which could be abused using... 

### Espresso
Boring and tricky as usual.

### MockWebServer
Reliable and unstable at the same time. Lol.

### FAQ
 - **Where is license part?**
      - What is the license? 
 - **Open issues and pull request?**
      - You are welcome. I love so. 



