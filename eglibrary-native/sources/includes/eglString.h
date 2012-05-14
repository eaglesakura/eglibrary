/*
 * String.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef STRING_H_
#define STRING_H_

#include "string.h"

namespace egl {

class String {
	SmartArray<charactor> str; //!<	文字配列
public:
	String(const charactor* p = NULL) {
		str = copy(p);
	}

	//!	コピーコンストラクタ。
	String(const String &cpy) {
		str = cpy.str;
	}
	//!	文字結合。
	String(const charactor *a, const charactor *b) {
		str = new charactor[1 + strlen((char*) a) + strlen((char*) b)];

		strcat(str, (char*) a);
		strcat(str, (char*) b);
	}

	~String() {
	}

	//!	文字の長さを取得する。
	s32 length() const {
		return (s32) strlen(str);
	}

	charactor* toCharArray() {
		return (charactor*) str;
	}
	const charactor* toCharArray() const {
		return (const charactor*) str;
	}

#ifdef	ANDROID
#endif

	String operator+(const charactor *p) const {
		return String(str.getPtr(), p);
	}

	bool operator==(const charactor* p) const {
		return strcmp(str, p) == 0;
	}

	bool operator!=(const charactor* p) const {
		return strcmp(str, p) != 0;
	}

	//!	pの文字列をコピーした文字列を作成する。
	static charactor* copy(const charactor* p) {
		charactor* temp = (charactor*) "";
		if (!p) {
			p = temp;
		}

		charactor* ret = new charactor[strlen(p) + 1];
		strcpy(ret, p);
		return ret;
	}
};

}
#endif /* STRING_H_ */
