/*
 * JointConnector.hpp
 *
 */

#ifndef JOINTCONNECTOR_HPP_
#define JOINTCONNECTOR_HPP_

#if defined(__ANDROID__)
#define __JOINTCONNECTOR_USE_JNI__ 1
#endif

#include    <stdlib.h>
#include    <assert.h>
#include    <string>
#include    <vector>
#include    <cstdint>
#include    <memory> /* shared_ptr */

/**
 * JNIを使用可能なプラットフォーム
 */
#ifdef  __JOINTCONNECTOR_USE_JNI__
#include    <jni.h>
namespace jc {

/**
 * private namespace
 */
namespace __private_jni {
/**
 * 1headerでstatic変数を使用するため、templateによる実装を行う
 */
template<typename AlwaysVoid>
struct JniSupportArgs {
    static JavaVM *sJavaVM;
};

template<typename AlwaysVoid> JavaVM* JniSupportArgs<AlwaysVoid>::sJavaVM;
}

namespace jni {

/**
 * Threadに関連付けられたデフォルトのJNIEnvを取得する。
 * 関数呼び出しを行う分、オーバーヘッドがかかる。
 */
inline JNIEnv* getThreadJniEnv() {
    JNIEnv *env = NULL;
    ::jc::__private_jni::JniSupportArgs<void>::sJavaVM->GetEnv((void**) &env, JNI_VERSION_1_6);
    return env;
}

/**
 * JNIサポートクラスの初期化を行う。
 *
 * Applicationクラス等から呼び出すと良い。
 */
inline void initJniSupport(JNIEnv *env) {
    // 既にセットアップ済みであれば何もしない
    if (::jc::__private_jni::JniSupportArgs<void>::sJavaVM) {
        return;
    }

    // envが有効であり、取得後はjavaVmが有効でなければならない
    assert(env);
    {
        env->GetJavaVM(&::jc::__private_jni::JniSupportArgs<void>::sJavaVM);
    }
    assert(::jc::jni::prv::JniSupportArgs<void>::sJavaVM);
}
}

/**
 * wrapperを直接公開はしない
 */
namespace __private_jni {

/**
 * jobjectに対する参照を保存する
 * この処理は実装時点でスレッドセーフに作られていないため、注意する
 */
struct JobjectRef {
    /**
     * 管理対象のオブジェクト
     */
    jobject javaobject;

    /**
     * マルチスレッドから扱われる場合にtrueを指定する
     * デフォルトはfalse
     */
    bool multiThread;

    /**
     * JNIEnvキャッシュ
     *
     * マルチスレッドアクセスの場合は常に最新のenvを取得するため、使用しない。
     */
    JNIEnv *env;

    JobjectRef(jobject origin, JNIEnv *env, bool newLocalRef) {
        assert(origin);

        multiThread = false;

        // envはNULLに備える
        if (!env) {
            env = ::jc::jni::getThreadJniEnv();
        }
        this->env = env;
        assert(this->env);

        // 必要であれば参照を新たにする。
        // Methodの引数として渡されたLocal参照は全て無視するため。
        if (newLocalRef) {
            javaobject = env->NewLocalRef(origin);
        } else {
            javaobject = origin;
        }

        // 最終的に参照が取得できなければならない
        assert(javaobject);
    }

    ~JobjectRef() {
        // 最終的な参照を削除する
        deleteRef(env, javaobject);
    }

    /**
     * 参照削除を行う
     */
    static void deleteRef(JNIEnv *env, jobject obj) {
        switch (env->GetObjectRefType(obj)) {
        case JNIGlobalRefType:
            env->DeleteGlobalRef(obj);
            break;
        case JNILocalRefType:
            env->DeleteLocalRef(obj);
            break;
        case JNIWeakGlobalRefType:
            env->DeleteWeakGlobalRef(obj);
            break;
        }
        return;
    }

    /**
     * グローバル参照へ切り替える。
     */
    static void global(JobjectRef *origin) {
        if (!origin || origin->env->GetObjectRefType(origin->javaobject) == JNIGlobalRefType) {
            return;
        }

        // 新しい参照オブジェクトを作成
        const jobject newRef = origin->env->NewGlobalRef(origin->javaobject);

        // 古い参照を削除
        deleteRef(origin->env, origin->javaobject);

        // 新しい参照に上書き
        origin->javaobject = newRef;
    }
};

/**
 * Java側のオブジェクトへの参照を管理する
 * デストラクタが呼ばれた時点でjobjectの所有者が無いと想定し、参照を削除する
 *
 * 複数スレッドからの同時アクセスに対応する場合はmultiThreadAccessを呼び出すこと。
 */
class JavaObjectWrapper {
protected:
    /**
     * 参照
     */
    ::std::shared_ptr<JobjectRef> mRef;

public:
    /**
     * デフォルトコンストラクタ
     */
    JavaObjectWrapper() {
    }

