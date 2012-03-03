# eglibrary

## このライブラリについて

Android向けOpenGL ES 1.1ラッパーライブラリです。
2Dゲームの制作に必要な描画周りの処理、数学処理、スレッド操作系の補助クラスが大量にあります。
基本的に@eaglesakuraが個人的に使うor使いたい機能のみで構成されています。
ソースコードはライセンスに従って自由に使ってもらって構いませんが、どんな不具合があっても責任は持ちません。
OpenGL関連のサンプルソースとして見るくらいが丁度いいのではないかと思います。

## 使い方

必要なプロジェクトをAndroidのライブラリプロジェクトとして追加するか、ビルドパスにrelease/配下のjarを追加してください。
jarにはソースコードも含まれていますので、内部でエラーが発生してもトレースができるかと思います。

## 既知の問題）Mali系GPUでの不具合

次の条件下で、Mali系のGPU（Galaxy S2等）に限りEGLの復帰に失敗するようです。
今のところQualcomm系のGPU（Nexus One等）では発生しません。

1. OpenGLManagerクラスとOpenGLViewを使っているActivityから別なActivityを呼び出す
1. 画面遷移中から「戻る」キーを連打し、すぐにActivityへ戻る
1. OpenGLViewでEGL_BAD_ALLOCが発生し、OpenGL ESの描画が行えない（反映されない）
1. 上記の状態になった場合、現在のところ例外を投げるようにしているため、検出は可能
1. そのような状態になったら、再度ActivityのSurfaceの破棄・復旧（AcrtivityのonPause/onResume等）を行うことで復旧できる