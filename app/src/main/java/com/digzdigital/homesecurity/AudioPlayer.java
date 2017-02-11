package com.digzdigital.homesecurity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Digz on 09/02/2017.
 */

public class AudioPlayer {

    static final String LOG_TAG = AudioPlayer.class.getSimpleName();

    private Context context;
    private MediaPlayer mediaPlayer;
    private AudioTrack progressTone;
    private final static int SAMPLE_RATE = 16000;

    public AudioPlayer(Context context){
        this.context = context.getApplicationContext();
    }

    public void playRingtone(){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        switch (audioManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    mediaPlayer.setDataSource("android.resource://" + context.getPackageName() + "/"+ R.raw.phone_loud1);
                    mediaPlayer.prepare();
                }catch (IOException e){
                    Log.e(LOG_TAG, "Could not setup media player for ringtone");
                    mediaPlayer = null;
                    return;
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                break;
        }
    }

    public void stopRingtone(){
        if (mediaPlayer !=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void playProgressTone(){
        stopProgressTone();
        try {
            progressTone = createProgressTone(context);
            progressTone.play();
        }catch (Exception e){
            Log.e(LOG_TAG, "Couldn't play progress tone", e);
        }
    }

    public void stopProgressTone(){
        if (progressTone !=null){
            progressTone.stop();
            progressTone.release();
            progressTone = null;
        }
    }

    private static AudioTrack createProgressTone(Context context)throws IOException{
        AssetFileDescriptor fileDescriptor = context.getResources()
                .openRawResourceFd(R.raw.progress_tone);
        int length = (int) fileDescriptor.getLength();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                length,
                AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        readFileToBytes(fileDescriptor, data);

        audioTrack.write(data, 0, data.length);
        audioTrack.setLoopPoints(0, data.length/2, 30);

        return audioTrack;
    }

    private static void readFileToBytes(AssetFileDescriptor fileDescriptor, byte[] data) throws IOException{
        FileInputStream inputStream = fileDescriptor.createInputStream();

        int bytesRead = 0;
        while (bytesRead < data.length){
            int res = inputStream.read(data, bytesRead, (data.length-bytesRead));
            if (res == -1){
                break;
            }
            bytesRead += res;
        }
    }
}
