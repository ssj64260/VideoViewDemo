package com.android.vedioviewdemo.config;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 网络请求接口 api
 */

public interface ServiceApi {

    String BASE_HOST = "http://10.0.0.112/onlineqs/";//TODO 更改服务器

    String VIDEO_URL = "http://10.0.0.112/onlineqs/upload/ad/0p3kkvdl3ij67p393p1ufgqcsl.mp4";

    @Streaming
    @GET
    Observable<ResponseBody> downVideo(@Url String url);
}