    JavaObjectWrapper(jobject obj, JNIEnv *env = nullptr, bool newLocalRefFlag = true) {
        assert(obj);

        // リファレンスを取得する
        this->mRef = ::std::shared_ptr<JobjectRef>(new JobjectRef(obj, env, newLocalRefFlag));
    }

    /**
     * コピーコンストラクタ
     */
    JavaObjectWrapper(const JavaObjectWrapper& origin) {
        this->mRef = origin.mRef;
    }

    /**
     * デストラクタが呼ばれた時点でjobjectの所有者が無いと想定し、参照を削除する
     */
    virtual ~JavaObjectWrapper() {

    }

    /**
     * JNIEnvを取得する
     */
    JNIEnv* getEnv() const {
        if (!mRef) {
            return nullptr;
        }

        if (mRef->multiThread) {
            // 常に最新のJNIEnvが必要な場合はそちらを使用する
            return ::jc::jni::getThreadJniEnv();
        } else {
            // キャッシュで問題ない場合はそちらを使用する
            return mRef->env;
        }
    }

    /**
     * copy
     */
    JavaObjectWrapper& operator=(const JavaObjectWrapper& origin) {
        this->mRef = origin.mRef;
        return *this;
    }

    /**
     * マルチスレッドからアクセスされる可能性がある場合はtrueを指定する
     */
    void setMultiThreadAccess(const bool set) {
        if (mRef) {
            mRef->multiThread = set;
        }
    }

    /**
     * グローバル参照へ移行する
     */
    void globalRef() {
        JobjectRef::global(mRef.get());
    }

    /**
     * 使用されるスレッドが移動した場合に呼び出す。
     * 別スレッドに所有権が移動した場合、使用するスレッドから呼び出しを行う。
     *
     * 複数スレッドから呼び出される可能性があるならば、setMultiThreadAccess(true)を指定すべき。
     */
    void onMovedThread() {
        if (mRef) {
            mRef->env = ::jc::jni::getThreadJniEnv();
        }
    }

    /**
     * Objectを返す
     */
    jobject getJobject() const {
        if (mRef) {
            return mRef->javaobject;
        } else {
            return nullptr;
        }
    }

    /**
     * 新規にローカル参照を生成する
     */
    jobject newLocalRef() {
        if (mRef) {
            return getEnv()->NewLocalRef(mRef->javaobject);
        } else {
            return nullptr;
        }
    }

    /**
     * 新規にグローバル参照を生成する
     */
    jobject newGlobalRef() {
        if (mRef) {
            return getEnv()->NewGlobalRef(mRef->javaobject);
        } else {
            return nullptr;
        }
    }

    /**
     * キャストサポート
     *
     * JNI->Java返却サポートのため、新たな参照を生成することに注意すること
     */
    operator jobject() {
        return newLocalRef();
    }

    /**
     * Object#getClass()にてオブジェクトを取得する。
     * 参照の変更は行わないため、呼び出し側で適宜調整すること。
     */
    jclass getClass() const {
        if (mRef) {
            return getEnv()->GetObjectClass(mRef->javaobject);
        } else {
            return nullptr;
        }
    }

    /**
     * 参照を保持していればtrue
     */
    bool hasObject() const {
        return mRef.get() != nullptr;
    }
};

/**
 * クラスオブジェクトを管理する
 */
class JavaClassWrapper: public JavaObjectWrapper {
public:
    /**
     * 標準生成
     */
    JavaClassWrapper(jclass obj, JNIEnv *env = nullptr, bool newLocalRef = true) :
            JavaObjectWrapper(obj, env, newLocalRef) {
        assert(obj);
    }

    /**
     * デフォルトコンストラクタ
     */
    JavaClassWrapper() :
            JavaObjectWrapper() {
    }

    /**
     * コピーコンストラクタ
     */
    JavaClassWrapper(const JavaClassWrapper& origin) :
            JavaObjectWrapper() {
        this->mRef = origin.mRef;
    }

    virtual ~JavaClassWrapper() {

    }

    /**
     * フィールドIDを取得する
     *
     * 読み込み検証は行わないため、失敗時はNULLを返して終了する。
     */
    jfieldID getField(const char* name, const char* signeture, bool isStatic) {
        if (!hasObject()) {
            return nullptr;
        }

        JNIEnv *env = getEnv();
        assert(env);

        if (isStatic) {
            return env->GetStaticFieldID((jclass) mRef->javaobject, name, signeture);
        } else {
            return env->GetFieldID((jclass) mRef->javaobject, name, signeture);
        }
    }

    /**
     * メソッドを取得する
     *
     * 読み込み検証は行わないため、失敗時はNULLを返して終了する。
     */
    jmethodID getMethod(const char* name, const char* signeture, bool isStatic) {
        if (!hasObject()) {
            return nullptr;
        }

        JNIEnv *env = getEnv();
        assert(env);

        if (isStatic) {
            return env->GetStaticMethodID((jclass) mRef->javaobject, name, signeture);
        } else {
            return env->GetMethodID((jclass) mRef->javaobject, name, signeture);
        }
    }

