package com.kurovsky.houseoftheday.options;

import com.kurovsky.houseoftheday.R;
import com.swarmconnect.SwarmActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class OptionsActivity extends SwarmActivity  {
	public static boolean mRollAllowed = false;
	public static boolean mGridLinesVisible = true;
	public static boolean mShowPieceShadow = true;
	public static boolean mPlaySound = true;
	public static boolean mPlayMusic = true;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.options);
		
		/*final CheckBox RollCheckbox = (CheckBox) findViewById(R.id.checkBox1);
		RollCheckbox.setChecked(mRollAllowed); 		 
		RollCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnClickRollCheckbox(v);
			}
		});*/
		
		final CheckBox GridCheckbox = (CheckBox) findViewById(R.id.checkBox2);
		GridCheckbox.setChecked(mGridLinesVisible); 		 
		GridCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnClickGridCheckbox(v);
			}
		});
		
		final CheckBox ShadowCheckbox = (CheckBox) findViewById(R.id.checkBox3);
		ShadowCheckbox.setChecked(mShowPieceShadow); 		 
		ShadowCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnClickShadowCheckbox(v);
			}
		});
		
		final CheckBox SoundCheckbox = (CheckBox) findViewById(R.id.checkBox4);
		SoundCheckbox.setChecked(mPlaySound); 		 
		SoundCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnClickSoundCheckbox(v);
			}
		});
		
		final CheckBox MusicCheckbox = (CheckBox) findViewById(R.id.checkBox5);
		MusicCheckbox.setChecked(mPlayMusic); 		 
		MusicCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnClickMusicCheckbox(v);
			}
		});
	}

	void OnClickRollCheckbox(View v){
		mRollAllowed = (((CheckBox) v).isChecked());
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsRollAllowed", mRollAllowed);
		PrefsEditor.commit();
	}
	
	void OnClickGridCheckbox(View v){
		mGridLinesVisible = (((CheckBox) v).isChecked());
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsGridLinesVisible", mGridLinesVisible);
		PrefsEditor.commit();
	}
	
	void OnClickShadowCheckbox(View v){
		mShowPieceShadow = (((CheckBox) v).isChecked());
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsShowPieceShadow", mShowPieceShadow);
		PrefsEditor.commit();
	}
	
	void OnClickSoundCheckbox(View v){
		mPlaySound = (((CheckBox) v).isChecked());
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsPlaySound", mPlaySound);
		PrefsEditor.commit();
	}
	
	void OnClickMusicCheckbox(View v){
		mPlayMusic = (((CheckBox) v).isChecked());
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsPlayMusic", mPlayMusic);
		PrefsEditor.commit();
	}
	
}


	