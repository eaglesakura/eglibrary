# eglibrary

## ライセンスについて
./LICENSEの内容に従ってください。
ソースコードは自由に使ってもらって構いませんが、どんな不具合があっても責任は持ちません。
基本的には @eaglesakura 自身が使用するためのUtil系クラス群です。

基本的に@eaglesakuraが個人的に使うor使いたい機能のみで構成されています。

## eglibrary-java-core

Pure-Javaで記述されたライブラリです。
Androidで実行可能、もしくはJava 1.6相当の機能で記述されています。

## eglibrary-java-wrapper

Pure-Java+各種外部ライブラリのラッパーライブラリです。Androidで実行可能、もしくはJava 1.6相当の機能で記述されています。

## eglibrary-android-java-core

eglibrary-android-api8をAPI15以降向けに整理・書きなおしたライブラリです。Android SDKのみで動作し、他のライブラリへの依存がありません。

## eglibrary-android-java-wrapper

各種ライブラリのラッパーライブラリです。特性上、apkサイズが肥大化しやすいため、proguardで適当に軽量化することを推奨します。
