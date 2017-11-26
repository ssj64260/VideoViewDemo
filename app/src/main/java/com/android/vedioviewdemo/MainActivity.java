package com.android.vedioviewdemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private ImageView ivPicture;

    private List<AdvertBean> mAdvertList;
    private int mCurrentAdVert = 0;

    private ScheduledExecutorService mAdvertThread;
    private final Handler mAdvertHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!mVideoView.isPlaying()) {
                if (mCurrentAdVert >= 0 && mCurrentAdVert < mAdvertList.size()) {
                    final AdvertBean advert = mAdvertList.get(mCurrentAdVert);
                    final String type = advert.getAdvertType();
                    final String path = advert.getAdvertPath();
                    if (AdvertBean.TYPE_IMAGE.equals(type)) {
                        mVideoView.setVisibility(View.GONE);
                        ivPicture.setVisibility(View.VISIBLE);
                        GlideApp.with(MainActivity.this)
                                .load(path)
                                .centerCrop()
                                .dontAnimate()
                                .into(ivPicture);
                        setAdvertPosition();
                    } else {
                        final File file = new File(path);
                        mVideoView.setVisibility(View.VISIBLE);
                        ivPicture.setVisibility(View.GONE);
                        mVideoView.setVideoPath(file.getAbsolutePath());
                        mVideoView.start();
                    }
                }
            }
        }
    };

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdvertHandler.removeCallbacksAndMessages(null);
    }

    private void initData() {
        final String[] urls = {
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705476620&di=facd4327a6d5007d4dea654a5a1ffd40&imgtype=0&src=http%3A%2F%2Ff.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2F5ab5c9ea15ce36d3c0a28ce330f33a87e850b1ba.jpg",
                Environment.getExternalStorageDirectory().getPath() + "/DCIM/wx_camera_1511687626488.mp4",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705500877&di=7786ff5b4706a55ea59bdd2ab34e4b33&imgtype=0&src=http%3A%2F%2Fc.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fa50f4bfbfbedab643102c011fd36afc378311eea.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511705525697&di=c0f4304dc2029b818a313a6bbe5dc7c8&imgtype=0&src=http%3A%2F%2Fh.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fb999a9014c086e061aa904a008087bf40bd1cbd3.jpg",
                Environment.getExternalStorageDirectory().getPath() + "/DCIM/wx_camera_1511687626488.mp4"
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
                    mAdvertHandler.sendEmptyMessage(0);
                }
            }, 100, 5000, TimeUnit.MILLISECONDS);
        }
    }

    private void stopAdvert() {
        mVideoView.stopPlayback();
        ThreadPoolUtil.getInstache().setShutDown(mAdvertThread, 0);
    }
}
