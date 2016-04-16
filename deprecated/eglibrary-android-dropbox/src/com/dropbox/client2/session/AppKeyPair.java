package com.dropbox.client2.session;

/**
 * <p>
 * Holds your app's key and secret.
 * </p>
 */
public final class AppKeyPair extends TokenPair {
    static final long serialVersionUID = 1;

    public AppKeyPair(String key, String secret) {
        super(key, secret);
    }
}
