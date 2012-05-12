/*
 * new.cpp
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */
#include "eglibrary.h"
#include "string.h"

namespace egl {

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

void* Memory::alloc(const size_t size) {
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
		ret = (sMemory*) Memory::offsetPtr(ptr, -(s32) sizeof(s32));
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
		ret = (sMemory*) Memory::offsetPtr(ptr, -(s32) sizeof(s32));
		--ret;
	}

	return ret;
}

void Memory::free(void* ptr) {
	if (!ptr) {
		return;
	}
	sMemory* mem = getHeader(ptr);
	sumFreeSize += mem->user;
	::free(mem);
}

size_t Memory::getSize(void* ptr) {
	bool isArray = false;
	sMemory* mem = getHeader(ptr, &isArray);

	if (isArray) {
		return mem->user - sizeof(s32);
	} else {
		return mem->user;
	}
}

void Memory::addRef(void* ptr) {
	sMemory* mem = getHeader(ptr);
	++mem->ref;
}

s32 Memory::release(void* ptr) {
	sMemory* mem = getHeader(ptr);
	--mem->ref;
	s32 ret = mem->ref;
	return ret;
}

size_t Memory::sumAllocSize = 0;
size_t Memory::sumFreeSize = 0;

size_t Memory::getSumFreeSize() {
	return sumFreeSize;
}

size_t Memory::getSumAllocSize() {
	return sumAllocSize;
}

size_t Memory::getExistsSize() {
	return sumAllocSize - sumFreeSize;
}

// end egl
}

void* operator new(const size_t size) {
	return egl::Memory::alloc(size);
}

void* operator new[](const size_t size) {
	void* ptr = egl::Memory::alloc(size);
	return ptr;
}

void operator delete(void* ptr) {
	egl::Memory::free(ptr);
}

void operator delete[](void* ptr) {
	egl::Memory::free(ptr);
}
