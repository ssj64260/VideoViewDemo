package com.android.vedioviewdemo.service;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggerInterceptor implements Interceptor {

    private boolean showJsonResponse = true;
    private boolean isSimpleInfo = true;

    public LoggerInterceptor() {

    }

    public LoggerInterceptor(boolean showJsonResponse) {
        this.showJsonResponse = showJsonResponse;
    }

    public LoggerInterceptor(boolean isSimple, boolean showJsonResponse) {
        isSimpleInfo = isSimple;
        this.showJsonResponse = showJsonResponse;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (isSimpleInfo) {
            return simpleInformation(chain);
        } else {
            return detailInformation(chain);
        }
    }

    private Response simpleInformation(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        final StringBuilder logcat = new StringBuilder();
        try {
            final String url = request.url().toString();

            logcat.append("---------------------request log ---------------------");
            logcat.append("\nurl : ").append(url);
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    if (isText(mediaType)) {
                        logcat.append("\ncontent : ").append(bodyToString(request));
                    } else {
                        logcat.append("\ncontent : ").append(" maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            logcat.append("\n").append(e.getMessage());
        }

        Response response = chain.proceed(request);

        String resp = "";
        try {
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            logcat.append("\n---------------------response log ---------------------");

            ResponseBody body = clone.body();
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    if (isText(mediaType)) {
                        resp = body.string();
                        logcat.append("\ncontent : ").append(resp);
                        body = ResponseBody.create(mediaType, resp);
                        return response.newBuilder().body(body).build();
                    } else {
                        logcat.append("\ncontent : ").append(" maybe [file part] , too large too print , ignored!");
                    }
                }
            }

            return response;
        } catch (Exception e) {
            logcat.append("\n").append(e.getMessage());
        } finally {
            Logger.d(logcat.toString());
            if (showJsonResponse) {
                Logger.json(resp + "");
            }
        }

        return response;
    }

    private Response detailInformation(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        final StringBuilder logcat = new StringBuilder();
        try {
            final String url = request.url().toString();
            final Headers headers = request.headers();

            logcat.append("---------------------request log start---------------------");
            logcat.append("\nmethod : ").append(request.method());
            logcat.append("\nurl : ").append(url);
            if (headers != null && headers.size() > 0) {
                logcat.append("\nheaders : \n").append(headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    logcat.append("\ncontentType : ").append(mediaType.toString());
                    if (isText(mediaType)) {
                        logcat.append("\ncontent : ").append(bodyToString(request));
                    } else {
                        logcat.append("\ncontent : ").append(" maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            logcat.append("\n").append(e.getMessage());
        } finally {
            logcat.append("\n---------------------request log end---------------------");
        }

        Response response = chain.proceed(request);

        String resp = "";
        try {
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            logcat.append("\n---------------------response log start---------------------");
            logcat.append("\nurl : ").append(clone.request().url());
            logcat.append("\ncode : ").append(clone.code());
            logcat.append("\nprotocol : ").append(clone.protocol());
            if (!TextUtils.isEmpty(clone.message())) {
                logcat.append("\nmessage : ").append(clone.message());
            }

//            final String headers = response.headers().toString();
//            if (!TextUtils.isEmpty(headers)) {
//                logcat.append("\nheaders : \n").append(headers);
//            }

            ResponseBody body = clone.body();
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    logcat.append("\ncontentType : ").append(mediaType.toString());
                    if (isText(mediaType)) {
                        resp = body.string();
                        logcat.append("\ncontent : ").append(resp);
                        body = ResponseBody.create(mediaType, resp);
                        return response.newBuilder().body(body).build();
                    } else {
                        logcat.append("\ncontent : ").append(" maybe [file part] , too large too print , ignored!");
                    }
                }
            }

            return response;
        } catch (Exception e) {
            logcat.append("\n").append(e.getMessage());
        } finally {
            logcat.append("\n---------------------response log end---------------------");

            Logger.d(logcat.toString());
            if (showJsonResponse) {
                Logger.json(resp + "");
            }
        }

        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.toString().equals("application/x-www-form-urlencoded") ||
                    mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("html") ||
                    mediaType.subtype().equals("webviewhtml"))
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
