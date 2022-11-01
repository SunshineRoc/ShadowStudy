package com.shadow.study.utils;

import android.text.TextUtils;

public class StringUtils {

    private static final String DEFAULT_SUFFIX = "-host";

    public static String addSuffix(String content) {
        return addSuffix(content, DEFAULT_SUFFIX);
    }

    /**
     * 给 content 添加后缀 suffix
     */
    public static String addSuffix(String content, String suffix) {
        if (TextUtils.isEmpty(content)) {
            return suffix;
        }

        if (TextUtils.isEmpty(suffix)) {
            return content;
        }

        return content + suffix;
    }
}
