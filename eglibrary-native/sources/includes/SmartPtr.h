/*
 * SmartPtr.h
 *
 *  Created on: 2012/05/13
 *      Author: Takeshi
 */

#ifndef SMARTPTR_H_
#define SMARTPTR_H_

//!	参照カウントの自動制御。
template<typename T>
class JCSmartPtr {
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
				SAFE_DELETE(ptr);
			}
		}
	}
public:
	JCSmartPtr(T *p = NULL) {
		ptr = NULL;
		setPtr(p);
	}
	JCSmartPtr(const JCSmartPtr<T> &cpy) {
		ptr = NULL;
		setPtr((T*) cpy.ptr);
	}
	//!	デストラクタ
	~JCSmartPtr() {
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

	JCSmartPtr<T>& operator=(const T* p) {
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
#endif /* SMARTPTR_H_ */
