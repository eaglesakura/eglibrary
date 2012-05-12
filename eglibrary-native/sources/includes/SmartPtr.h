/*
 * SmartPtr.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef SMARTPTR_H_
#define SMARTPTR_H_

namespace egl {

//!	参照カウントの自動制御。
template<typename T>
class SmartPtr {
	T* ptr;
	//!	ポインタを入れ替える
	void setPtr(T* p) {
		//!	参照を追加する
		if (p) {
			Memory::addRef(p);
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
			if (Memory::release(ptr) <= 0) {
				SAFE_DELETE(ptr);
			}
		}
	}
public:
	SmartPtr(T *p = NULL) {
		ptr = NULL;
		setPtr(p);
	}
	SmartPtr(const SmartPtr<T> &cpy) {
		ptr = NULL;
		setPtr((T*) cpy.ptr);
	}
	//!	デストラクタ
	~SmartPtr() {
		//!	参照カウントを減らす
		releasePtr();
	}

	T* operator->(void) {
		return ptr;
	}
	const T* operator->(void) const {
		return ptr;
	}

	operator T*(void) {
		return ptr;
	}
	operator const T*(void) const {
		return ptr;
	}

	SmartPtr<T>& operator=(const T* p) {
		setPtr((T*) p);
		return (*this);
	}

	bool operator==(const T* p) const {
		return ptr == p;
	}
	bool operator!=(const T* p) const {
		return ptr != p;
	}
};

}

#endif /* SMARTPTR_H_ */
