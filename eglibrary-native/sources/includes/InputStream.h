/*
 * InputStream.h
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#ifndef INPUTSTREAM_H_
#define INPUTSTREAM_H_

namespace egl {

class InputStream {
public:
	virtual ~InputStream() {

	}

	/**
	 * 1byte読み取る
	 * @return 読み取ったデータ
	 */
	virtual u8 read() = 0;

	/**
	 * 指定バイト数読み取る
	 * @return 読み込んだバイト数
	 */
	virtual s32 read(u8* result, s32 size) = 0;

	/**
	 * 指定バイト数読み取りヘッダを飛ばす
	 */
	virtual s32 skip(s32 bytes) = 0;
};

}

#endif /* INPUTSTREAM_H_ */
