package com.android.vedioviewdemo;

/**
 * 广告
 */

public class AdvertBean {

    public static final String TYPE_VEDIO = "2";
    public static final String TYPE_IMAGE = "1";

    private String advertPath;
    private String advertType;

    public String getAdvertPath() {
        return advertPath;
    }

    public void setAdvertPath(String advertPath) {
        this.advertPath = advertPath;
    }

    public String getAdvertType() {
        return advertType;
    }

    public void setAdvertType(String advertType) {
        this.advertType = advertType;
    }
}