    /**
     * jclass型へ変換する。
     *
     * 管理しているjobjectをそのまま帰すため、外部で解放しないこと。
     */
    jclass getJclass() const {
        return (jclass) getJobject();
    }

    JavaClassWrapper& operator=(const JavaClassWrapper& origin) {
        this->mRef = origin.mRef;
        return *this;
    }

    /**
     * jclass型へ変換する。
     *
     * JNI->Java返却サポートのため、新たな参照を生成することに注意すること
     */
    operator jclass() {
        return (jclass) newLocalRef();
    }

    /**
     * Object.getClass()を使用して取得する
     */
    static JavaClassWrapper from(const JavaObjectWrapper &obj) {
        return JavaClassWrapper(obj.getClass(), obj.getEnv(), false);
    }

    /**
     * クラス名を指定してClassオブジェクトを読み込む
     */
    static JavaClassWrapper find(JNIEnv *env, const char* name) {
        if (!env) {
            env = ::jc::jni::getThreadJniEnv();
        }
        assert(env);
        jclass clz = env->FindClass(name);
        if (!clz) {
            return JavaClassWrapper();
        } else {
            return JavaClassWrapper(clz, env, false);
        }
    }
};

/**
 * jstring型オブジェクトへの参照を管理する
 */
class JavaStringWrapper: public JavaObjectWrapper {
public:
    JavaStringWrapper(jstring obj, JNIEnv *env = nullptr, bool newLocalRef = true) :
            JavaObjectWrapper(obj, env, newLocalRef) {
    }

    JavaStringWrapper(jobject obj, JNIEnv *env = nullptr, bool newLocalRef = true) :
            JavaObjectWrapper(obj, env, newLocalRef) {
    }

    JavaStringWrapper(const JavaStringWrapper& origin) :
            JavaObjectWrapper() {
        this->mRef = origin.mRef;
    }

    /**
     * 生文字列から生成する
     */
    JavaStringWrapper(const char* str, JNIEnv *env = nullptr) :
            JavaObjectWrapper() {
        if (!env) {
            env = ::jc::jni::getThreadJniEnv();
        }

        // jstring生成
        jstring jstr = env->NewStringUTF(str);
        mRef = ::std::shared_ptr<JobjectRef>(new JobjectRef(jstr, env, false));
    }

    /**
     * C++文字列から生成する
     */
    JavaStringWrapper(const ::std::string &str, JNIEnv *env = nullptr) {
        JavaStringWrapper((const char*) str.c_str(), env);
    }

    virtual ~JavaStringWrapper() {
    }

    JavaStringWrapper& operator=(const JavaStringWrapper &origin) {
        this->mRef = origin.mRef;
        return *this;
    }

    /**
     * String型として取得する
     */
    ::std::string asString() const {
        JNIEnv *env = getEnv();
        // JNIからcharを取得する
        const char *pResult = env->GetStringUTFChars((jstring) mRef->javaobject, NULL);
        ::std::string str(pResult);
        // charを解放する
        env->ReleaseStringUTFChars((jstring) mRef->javaobject, pResult);

        return str;
    }

    jstring getJstring() const {
        return (jstring) getJobject();
    }

    /**
     * C++文字列にキャストする
     */
    operator ::std::string() const {
        return asString();
    }

    /**
     * 暗黙的な型変換に対応する
     *
     * JNI->Java返却サポートのため、新たな参照を生成することに注意すること
     */
    operator jstring() {
        return (jstring) newLocalRef();
    }
};

}

/**
 * Javaのラッパー型はJavaに対応した環境でしか動作しない
 */
namespace lang {

/**
 * Java string型
 */
typedef ::jc::__private_jni::JavaStringWrapper string_wrapper;

/**
 * Java object型
 */
typedef ::jc::__private_jni::JavaObjectWrapper object_wrapper;

/**
 * Java object型
 */
typedef ::jc::__private_jni::JavaClassWrapper class_wrapper;

/**
 * VMから渡された値をラップする
 */
template<typename OutType, typename inType>
inline OutType wrapFromVM(JNIEnv *env, const jobject in) {
    return OutType((inType) in, env, false);
}

}

}

#endif /* __JOINTCONNECTOR_USE_JNI__ */

/**
 * プリミティブ型は多言語使用できるようにする
 */
namespace jc {

namespace lang {

/**
 * Java byte型
 */
typedef int8_t s8_wrapper;

/**
 * Java short型
 */
typedef int16_t s16_wrapper;

/**
 * Java int型
 */
typedef int32_t s32_wrapper;

/**
 * Java long型
 */
typedef int64_t s64_wrapper;

/**
 * Java float型
 */
typedef float float_wrapper;

/**
 * Java double型
 */
typedef double double_wrapper;

/**
 * Java boolean型
 */
typedef bool boolean_wrapper;

/**
 * Java char型
 */
typedef u_int16_t char_wrapper;

}

}

#endif /* JOINTCONNECTOR_HPP_ */
