package org.example;

import java.util.StringTokenizer;

public class Config {
    private final static byte[] tokenKey = "ptDlUglHBnAONu3ytIip".getBytes();
    private final static byte[] hashKey= "1vVjzwpW98uRRB7UUu9n".getBytes();

    private Config(){}

    public static byte[] getTokenKey() {
        return tokenKey;
    }
    public static byte[] getHashKey() {
        return hashKey;
    }
}
