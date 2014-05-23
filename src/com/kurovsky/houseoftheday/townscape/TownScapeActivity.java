package com.kurovsky.houseoftheday.townscape;

import android.os.Bundle;
import android.view.Window;

import com.kurovsky.houseoftheday.Game;
import com.kurovsky.houseoftheday.Grid;
import com.kurovsky.houseoftheday.R;
import com.swarmconnect.SwarmActivity;

public class TownScapeActivity extends SwarmActivity {  
	
	static private int  mStartX = 0; 
	static private Grid mTownScapeGrid1 = new Grid(Game.GRID_DIMENSIONX, Game.GRID_DIMENSIONY);
	
	public static Grid GetTownScapeGrid1(){
		return mTownScapeGrid1;
	}
	
	public static void Reset() {
		mStartX = 0; 
		mTownScapeGrid1.Clear();
	}
	
	public static void AddHouse(Grid grid) {
		if (mStartX >= Game.GRID_DIMENSIONX) return;
		mStartX = grid.AddToGridBottom(mTownScapeGrid1, mStartX);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.townscape);
	}
}
	