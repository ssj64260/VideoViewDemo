package com.android.vedioviewdemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.android.vedioviewdemo.service.FileDownLoadObserver;
import com.android.vedioviewdemo.service.ServiceClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private ImageView ivPicture;

    private List<AdvertBean> mAdvertList;
    private int mCurrentAdVert = 0;

    private ScheduledExecutorService mAdvertThread;

    private String mFilePath;
    private boolean mIsDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        startAdvert();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAdvert();
    }

    private void initData() {
        mFilePath = SDCardUtil.getFilesDir(this) + File.separator + "videos";

        final String[] urls = {
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705476620&di=facd4327a6d5007d4dea654a5a1ffd40&imgtype=0&src=http%3A%2F%2Ff.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2F5ab5c9ea15ce36d3c0a28ce330f33a87e850b1ba.jpg",
                "http://10.0.0.112/onlineqs/upload/ad/oe5jv9ep4ogppodolt58fr98gt.mp4",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705500877&di=7786ff5b4706a55ea59bdd2ab34e4b33&imgtype=0&src=http%3A%2F%2Fc.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fa50f4bfbfbedab643102c011fd36afc378311eea.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705525697&di=c0f4304dc2029b818a313a6bbe5dc7c8&imgtype=0&src=http%3A%2F%2Fh.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fb999a9014c086e061aa904a008087bf40bd1cbd3.jpg",
                "http://10.0.0.112/onlineqs/upload/ad/p9hmakggdihcko3pc4nq3oif5a.mp4"
        };
        final String[] urlTypes = {
                AdvertBean.TYPE_IMAGE,
                AdvertBean.TYPE_VEDIO,
                AdvertBean.TYPE_IMAGE,
                AdvertBean.TYPE_IMAGE,
                AdvertBean.TYPE_VEDIO
        };

        mAdvertList = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            final AdvertBean advert = new AdvertBean();
            advert.setAdvertPath(urls[i]);
            advert.setAdvertType(urlTypes[i]);
            mAdvertList.add(advert);
        }
    }

    private void initView() {
        mVideoView = findViewById(R.id.videoview);
        ivPicture = findViewById(R.id.iv_picture);

        final MediaController mc = new MediaController(this);
        mc.setVisibility(View.INVISIBLE);
        mVideoView.setMediaController(mc);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mVideoView.stopPlayback();
                setAdvertPosition();
            }
        });
    }

    private void setAdvertPosition() {
        mCurrentAdVert++;
        if (mCurrentAdVert >= mAdvertList.size()) {
            mCurrentAdVert = 0;
        }
    }

    private void startAdvert() {
        ThreadPoolUtil.getInstache().setShutDown(mAdvertThread, 0);
        if (mAdvertList.size() > 0) {
            mAdvertThread = Executors.newScheduledThreadPool(1);
            mAdvertThread.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mVideoView.isPlaying() && !mIsDownloading) {
                                if (mCurrentAdVert >= 0 && mCurrentAdVert < mAdvertList.size()) {
                                    final AdvertBean advert = mAdvertList.get(mCurrentAdVert);
                                    final String type = advert.getAdvertType();
                                    final String url = advert.getAdvertPath();
                                    if (AdvertBean.TYPE_IMAGE.equals(type)) {
                                        mVideoView.setVisibility(View.GONE);
                                        ivPicture.setVisibility(View.VISIBLE);
                                        GlideApp.with(MainActivity.this)
                                                .load(url)
                                                .centerCrop()
                                                .dontAnimate()
                                                .into(ivPicture);
                                        setAdvertPosition();
                                    } else {
                                        downloadVideo(url, new FileDownLoadObserver<File>() {
                                            @Override
                                            public void onDownLoadSuccess(File file) {
                                                mIsDownloading = false;
                                            }

                                            @Override
                                            public void onDownLoadFail(Throwable throwable) {
                                                mIsDownloading = false;
                                                setAdvertPosition();
                                            }

                                            @Override
                                            public void onProgress(int progress, long total) {

                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }
            }, 100, 5000, TimeUnit.MILLISECONDS);
        }
    }

    private void stopAdvert() {
        mVideoView.stopPlayback();
        ThreadPoolUtil.getInstache().setShutDown(mAdvertThread, 0);
    }

    private void downloadVideo(String url, final FileDownLoadObserver<File> fileDownLoadObserver) {
        final String videoName = getMp4FileName(url);

        final File videoFile = new File(mFilePath, videoName);
        if (videoFile.exists()) {
            mIsDownloading = false;
            mVideoView.setVisibility(View.VISIBLE);
            ivPicture.setVisibility(View.GONE);
            mVideoView.setVideoPath(videoFile.getAbsolutePath());
            mVideoView.start();
        } else {
            mIsDownloading = true;
            ServiceClient.getService().downVideo(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .map(new Function<ResponseBody, File>() {
                        @Override
                        public File apply(@NonNull ResponseBody responseBody) throws Exception {
                            return fileDownLoadObserver.saveFile(responseBody, mFilePath, videoName);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(fileDownLoadObserver);
        }
    }

    private String getMp4FileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }
}
