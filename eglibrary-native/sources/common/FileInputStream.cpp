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
	file = fopen((char*) fileName.toCharArray(), "rb");
	setAutoClose(true);
}

FileInputStream::FileInputStream(const charactor *fileName) {
	file = fopen((char*) fileName, "rb");
	setAutoClose(true);
}
FileInputStream::FileInputStream(FILE* fp) {
	file = fp;
	setAutoClose(false);
}

FileInputStream::~FileInputStream() {
	if (file != NULL && isAutoClose() ) {
		fclose(file);
		file = NULL;
	}
}

void FileInputStream::init() {
	fpos_t current = 0;
	// 現在位置を保存
	fgetpos(file, &current);

	// ファイルの最後まで移動して位置を保存
	fseek(file, 0, SEEK_END);
	fpos_t size = 0;
	fgetpos(file, &size);

	// 現在位置から全体サイズを引いたら残量
	this->size = (size - current);
}

s32 FileInputStream::read(u8 *result, s32 size) {
	return fread(result, 1, size, file);
}

s32 FileInputStream::skip(s32 bytes) {
	bytes = egl::min(size, bytes);
	return fseek(file, bytes, SEEK_CUR);
}

}
