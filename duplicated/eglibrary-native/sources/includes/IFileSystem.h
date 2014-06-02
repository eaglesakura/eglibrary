/*
 * IFileSystem.h
 *
 *  Created on: 2012/05/20
 *      Author: Takeshi
 */

#ifndef IFILESYSTEM_H_
#define IFILESYSTEM_H_

#include	"eglibrary.h"
#include	"Uri.h"

class IFileSystem {
protected:
	IFileSystem() {
	}

public:
	virtual ~IFileSystem() {
	}

	/**
	 * 読み込み用のストリームを開く。
	 */
	virtual JCInputStream* openInputStream(JCUri *uri);
};

#endif /* IFILESYSTEM_H_ */
