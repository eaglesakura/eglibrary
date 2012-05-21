/*
 * InputStream.h
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#ifndef INPUTSTREAM_H_
#define INPUTSTREAM_H_

class JCInputStream {
public:
	virtual ~JCInputStream() {

	}

	/**
	 * 1byte読み取る
	 * @return 読み取ったデータ
	 */
	virtual u8 read() {
		u8 result = 0;
		if (read(&result, 1) == 1) {
			return result;
		}
		return -1;
	}

	/**
	 * 指定バイト数読み取る
	 * @return 読み込んだバイト数
	 */
	virtual s32 read(u8* result, s32 size) = 0;

	/**
	 * 指定バイト数読み取りヘッダを飛ばす
	 */
	virtual s32 skip(s32 bytes) = 0;

	/**
	 * 読み取り可能な残容量を取得する。
	 */
	virtual s32 available() const = 0;
};

/**
 * ManagedPointer
 */
typedef JCSmartPtr<JCInputStream> MInputStream;

#endif /* INPUTSTREAM_H_ */
