/*
 * FileInputStream.cpp
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#include "eglibrary.h"
#include "FileInputStream.h"

namespace egl {

FileInputStream::FileInputStream(const egl::String fileName) {
	file = fopen((char*) fileName.toNative(), "rb");
}

FileInputStream::FileInputStream(const charactor *fileName) {
	file = fopen((char*) fileName, "rb");
}

FileInputStream::~FileInputStream() {
	if (file != NULL) {
		fclose(file);
		file = NULL;
	}
}

u8 FileInputStream::read() {
	u8 result = 0;
	if (read(&result, 1) == 1) {
		return result;
	}
	return -1;
}

s32 FileInputStream::read(u8 *result, s32 size) {
	return fread(result, 1, size, file);
}

s32 FileInputStream::skip(s32 bytes) {
	return fseek(file, bytes, SEEK_CUR);
}

}
