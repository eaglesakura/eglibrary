/*
 * Uri.h
 *
 *  Created on: 2012/05/20
 *      Author: Takeshi
 */

#ifndef URI_H_
#define URI_H_

#include	"eglibrary.h"

class JCUri {
	/**
	 * resource/rawからファイルを読み込む
	 * android only
	 */
	static const charactor* SCHEME_APK_RAW = "apkraw://";

	/**
	 * アプリ内のアセットからファイルを読み込む
	 * Android / iOS
	 */
	static const charactor* SCHEME_APPLI_ASSETS = "assets://";

	/**
	 * HTTP通信を行う
	 */
	static const charactor* SCHEME_HTTP = "http://";

	/**
	 * HTTPS通信を行う
	 */
	static const charactor* SCHEME_HTTPS = "https://";

	JCString uri;
public:
	JCUri(JCString uriString) {
		this->uri = uriString;
	}
	virtual ~JCUri() {

	}
};

#endif /* URI_H_ */
