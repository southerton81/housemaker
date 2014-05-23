package com.kurovsky.houseoftheday;

import com.kurovsky.houseoftheday.options.OptionsActivity;
import com.kurovsky.houseoftheday.R;
import com.swarmconnect.SwarmActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ToggleButton;
import com.google.ads.*;

public class StartMenu extends SwarmActivity  {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start);
		LoadOptions();
		
		AdView adView = (AdView)this.findViewById(R.id.ad);
	    adView.loadAd(new AdRequest());
	}

	public void onWindowFocusChanged (boolean hasFocus){
		ToggleButton tb = (ToggleButton) findViewById(R.id.soundTogglebutton);
		tb.setChecked(OptionsActivity.mPlayMusic == true || OptionsActivity.mPlaySound == true);
	}

	private void LoadOptions(){
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		OptionsActivity.mRollAllowed = Prefs.getBoolean("IsRollAllowed", false);
		OptionsActivity.mGridLinesVisible = Prefs.getBoolean("IsGridLinesVisible", true);
		OptionsActivity.mShowPieceShadow = Prefs.getBoolean("IsShowPieceShadow", true);
	}

	public void onSoundClicked(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		OptionsActivity.mPlayMusic = on;
		OptionsActivity.mPlaySound = on;
		SharedPreferences Prefs = getSharedPreferences("Options", Activity.MODE_PRIVATE);
		SharedPreferences.Editor PrefsEditor = Prefs.edit();
		PrefsEditor.putBoolean("IsPlaySound", OptionsActivity.mPlaySound);
		PrefsEditor.putBoolean("IsPlayMusic", OptionsActivity.mPlayMusic);
		PrefsEditor.commit();
	}
}

