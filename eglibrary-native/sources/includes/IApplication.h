/*
 * IApplication.h
 *
 *  Created on: 2012/05/20
 *      Author: Takeshi
 */

#ifndef IAPPLICATION_H_
#define IAPPLICATION_H_

class IApplication {
public:
	enum Platform {
		/**
		 * Androidで実行されている
		 */
		Android,
		/**
		 * iPhone / iPod Touchで実行されている
		 */
		iPhone,

		/**
		 * iPadで実行されている
		 */
		iPad,
	};

	IApplication() {
	}
	virtual ~IApplication() {
	}

	/**
	 * 実行環境のプラットフォームタイプを取得する。
	 */
	virtual IApplication::Platform getPlatfor() = 0;
};

#endif /* IAPPLICATION_H_ */
