/*
 * new.cpp
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */
#include "eglibrary.h"
#include "string.h"

/**
 * メモリを実際に保存する構造体
 */
struct sMemory {
	size_t user; //!<	ユーザが要求した長さ。
	s32 ref; //!<	参照カウント。
	void* reserve; //!<	チェック領域。基本的には先頭アドレスと同じになる。ならない場合は、配列でポインタがズレている
};

sMemory* getHeader(void* ptr, bool *isArray);
sMemory* getHeader(void* ptr);

void* JCMemory::alloc(const size_t size) {
	//! 合計容量に追加
	sumAllocSize += size;
	sMemory* mem = (sMemory*) malloc(size + sizeof(sMemory));

	//!	ゼロクリア
	memset(mem, 0x00, size + sizeof(sMemory));
	mem->user = size;
	mem->ref = 0;
	mem->reserve = (void*) (mem + 1); //!<	アドレスを保存しておく
	return (void*) (mem + 1);
}

 sMemory* getHeader(void* ptr, bool *isArray) {
	sMemory *ret = (sMemory*) ptr;
	--ret;

	//!	配列の可能性
	if (ret->reserve != ptr) {
		ret = (sMemory*) JCMemory::offsetPtr(ptr, -(s32) sizeof(s32));
		--ret;

		(*isArray) = true;
	} else {
		(*isArray) = false;
	}

	return ret;
}

 sMemory* getHeader(void* ptr) {
	sMemory *ret = (sMemory*) ptr;
	--ret;

	//!	配列の可能性
	if (ret->reserve != ptr) {
		ret = (sMemory*) JCMemory::offsetPtr(ptr, -(s32) sizeof(s32));
		--ret;
	}

	return ret;
}

void JCMemory::free(void* ptr) {
	if (!ptr) {
		return;
	}
	sMemory* mem = getHeader(ptr);
	sumFreeSize += mem->user;
	::free(mem);
}

size_t JCMemory::getSize(void* ptr) {
	bool isArray = false;
	sMemory* mem = getHeader(ptr, &isArray);

	if (isArray) {
		return mem->user - sizeof(s32);
	} else {
		return mem->user;
	}
}

void JCMemory::addRef(void* ptr) {
	sMemory* mem = getHeader(ptr);
	++mem->ref;
}

s32 JCMemory::release(void* ptr) {
	sMemory* mem = getHeader(ptr);
	--mem->ref;
	s32 ret = mem->ref;
	return ret;
}

size_t JCMemory::sumAllocSize = 0;
size_t JCMemory::sumFreeSize = 0;

size_t JCMemory::getSumFreeSize() {
	return sumFreeSize;
}

size_t JCMemory::getSumAllocSize() {
	return sumAllocSize;
}

size_t JCMemory::getExistsSize() {
	return sumAllocSize - sumFreeSize;
}

void* operator new(const size_t size) {
	return JCMemory::alloc(size);
}

void* operator new[](const size_t size) {
	void* ptr = JCMemory::alloc(size);
	return ptr;
}

void operator delete(void* ptr) {
	JCMemory::free(ptr);
}

void operator delete[](void* ptr) {
	JCMemory::free(ptr);
}
