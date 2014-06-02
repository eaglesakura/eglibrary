/*
 * eglmath.h
 *
 *  Created on: 2012/05/16
 *      Author: Takeshi
 */

#ifndef EGLMATH_H_
#define EGLMATH_H_

namespace egl {

/**
 * 最小値を返す
 */
template<class T>
inline const T min(const T a, const T b) {
	return a < b ? a : b;
}

/**
 * 最大値を返す
 */
template<class T>
inline const T max(const T a, const T b) {
	return a > b ? a : b;
}


/**
 * 値が_min <= value <= _maxになるように正規化してvalueを返す。
 */
template<class T>
inline const T minmax(const T _min, const T _max, const T value) {
	if (value < _min) {
		return _min;
	}

	if (value > _max) {
		return _max;
	}

	return value;
}

}

#endif /* EGLMATH_H_ */
