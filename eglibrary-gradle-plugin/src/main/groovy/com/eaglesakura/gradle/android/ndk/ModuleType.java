package com.eaglesakura.gradle.android.ndk;

/**
 * build module type
 */
public enum ModuleType {
    PrebuildStaticLibrary {
        @Override
        public String buildLine() {
            return "include $(PREBUILT_STATIC_LIBRARY"; // *.a
        }
    },

    SharedLibrary {
        @Override
        public String buildLine() {
            return "include $(BUILD_SHARED_LIBRARY)";   // *.so
        }
    };

    public abstract String buildLine();
}
