package com.kurovsky.houseoftheday.highscores;

import android.app.Activity;
import android.util.Log;

import com.kurovsky.houseoftheday.R;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActiveUser;
import com.swarmconnect.SwarmLeaderboard;
import com.swarmconnect.SwarmLeaderboard.GotLeaderboardCB;
import com.swarmconnect.SwarmLeaderboard.SubmitScoreCB;
import com.swarmconnect.SwarmLeaderboardScore;
import com.swarmconnect.delegates.SwarmLoginListener;

public class LoginListener implements SwarmLoginListener {

	public final static LoginListener INSTANCE = new LoginListener();

	private final int LEADERBOARD_ID = 9391;//1141;
	private long mScore = -1; 
	
	private SwarmLeaderboard mLeaderboard;
	
	void SetScore(long score, final Activity activity){
		mScore = score;
		
		if (!Swarm.isInitialized()) {
			Swarm.setAllowGuests(true);
			Swarm.init(activity, 
					Integer.valueOf(R.string.SWARM_APP_ID).intValue(), 
					activity.getString(R.string.SWARM_APP_KEY), 
					LoginListener.INSTANCE);
		}
		else
			SubmitScore();		
	}
	
	// This method is called when the login process has started
	// (when a login dialog is displayed to the user).
	public void loginStarted() {    
	}

	// This method is called if the user cancels the login process.
	public void loginCanceled() {
	}

	// This method is called when the user has successfully logged in.
	public void userLoggedIn(SwarmActiveUser user) {
		// Load our Leaderboard
		SwarmLeaderboard.getLeaderboardById(LEADERBOARD_ID, new GotLeaderboardCB() {
			public void gotLeaderboard(SwarmLeaderboard lb) {
				if (lb == null) {
					Log.v("Unable to retrieve leaderboard", "Swarm");
					return;
				}
				mLeaderboard = lb;
				if (mScore != -1) 
					SubmitScore();
				else
					mLeaderboard.showLeaderboard();	
			}
		});
	}

	// This method is called when the user logs out.
	public void userLoggedOut() {
	}

	private void SubmitScore() {
		if (mLeaderboard == null) 
			return;

		mLeaderboard.submitScore((float)mScore, "", new SubmitScoreCB() {
			public void scoreSubmitted(int arg0) {
				mLeaderboard.showLeaderboard();	
			}
		});
	}
};