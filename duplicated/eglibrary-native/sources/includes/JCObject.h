/*
 * JCObject.h
 *
 *  Created on: 2012/05/16
 *      Author: Takeshi
 */

#ifndef JCOBJECT_H_
#define JCOBJECT_H_

#define	JCOBJECT_GETFILENAME(...)	virtual	JCString	getClassName(){ toFileName(__FILE__); }
class JCObject {
protected:
	static JCString toFileName(const charactor* __file__);

public:
	JCObject() {
	}

	virtual ~JCObject() {
	}

	JCOBJECT_GETFILENAME()
};

#endif /* JCOBJECT_H_ */
