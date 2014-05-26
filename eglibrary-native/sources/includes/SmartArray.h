/*
 * SmartArray.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef SMARTARRAY_H_
#define SMARTARRAY_H_

template<typename T>
class JCSmartArray {
	T* ptr;
	//!	ポインタを入れ替える
	void setPtr(T* p) {
		//!	参照を追加する
		if (p) {
			JCMemory::addRef(p);
		}

		//!	参照カウントを減らす
		releasePtr();

		//!	代入する
		ptr = p;
	}
	//!	ポインタの参照を解除する
	void releasePtr() {
		//!	ポインタが設定済み
		if (ptr) {
			//!	リリースし、参照が下回った時点で消去
			if (JCMemory::release(ptr) <= 0) {
				SAFE_DELETE_ARRAY(ptr);
			}
		}
	}
public:
	//!
	JCSmartArray(T *p = NULL) {
		ptr = NULL;
		setPtr(p);
	}
	JCSmartArray(const JCSmartArray<T> &cpy) {
		ptr = NULL;
		setPtr((T*) cpy.ptr);
	}
	//!	デストラクタ
	~JCSmartArray() {
		//!	参照カウントを減らす
		releasePtr();
	}
	T& operator[](s32 i) {
		return ptr[i];
	}
	const T& operator[](s32 i) const {
		return ptr[i];
	}
	operator T*(void) {
		return ptr;
	}
	operator const T*(void) const {
		return ptr;
	}

	void set(s32 i, const T& param) {
		ptr[i] = param;
	}
	T* getPtr() {
		return ptr;
	}
	const T* getPtr() const {
		return ptr;
	}
	JCSmartArray<T>& operator=(const T* p) {
		setPtr((T*) p);
		return (*this);
	}
	JCSmartArray<T>& operator=(const JCSmartArray<T>& p) {
		setPtr((T*) p.ptr);
		return (*this);
	}

	size_t length() const {
		return ptr ? (JCMemory::getSize(ptr) / sizeof(T)) : 0;
	}
	bool operator==(const T* p) const {
		return ptr == p;
	}
	bool operator!=(const T* p) const {
		return ptr != p;
	}
};

#endif /* SMARTARRAY_H_ */
