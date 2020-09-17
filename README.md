# EasyNews
[![platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=23)
[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](https://opensource.org/licenses/MIT)

 EasyNews is a newsfeed app that allows you to read, and share the news from your location (town) or from the other world. 
 It is my first Android App made using Kotlin.

<a name="tasks"></a>
## Assigned tasks
- Load news from [NewsAPI](https://newsapi.org/)
- Ability to search news by keyword, language, sorted by popularity or date of publication
- Visualize news headlines organized in two categories: Local news and World news
- 2 languages supported (EN, RU)
- Show news with the following details :
    - News image
    - News Title
    - News Description
    - Publication Date
- Using News API to retrieve news that matches the search
- Open news links via WebView
- Change brightness and font in WebView
- Share links to news
- Save/Delete Bookmarks in database with Room

<a name="screenshots"></a>
## Screenshots

<table style="width:100%">
  <tr>
    <th>1. Local news</th>
    <th>2. World news</th>
    <th>3. Search</th>
    <th>4. Bookmarks</th>
    <th>5. News details</th>
  </tr>
  <tr>
    <td><img src="https://github.com/N-1-K-k-1/EasyNews/blob/master/screenshots/1.png"/></td>
    <td><img src="https://github.com/N-1-K-k-1/EasyNews/blob/master/screenshots/2.png"/></td>
    <td><img src="https://github.com/N-1-K-k-1/EasyNews/blob/master/screenshots/3.png"/></td>
    <td><img src="https://github.com/N-1-K-k-1/EasyNews/blob/master/screenshots/4.png"/></td>
    <td><img src="https://github.com/N-1-K-k-1/EasyNews/blob/master/screenshots/5.png"/></td>
  </tr>
   </table>

<a name="tools"></a>
## Languages, libraries and tools used

 * [Kotlin](https://kotlinlang.org/)
 * [AndroidX libraries](https://developer.android.com/jetpack/androidx)
 * [Android LifeCycle](https://developer.android.com/topic/libraries/architecture)
 * [Material Components for Android](https://github.com/material-components/material-components-android) 
 * [Retrofit2](https://github.com/square/retrofit)
 * [PaperDb](https://github.com/pilgr/Paper)
 * [DiagonalLayout](https://github.com/florent37/DiagonalLayout)
 * [KenBurnsView](https://github.com/flavioarfaria/KenBurnsView)
 * [RecyclerView Animators](https://github.com/wasabeef/recyclerview-animators)
 * [Spots progress dialog](https://github.com/dybarsky/spots-dialog)
 * [Picasso](https://github.com/square/picasso)
 * [CircleImageView](https://github.com/hdodenhof/CircleImageView)
 * [Android-ago](https://github.com/curioustechizen/android-ago)
 * [Adblock Android SDK](https://github.com/adblockplus/libadblockplus-android)
 
<a name="requirements"></a>
## Requirements
- min SDK 23

<a name="installation"></a>
## Installation

- Clone the app and import to Android Studio.
``git clone https://github.com/N-1-K-k-1/EasyNews.git``  
You'll need to provide API key to fetch the news from the News Service (API). Currently the news is fetched from [NewsAPI](https://newsapi.org/)
- Generate an API key (It's only 2 steps!) from [NewsAPI](https://newsapi.org/)
- Go to file /interface/NewsService.kt in our project root folder
- Change the API in the annotation that shown below:
```
    @Headers("X-Api-Key: Your API key")
```
- Build the app 


<a name="license"></a>
## License

MIT License
```
Copyright (c) [2020] [Viacheslav Proshkin]
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
