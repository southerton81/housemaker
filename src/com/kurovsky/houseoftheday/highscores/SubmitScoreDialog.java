package com.kurovsky.houseoftheday.highscores;


import com.kurovsky.houseoftheday.R;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActiveUser;
import com.swarmconnect.SwarmLeaderboard;
import com.swarmconnect.SwarmLeaderboard.GotLeaderboardCB;
import com.swarmconnect.delegates.SwarmLoginListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class SubmitScoreDialog {
	
	public static void Show(final Activity activity, final long score) {
		if (score > 0)
			ShowSubmitScoreDlg(activity, score);
		else 
			ShowGameOverDlg(activity, score);
	}
	
	public static void ShowSubmitScoreDlg(final Activity activity, final long score) {
		activity.runOnUiThread(new Runnable() {
			public void run() {

				AlertDialog.Builder DialogBuilder = new AlertDialog.Builder(activity);
				DialogBuilder.setTitle(R.string.GameOver);
				DialogBuilder.setMessage(activity.getString(R.string.ConfirmSubmitScore1)
						+ " " + score + " "  + activity.getString(R.string.ConfirmSubmitScore2)
						+ "\n" + activity.getString(R.string.ConfirmSubmitScore3));

				DialogBuilder.setPositiveButton(R.string.SubmitScore, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						activity.finish();
						LoginListener.INSTANCE.SetScore(score, activity);
					}
				});

				DialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						activity.finish();
					}
				});

				DialogBuilder.show();
			}
		});
	}
	
	public static void ShowGameOverDlg(final Activity activity, final long score) {
		activity.runOnUiThread(new Runnable() {
			public void run() {

				AlertDialog.Builder DialogBuilder = new AlertDialog.Builder(activity);
				DialogBuilder.setTitle(R.string.GameOver);
				DialogBuilder.setMessage("Alas! Score is to low" + "\n" + "to submit to leaderboard");

				DialogBuilder.setPositiveButton(R.string.OK_STR, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						activity.finish();

					}
				});
				DialogBuilder.show();
			}
		});
	}
	

}
