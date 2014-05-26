/*
 * JpegStreamSource.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef JPEGSTREAMSOURCE_H_
#define JPEGSTREAMSOURCE_H_

#include "eglibrary.h"
#include "InputStream.h"
#include "jpeglib.h"
#include "jerror.h"

struct JCJpegStreamSource {
	jpeg_source_mgr pub; /* public fields */

	/**
	 * 読み込みストリーム
	 */
	JCInputStream *stream;

	/**
	 * 保持するバッファの長さ
	 */
	s32 bufferLength;

	/**
	 * 一時的に保持しておくバッファ
	 */
	JCSmartArray<u8> buffer;

private:
	/**
	 * 通常のコンストラクタ
	 */
	JCJpegStreamSource(JCInputStream *stream, s32 bufferlength);
public:
	~JCJpegStreamSource();

	static JCJpegStreamSource* initialize(jpeg_decompress_struct *cInfo,
			JCInputStream *stream, s32 bufferLength);
};
#endif /* JPEGSTREAMSOURCE_H_ */