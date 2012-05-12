/*
 * JpegStreamSource.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef JPEGSTREAMSOURCE_H_
#define JPEGSTREAMSOURCE_H_

#include "jpeglib.h"
#include "jerror.h"

struct JpegStream {
	jpeg_source_mgr pub; /* public fields */

	/**
	 * 読み込みストリーム
	 */
	InputStream *stream;

	/**
	 * 保持するバッファの長さ
	 */
	s32 bufferLength;

	/**
	 * 一時的に保持しておくバッファ
	 */
	egl::SmartArray<u8> buffer;

	static void init_source(jpeg_decompress_struct *cInfo) {

	}

	static boolean fill_input_buffer(j_decompress_ptr cinfo) {
		JpegStream *jStream = (JpegStream*) cinfo->src;
		size_t nbytes;

		nbytes = jStream->stream->read((u8*) jStream->buffer,
				jStream->bufferLength);
		// 読み込みに失敗したら
		if (nbytes <= 0) {
			WARNMS(cinfo, JWRN_JPEG_EOF);
			/* Insert a fake EOI marker */
			jStream->buffer[0] = (JOCTET) 0xFF;
			jStream->buffer[1] = (JOCTET) JPEG_EOI;
			nbytes = 2;
		}

		// 読み込んだ量を設定する
		jStream->pub.next_input_byte = jStream->buffer;
		jStream->pub.bytes_in_buffer = nbytes;

		return TRUE;
	}

	static void skip_input_data(j_decompress_ptr cinfo, long num_bytes) {
		struct jpeg_source_mgr * src = cinfo->src;

		/* Just a dumb implementation for now.  Could use fseek() except
		 * it doesn't work on pipes.  Not clear that being smart is worth
		 * any trouble anyway --- large skips are infrequent.
		 */
		if (num_bytes > 0) {
			while (num_bytes > (long) src->bytes_in_buffer) {
				num_bytes -= (long) src->bytes_in_buffer;
				(void) (*src->fill_input_buffer)(cinfo);
				/* note we assume that fill_input_buffer will never return FALSE,
				 * so suspension need not be handled.
				 */
			}
			src->next_input_byte += (size_t) num_bytes;
			src->bytes_in_buffer -= (size_t) num_bytes;
		}
	}

	static void term_source(j_decompress_ptr cinfo) {
		JpegStream *jStream = (JpegStream*) cinfo->src;

		SAFE_DELETE(jStream->stream);
		SAFE_DELETE(jStream);

		log("delete source!!");
	}

	static void init(jpeg_decompress_struct *cInfo) {
		JpegStream *jStream = NULL;

		log("init by stream!!");
		jStream = new JpegStream();
		jStream->bufferLength = 1024 * 32;
		jStream->buffer = new u8[jStream->bufferLength];
		cInfo->src = (jpeg_source_mgr*) (jStream);

		jStream->stream = new FileInputStream("/sdcard/texture.jpg");
		jStream->pub.bytes_in_buffer = 0;
		jStream->pub.next_input_byte = NULL;

		// functions
		jStream->pub.init_source = JpegStream::init_source;
		jStream->pub.fill_input_buffer = JpegStream::fill_input_buffer;
		jStream->pub.skip_input_data = JpegStream::skip_input_data;
		jStream->pub.resync_to_restart = jpeg_resync_to_restart;
		jStream->pub.term_source = JpegStream::term_source;
	}
};

#endif /* JPEGSTREAMSOURCE_H_ */
