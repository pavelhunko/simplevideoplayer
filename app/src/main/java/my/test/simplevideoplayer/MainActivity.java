package my.test.simplevideoplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.github.developerpaul123.filepickerlibrary.enums.Request;
import com.github.developerpaul123.filepickerlibrary.enums.ThemeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private ListView mListView;
    private RelativeLayout mHintScreen;
    private int mPosition = 0;
    private MediaController mMediaControls;
    private ArrayList<File> mVideoPlaylist;
    private int nowplaying = 1;

    private final int REQUEST_DIRECTORY = 101;
    private final String fileFilter = ".mp4";
    private final String VIDEO_MIME = "video/mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = (VideoView) findViewById(R.id.video_frame);
        mListView = (ListView) findViewById(R.id.listview);
        mHintScreen = (RelativeLayout) findViewById(R.id.hint_screen);
        initFAB();
        initMediaControls();
    }

    private void initListAdapter(){
        VideoListAdapter adapter = new VideoListAdapter(this, mVideoPlaylist);
        mListView.setAdapter(adapter);
    }

    private void initMediaControls() {
        if (mMediaControls == null)
            mMediaControls = new MediaController(MainActivity.this);
        mMediaControls.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });
    }

    private void initFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pickVideosFromSDcard();
                }
            });
        }
    }

    private void addOnPreparedListener() {
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        mVideoView.setMediaController(mMediaControls);
                        mMediaControls.setAnchorView(mVideoView);

                    }
                });
            }
        });
    }

    private void showDefaultGreetingsScreen(boolean show){
        if (!show){
            mHintScreen.setVisibility(View.GONE);
        } else {
            mHintScreen.setVisibility(View.VISIBLE);
        }

    }

    private void pickVideosFromSDcard() {
        Intent filePickerDialogIntent = new Intent(this, FilePickerActivity.class);
        filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
        filePickerDialogIntent.putExtra(FilePickerActivity.REQUEST, Request.DIRECTORY);
        filePickerDialogIntent.putExtra(FilePickerActivity.MIME_TYPE, VIDEO_MIME);
        startActivityForResult(filePickerDialogIntent, REQUEST_DIRECTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_DIRECTORY) && (resultCode == RESULT_OK)) {
            mVideoPlaylist = fetchlistOfVideos(data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH));
            if (mVideoPlaylist == null || mVideoPlaylist.isEmpty()) {
                showDefaultGreetingsScreen(true);
                Toast.makeText(this, getResources().getString(R.string.no_videos_popup), Toast.LENGTH_SHORT).show();
            } else {
                initListAdapter();
                playVideo(getVideoUri(mVideoPlaylist.get(0)));
                showDefaultGreetingsScreen(false);
            }
        }
    }

    private void next() {
        mVideoView.stopPlayback();
        nowplaying++;
        if (nowplaying >= mVideoPlaylist.size())
            nowplaying = 0;
        mVideoView.setVideoURI(getVideoUri(mVideoPlaylist.get(nowplaying)));
        mVideoView.start();
    }

    private void previous() {
        mVideoView.stopPlayback();
        nowplaying--;
        if (nowplaying < 0)
            nowplaying = mVideoPlaylist.size() - 1;
        mVideoView.setVideoURI(getVideoUri(mVideoPlaylist.get(nowplaying)));
        mVideoView.start();
    }

    private ArrayList<File> fetchlistOfVideos(String filepath) {
        if (filepath.isEmpty())
            return null;
        ArrayList<File> videos = new ArrayList<>();
        for (File f : new File(filepath).listFiles()) {
            if (f.getName().endsWith(fileFilter))
                videos.add(f);
        }
        return videos;
    }


    private Uri getVideoUri(File f) {
        return Uri.parse(f.toString());
    }

    private void playVideo(Uri uri) {
        mVideoView.setVideoURI(uri);
        addOnPreparedListener();
        mVideoView.requestFocus();
        mVideoView.start();
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

    private class VideoListAdapter extends ArrayAdapter<File> {

        public VideoListAdapter(Context context, ArrayList<File> videos) {
            super(context,0, videos);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            File file = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_view, parent, false);
            }
            TextView tvName = (TextView) convertView.findViewById(R.id.file_name);
            tvName.setText(file.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nowplaying = position;
                    playVideo(getVideoUri(mVideoPlaylist.get(position)));
                }
            });

            return convertView;
        }
    }
}
