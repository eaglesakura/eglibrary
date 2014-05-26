/*
 * FileInputStream.cpp
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#include "eglibrary.h"
#include "FileInputStream.h"

JCFileInputStream::JCFileInputStream(const JCString fileName) {
	file = fopen((char*) fileName.toCharArray(), "rb");
	setAutoClose(true);
}

JCFileInputStream::JCFileInputStream(const charactor *fileName) {
	file = fopen((char*) fileName, "rb");
	setAutoClose(true);
}
JCFileInputStream::JCFileInputStream(FILE* fp) {
	file = fp;
	setAutoClose(false);
}

JCFileInputStream::~JCFileInputStream() {
	if (file != NULL && isAutoClose()) {
		fclose(file);
		file = NULL;
	}
}

void JCFileInputStream::init() {
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

s32 JCFileInputStream::read(u8 *result, s32 size) {
	return fread(result, 1, size, file);
}

s32 JCFileInputStream::skip(s32 bytes) {
	bytes = egl::min(size, bytes);
	return fseek(file, bytes, SEEK_CUR);
}
