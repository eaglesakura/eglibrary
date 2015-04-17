# eglibrary-Framework

[@eaglesakura](https://twitter.com/eaglesakura)が関わった開発で利用することを前提としたライブラリです。作者個人が開発を効率化するために開発が行われているため、たまに動作が変わったりします。

分割はそれなりにされていますが、そこそこ適当です。また、依存ライブラリが結構多いです。

普通に使用する場合はForkしたり、dependenciesでバージョンを固定したり、参考にするに留めるほうが良いです。

## Gradle

* Maven Centralに追加するのがメンドウだったので、eglibraryを使用するためにはgithubのリポジトリをbuild.gradleへ追加する必要があります。

### Java / Android-Java Library
* ライブラリを使用する場合は、build.gradleにリポジトリと必要なパッケージを追加します。
<pre>
allprojects {
    repositories {
        // add repository
        maven { url "http://eaglesakura.github.io/maven/" }
    }
}
</pre>
<pre>
    // add library
    compile "com.eaglesakura:eglibrary-java-core:0.2.+"
    compile "com.eaglesakura:eglibrary-java-wrapper:0.2.+"
    compile "com.eaglesakura:eglibrary-android-java-core:0.2.+"
    compile "com.eaglesakura:eglibrary-android-java-wrapper:0.2.+"
    compile "com.eaglesakura:eglibrary-android-material-support:0.2.+"
    compile "com.eaglesakura:eglibrary-android-app-framework:0.2.+"
</pre>

### Gradle Plugin
* Gradle Pluginを利用する場合は、buildscriptにmavenリポジトリとclasspathを追加してください。
<pre>
buildscript {
    repositories {
        // add repository
        maven { url "http://eaglesakura.github.io/maven/" }
    }
    dependencies {
        // add classpath
        classpath "com.eaglesakura:eglibrary-gradle-plugin:0.2.+"
    }
}
</pre>
<pre>
apply plugin: 'com.eaglesakura.android-support'
</pre>

## LICENSE

リポジトリの最新版(ver 0.2.+以降のバージョン)では、プロジェクトの都合に応じて、下記のどちらかを選択してください。0.1.+のバージョンをdependenciesを利用して取得する場合、旧ライセンス(NYSL)として使用できます。

ソースコードは自由に使ってもらって構いませんが、どんな不具合があっても責任は持ちません。

また、ライブラリ内で依存している別なライブラリについては、必ずそのライブラリのライセンスに従ってください。

### アプリ等の成果物で権利情報を表示可能な場合

権利情報の表示を行う（行える）場合、MIT Licenseを使用してください。
<pre>
The MIT License (MIT)

Copyright (c) 2011 @eaglesakura

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
</pre>

### 何らかの理由で権利情報を表示不可能な場合

何らかの事情によりライセンス表記を行えない場合、下記のライセンスで使用可能です。

* 日本語
<pre>
A. 本ソフトウェアは Everyone'sWare です。このソフトを手にした一人一人が、
   ご自分の作ったものを扱うのと同じように、自由に利用することが出来ます。

  A-1. フリーウェアです。作者からは使用料等を要求しません。
  A-2. 有料無料や媒体の如何を問わず、自由に転載・再配布できます。
  A-3. いかなる種類の 改変・他プログラムでの利用 を行っても構いません。
  A-4. 変更したものや部分的に使用したものは、あなたのものになります。
       公開する場合は、あなたの名前の下で行って下さい。

B. このソフトを利用することによって生じた損害等について、作者は
   責任を負わないものとします。各自の責任においてご利用下さい。

C. 著作者人格権は @eaglesakura に帰属します。著作権は放棄します。

D. 以上の３項は、ソース・実行バイナリの双方に適用されます。
</pre>
* English
<pre>
A. This software is "Everyone'sWare". It means:
  Anybody who has this software can use it as if he/she is
  the author.

  A-1. Freeware. No fee is required.
  A-2. You can freely redistribute this software.
  A-3. You can freely modify this software. And the source
      may be used in any software with no limitation.
  A-4. When you release a modified version to public, you
      must publish it with your name.

B. The author is not responsible for any kind of damages or loss
  while using or misusing this software, which is distributed
  "AS IS". No warranty of any kind is expressed or implied.
  You use AT YOUR OWN RISK.

C. Copyrighted to @eaglesakura

D. Above three clauses are applied both to source and binary
  form of this software.
</pre>

## 各種ライブラリ

### eglibrary-java-core

Pure-Javaで記述されたライブラリです。

Androidで実行可能、もしくはJava 1.7相当の機能で記述されています。

### eglibrary-java-geo

緯度経度(GPS)関連のライブラリです。内部に含まれているGeohash.javaはMIT LICENSEで提供されています（後述）。

Androidで実行可能、もしくはJava 1.7相当の機能で記述されています。

* 参考URL
	* [2点間の距離測定](http://perota.sakura.ne.jp/blog/android%E3%81%A7%E3%82%82%E7%B7%AF%E5%BA%A6%E7%B5%8C%E5%BA%A6%E3%81%A72%E7%82%B9%E9%96%93%E3%81%AE%E8%B7%9D%E9%9B%A2%E3%82%92%E5%8F%96%E5%BE%97/)
	* [Geohash library for Java](http://github.com/davetroy/geohash-js/tree/master)
	* [Geohash.java](https://github.com/hakobe/Gotouchi/blob/master/src/jp/hakobe/android/util/Geohash.java)

### eglibrary-java-wrapper

Pure-Java+各種外部ライブラリのラッパーライブラリです。

Androidで実行可能、もしくはJava 1.7相当の機能で記述されています。

* 依存外部ライブラリ
	* com.fasterxml.jackson.core:jackson-core:2.4.+
	* com.fasterxml.jackson.core:jackson-databind:2.4.+

### eglibrary-android-java-core

eglibrary-android-api8をAPI15以降向けに整理・書きなおしたライブラリです。Android SDKのみで動作します。

* minSdkVersion 10

### eglibrary-android-java-wrapper

頻繁に使用するライブラリのラッパーを提供します。

* minSdkVersion 10
* 依存外部ライブラリ
	* com.fasterxml.jackson.core:jackson-core:2.4.+
	* com.fasterxml.jackson.core:jackson-databind:2.4.+
	* org.androidannotations:androidannotations-api:3.0.+
	* com.googlecode.android-query:android-query:+
	* de.greenrobot:greendao:1.3.+
	* com.google.protobuf:protobuf-java:2.5.+

### eglibrary-android-material-support

マテリアルデザインっぽいUIを提供するためのStyleやcolor等をまとめたライブラリです。


* minSdkVersion 10
* 依存外部ライブラリ
	* com.melnykov:floatingactionbutton:1.0.+
	* org.androidannotations:androidannotations-api:3.0.+
	* com.googlecode.android-query:android-query:+

### eglibrary-android-glkit

OpenGL ES、特にEGL周りに関するラッパーを提供するライブラリです。

* minSdkVersion 10

### eglibrary-android-app-framework

アプリ開発で使用する、ActivityやFragment、ネットワーク等のよく使う機能をまとめたフレームワークです。

* minSdkVersion 10
* 依存外部ライブラリ
	* com.fasterxml.jackson.core:jackson-core:2.4.+
	* com.fasterxml.jackson.core:jackson-databind:2.4.+
	* org.androidannotations:androidannotations-api:3.0.+
	* com.googlecode.android-query:android-query:+
	* de.greenrobot:greendao:1.3.+
	* com.google.protobuf:protobuf-java:2.5.+
	* com.mcxiaoke.volley:library-aar:1.0.+@aar

## ソースコードとして含まれているライブラリのライセンス

### Geohash.java

#### Geohash.java License
<pre>
// Geohash.java
// Geohash library for Java
// ported from David Troy's Geohash library for Javascript
//  - http://github.com/davetroy/geohash-js/tree/master
// (c) 2008 David Troy
// (c) 2008 Tom Carden
// Distributed under the MIT License
Geohash Javascript Demonstration
(c) 2008 David Troy
Released under the MIT License

SUMMARY
This is a basic demonstration of how the GeoHash algorithm can be used to generate bounding box searches without the use of specialized spatial indexing approaches.

This can be especially helpful in cases where spatial indexes are either not supported or do not scale to high volumes.  Environments such as Google App Engine, EC2, and SQLite provide reasonable string indexing services but do not support spatial indexing.  This algorithm could be used to provide proximity searching in these environments.

BACKGROUND
The Geohash algorithm was first described by Gustavo Niemeyer in February 2008.  By interleaving latitude and longitude information in a bitwise fashion, a composite value is generated that provides a high resolution geographic point, and is well suited for storage or transmission as a character string.

Geohash also has the property that as the number of digits decreases (from the right), accuracy degrades.  This property can be used to do bounding box searches, as points near to one another will share similar Geohash prefixes.

However, because a given point may appear at the edge of a given Geohash bounding box, it is necessary to generate a list of Geohash values in order to perform a true proximity search around a point.  Because the Geohash algorithm uses a base-32 numbering system, it is possible to derive the Geohash values surrounding any other given Geohash value using a simple lookup table.

So, for example, 1600 Pennsylvania Avenue, Washington DC resolves to:
38.897, -77.036

Using the geohash algorithm, this latitude and longitude is converted to:
dqcjqcp84c6e

A simple bounding box around this point could be described by truncating this geohash to:
dqcjqc

However, 'dqcjqcp84c6e' is not centered inside 'dqcjqc', and searching within 'dqcjqc' may miss some desired targets.

So instead, we can use the mathematical properties of the Geohash to quickly calculate the neighbors of 'dqcjqc';  we find that they are:
'dqcjqf','dqcjqb','dqcjr1','dqcjq9','dqcjqd','dqcjr4','dqcjr0','dqcjq8'

This gives us a bounding box around 'dqcjqcp84c6e' roughly 2km x 1.5km and allows for a database search on just 9 keys:
SELECT * FROM table WHERE LEFT(geohash,6) IN ('dqcjqc', 'dqcjqf','dqcjqb','dqcjr1','dqcjq9','dqcjqd','dqcjr4','dqcjr0','dqcjq8');

MORE INFORMATION
GeoHash on Wikipedia (http://en.wikipedia.org/wiki/Geohash)
GeoHash gem on Rubyforge (http://geohash.rubyforge.org/)

THIS PROJECT
Demo Site (http://openlocation.org/geohash/geohash-js)
Source Code (http://github.com/davetroy/geohash-js/tree/master)

Please contact me at dave at roundhousetech.com with any questions you may have about this code;  right now this is experimental.  The bounding box code found here will be added to the Ruby gem soon.
</pre>