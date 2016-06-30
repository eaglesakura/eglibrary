# eglibrary-Framework

[@eaglesakura](https://twitter.com/eaglesakura)が関わった開発で利用することを前提としたライブラリです。作者個人が開発を効率化するために開発が行われているため、たまに動作が変わったりします。

分割はそれなりにされていますが、そこそこ適当です。あまりに規模が大きくなった場合は将来的に分割する可能性があります。また、依存ライブラリが結構多いです。

普通に使用する場合はForkしたり、dependenciesでバージョンを固定したり、参考にするに留めるほうが良いです。

## build.gradle

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
dependencies {
    // add library
    compile "com.eaglesakura:${library name}:0.2.+"
}
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

リポジトリの最新版(ver 0.2.+以降のバージョン)では、プロジェクトの都合に応じて、下記のどちらかを選択してください。ソースコードは自由に使ってもらって構いませんが、どんな不具合があっても責任は持ちません。

また、ライブラリ内で依存している別なライブラリについては、必ずそのライブラリのライセンスに従ってください。

* アプリ等の成果物で権利情報を表示可能な場合
	* 権利情報の表示を行う（行える）場合、MIT Licenseを使用してください。
	* [MIT License](LICENSE-MIT.txt)
* 何らかの理由で権利情報を表示不可能な場合
	* 何らかの事情によりライセンス表記を行えない場合、下記のライセンスで使用可能です。
	* ライブラリ内で依存している別なライブラリについては、必ずそのライブラリのライセンスに従ってください。
	* [NYSL(English)](LICENSE-NYSL-eng.txt)
	* [NYSL(日本語)](LICENSE-NYSL-jpn.txt)
* 0.1.+バージョンを使用する場合
	* dependenciesを利用して取得する場合、継続してNYSLとして使用できます。
	* [旧ライセンス(NYSL)](LICENSE-NYSL-eng.txt)

## ソースコードとして含まれているライブラリのライセンス

### com.eaglesakura.android.aquery 配下

 * Android Queryから必要最低限の機能を切り出したバージョンを組み込んでいます。
 * 名前衝突を避けるため、package名を変更しています。
 * ver 0.26.8

```
/*
 * Copyright 2011 - AndroidQuery.com (tinyeeliu@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

 ```

### Geohash.java License

```
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

```
