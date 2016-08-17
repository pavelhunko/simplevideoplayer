package my.test.simplevideoplayer;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private int mPosition = 0;
    private ProgressDialog mProgressDialog;
    private MediaController mMediaControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = (VideoView) findViewById(R.id.video_frame);

        if (mMediaControls == null)
            mMediaControls = new MediaController(MainActivity.this);

        mVideoView.setMediaController(mMediaControls);

        mProgressDialog = new ProgressDialog(MainActivity.this);
        //todo support tags from video meta info
        mProgressDialog.setTitle("Title");
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        try {
            mVideoView.setMediaController(mMediaControls);
            mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_1));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mProgressDialog.dismiss();
                mVideoView.seekTo(mPosition);
                if (mPosition == 0) {
                    mVideoView.start();
                } else {
                    mVideoView.pause();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mVideoView.getCurrentPosition());
        mVideoView.pause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPosition = savedInstanceState.getInt("position");
        mVideoView.seekTo(mPosition);
    }
}
