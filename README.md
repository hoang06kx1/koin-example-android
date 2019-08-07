# nbl-survey
> Just another endless surveys...

This is an demo appliation demonstrating how a good coverting test could help you in writing less error-prone code.
Totally written in Kotlin. (**Java**? sorry, I'm tired of null check)

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
 
## Test
 - AndroidX Test (*Deserve to be the next big thing in Anroid developers struggle life, I believe*)
 - Espresso 
 - Robolectric 
 - Specially thanks to: [**Kluent**](https://github.com/MarkusAmshove/Kluent) and [**Barista**](https://github.com/SchibstedSpain/Barista) - *the guy who serve great Espresso* saved me from nightmare of **Hamcrest**

## Technical issues

### Koin

Work pretty well, add and wire dependencies smooth and intuitive at first sight. When come to writing test, everything just collapse suddenly. Can not figure out how to override some dependency in test environment plus with lacking and ambiguous document makes me feel regret about choosing it over Dagger2.

And one important thing: Dagger2 error is notified at compiler time, but Koin is at runtime. With Koin, you need to check your app more carefully.

And circular dependency seems a problem. I used an *Holder* as workaround.

### Viewpager2
Now you can use RecyclerView.Adapter for Viewpager. Awesome. Google should had shipped this feature with Viewpager on early days.

### AndroidX Test
At last, unified API between unit test and UI test (intergration test). Test codes (unit & UI) can be put in a shared folder now. Robolectric works out of the box. Nitrogen project is worth waiting for.

### Espresso
Boring and tricky as usual.

### MockWebServer
Reliable and unstable at a same time. Lol.




