package com.kurovsky.houseoftheday.soundmanager;

import java.util.HashSet;

import com.kurovsky.houseoftheday.options.OptionsActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundManager {
	public final static SoundManager INSTANCE = new SoundManager();
	private static HashSet<MediaPlayer> mMpSet = new HashSet<MediaPlayer>();
	private static MediaPlayer mMusicMediaPlayer = null;

	public void PlaySound(Context context, int resid){
		if (!OptionsActivity.mPlaySound) return;

		final MediaPlayer Mp = MediaPlayer.create(context, resid); 

		Mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mMpSet.remove(mp);
				Mp.stop();
				Mp.release();
			}
		});
		mMpSet.add(Mp);
		Mp.start();
	}
	
	public void Release() {
		for (MediaPlayer mp : mMpSet) {
			try {
				if (mp != null) {
					if (mp.isPlaying()) {
						mp.stop();
					}
					mp.release();
					mp = null;
				}
			} catch (Exception e) {
				Log.e("SoundManager", e.getMessage(), e);
			}
		}
		mMpSet.clear();
		
		if (mMusicMediaPlayer != null){
			mMusicMediaPlayer.release();
			mMusicMediaPlayer = null;
		}
	}

	public void PlayMusic(Context context, int musicResId) {
		if (!OptionsActivity.mPlayMusic) return;

		mMusicMediaPlayer = MediaPlayer.create(context, musicResId); 
		mMusicMediaPlayer.start();
		mMusicMediaPlayer.setLooping(true);
	}

}

/*
public class MusicManager {
	private static final String TAG = "MusicManager";
	public static final int MUSIC_PREVIOUS = -1;
	public static final int MUSIC_MENU = 0;
	public static final int MUSIC_GAME = 1;
	public static final int MUSIC_END_GAME = 2;

	private static HashMap players = new HashMap();
	private static int currentMusic = -1;
	private static int previousMusic = -1;

	public static float getMusicVolume(Context context) {
		String[] volumes = context.getResources().getStringArray(R.array.volume_values);
		String volumeString = PreferenceManager.getDefaultSharedPreferences(context).getString(
				context.getString(R.string.key_pref_music_volume), volumes[PREF_DEFAULT_MUSIC_VOLUME_ITEM]);
		return new Float(volumeString).floatValue();
	}

	public static void start(Context context, int music) {
		start(context, music, false);
	}

	public static void start(Context context, int music, boolean force) {
		if (!force && currentMusic > -1) {
			// already playing some music and not forced to change
			return;
		}
		if (music == MUSIC_PREVIOUS) {
			Log.d(TAG, "Using previous music [" + previousMusic + "]");
			music = previousMusic;
		}
		if (currentMusic == music) {
			// already playing this music
			return;
		}
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
			// playing some other music, pause it and change
			pause();
		}
		currentMusic = music;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
		MediaPlayer mp = players.get(music);
		if (mp != null) {
			if (!mp.isPlaying()) {
				mp.start();
			}
		} else {
			if (music == MUSIC_MENU) {
				mp = MediaPlayer.create(context, R.raw.menu_music);
			} else if (music == MUSIC_GAME) {
				mp = MediaPlayer.create(context, R.raw.game_music);
			} else if (music == MUSIC_END_GAME) {
				mp = MediaPlayer.create(context, R.raw.end_game_music);
			} else {
				Log.e(TAG, "unsupported music number - " + music);
				return;
			}
			players.put(music, mp);
			float volume = getMusicVolume(context);
			Log.d(TAG, "Setting music volume to " + volume);
			mp.setVolume(volume, volume);
			if (mp == null) {
				Log.e(TAG, "player was not created successfully");
			} else {
				try {
					mp.setLooping(true);
					mp.start();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	public static void pause() {
		Collection mps = players.values();
		for (MediaPlayer p : mps) {
			if (p.isPlaying()) {
				p.pause();
			}
		}
		// previousMusic should always be something valid
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}

	public static void updateVolumeFromPrefs(Context context) {
		try {
			float volume = getMusicVolume(context);
			Log.d(TAG, "Setting music volume to " + volume);
			Collection mps = players.values();
			for (MediaPlayer p : mps) {
				p.setVolume(volume, volume);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public static void release() {
		Log.d(TAG, "Releasing media players");
		Collection mps = players.values();
		for (MediaPlayer mp : mps) {
			try {
				if (mp != null) {
					if (mp.isPlaying()) {
						mp.stop();
					}
					mp.release();
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		mps.clear();
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}
}*/