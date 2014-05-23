package com.kurovsky.houseoftheday;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kurovsky.houseoftheday.buttons.Button;
import com.kurovsky.houseoftheday.buttons.ButtonManager;
import com.kurovsky.houseoftheday.buttons.TextButton;
import com.kurovsky.houseoftheday.helpactivity.HelpActivity;
import com.kurovsky.houseoftheday.highscores.LoginListener;
import com.kurovsky.houseoftheday.options.OptionsActivity;
import com.kurovsky.houseoftheday.procgraphics.Noise;
//import com.kurovsky.houseoftheday.townscape.TownScapeActivity;
import com.swarmconnect.Swarm;

class StartMenuView extends View {
	 public static volatile boolean mDataInitialized = false; 
	 private Rect SourceRect = new Rect(0,0,0,0);
	 private Rect DestRect = new Rect(0,0,0,0);
	 private Paint mPaint = new Paint();
	 private int mViewWidth = -1;
	 private int mViewHeight = -1;
	 private ButtonManager mButtonMan;

	 private Grid    mHotD = new Grid(Game.GRID_DIMENSIONX, Game.GRID_DIMENSIONY);
	 private Point   mTileSize = new Point(-1, -1);
	 private Point   mScaledTileSize = new Point(-1, -1);
	 private Point   mPoint = new Point(0, 0);
	 private RectF   mLogoOval;
	 private Path    mLogoPath = new Path();
	 private long    mButtonHeight = 0;
	 private String  mResumeStr;
	 private String  mNewGameStr;
	 private String  mOptionsStr;
	 private String  mLeaderboardStr;
	 private String  mHelpStrId;
	 private String  mLocalRecordStr;
	 private long 	 mLocalRecordVal = 0;
	 
	 private Renderer mRenderer = new Renderer();
	 
	 public StartMenuView(Context context, AttributeSet attr) {
		 super(context, attr);
		 
		 if (CommonResources.BackgroundBitmap == null)
			 CommonResources.BackgroundBitmap = BitmapFactory.decodeResource(getResources(), com.kurovsky.houseoftheday.R.drawable.fon);
		
		 SourceRect.set(0,0,CommonResources.BackgroundBitmap.getWidth(),CommonResources.BackgroundBitmap.getHeight());
	
		 if (CommonResources.mFont == null)
			 CommonResources.mFont = Typeface.createFromAsset(context.getAssets(),"fonts/" + "FredokaOne-Regular.ttf");
	 
		 mResumeStr = context.getString(R.string.Resume);
		 mNewGameStr = context.getString(R.string.NewGame);
		 mOptionsStr = context.getString(R.string.Options);
		 mLeaderboardStr = context.getString(R.string.Leaderboard);
		 mHelpStrId = context.getString(R.string.Help);
		 mLocalRecordStr = context.getString(R.string.LocalRecord);
	 }

     protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
         super.onSizeChanged(xNew, yNew, xOld, yOld);
         mViewWidth = xNew;
         mViewHeight = yNew;
         InitializeData();
     }

     protected void onWindowVisibilityChanged(int visibility){
    	 if (visibility == View.INVISIBLE || visibility == View.GONE)
    		 mDataInitialized = false;
     }

     private void InitializeData(){
    	 mLogoOval = new RectF(0,0,mViewWidth, mViewWidth);
    	 CreateButtons(mViewWidth, mViewHeight);
    	 RestoreHouseOfTheDay();
    	 mDataInitialized = true;
     }

     private void RestoreHouseOfTheDay() {
    	 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    	 mHotD.Restore(sp, "HotD"); 
    	 if (!mHotD.IsEmpty()) 
    	 {
    		 long Seed = sp.getLong("HotDSeed", Noise.GenerateTodaysSeed());
    		 Rect Rc = new Rect(0,0,0,0);
    		 mTileSize = mRenderer.SetupImages(Seed, getContext(), mViewWidth, mViewHeight, Rc);
    	 }
    	 
    	 mLocalRecordVal = sp.getLong("LocalRecord", 0);
     }

     private boolean IsGameStored(){
    	 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    	 return sp.getBoolean("IsGameStored", false);
     }
	 
     private void CreateButtons(int width, int yNew) {
    	 if (mButtonMan == null) mButtonMan = new ButtonManager();

    	 int StartY = (int)((double) yNew / 2);
    	 int Height = yNew / 9;

    	 ArrayList<Button> Buttons = new ArrayList<Button>();
    	 TextButton tb;
    	 
    	 if (IsGameStored()) {
    		 tb = new TextButton(ButtonManager.BUTTON_RESUME, mResumeStr);
    		 tb.mX = 0; tb.mY = StartY; tb.mX2 = width; tb.mY2 = StartY + Height;
    		 StartY = tb.mY2;
    		 Buttons.add(tb);
    	 }

    	 tb = new TextButton(ButtonManager.BUTTON_NEWGAME, mNewGameStr);
    	 tb.mX = 0; tb.mY = StartY; tb.mX2 = width; tb.mY2 = StartY + Height;
    	 StartY = tb.mY2;
    	 Buttons.add(tb);

    	 tb = new TextButton(ButtonManager.BUTTON_OPTIONS, mOptionsStr);
    	 tb.mX = 0; tb.mY = StartY; tb.mX2 = width; tb.mY2 = StartY + Height; 
    	 Buttons.add(tb);
    	 StartY = tb.mY2;

    	 tb = new TextButton(ButtonManager.BUTTON_HIGHSCORES, mLeaderboardStr);
    	 tb.mX = 0; tb.mY = StartY; tb.mX2 = width; tb.mY2 = StartY + Height; 
    	 StartY = tb.mY2;
    	 Buttons.add(tb);
    	 
    	 tb = new TextButton(ButtonManager.BUTTON_HELP, mHelpStrId);
    	 tb.mX = 0; tb.mY = StartY; tb.mX2 = width; tb.mY2 = StartY + Height; 
    	 StartY = tb.mY2;
    	 Buttons.add(tb);
    
    	 mButtonMan.AddButtons(Buttons);
     }
	 
     public void onDraw(Canvas canvas) {	 
    	 if (CommonResources.BackgroundBitmap != null) {
    		 DestRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
    		 canvas.drawBitmap(CommonResources.BackgroundBitmap, SourceRect, DestRect, null);	
    	 }
    	
    	 if (!mDataInitialized)
    		 InitializeData(); 
    	 
    	 if (!mHotD.IsEmpty()) 
    		 DrawHouseOfTheDay(canvas); 

       	 DrawLogo(canvas);
    	 DrawButtons(canvas);
     }
     
     void DrawLogo(Canvas c) {
    	 mLogoPath.reset();
    	 mLogoPath.addArc(mLogoOval, -180, 180); 

    	 int TextHeight = mViewHeight/10;
    	 mPaint.reset();
		 mPaint.setAntiAlias(true);
		 mPaint.setTypeface(CommonResources.mFont);
	     mPaint.setTextSize(TextHeight);
		 mPaint.setTextAlign(Align.CENTER);
	     mPaint.setShadowLayer(1.5f, 0f, 1f, Color.rgb(90, 0, 90));
	     mPaint.setColor(Color.rgb(253, 196, 45));

	     if (mLocalRecordVal != 0){
	    	 StringBuilder Sb = new StringBuilder();
	    	 Sb.append(mLocalRecordStr);
	    	 Sb.append(" ");
	    	 Sb.append(mLocalRecordVal);
	    	 Sb.append("$");
	    	 c.drawTextOnPath(Sb.toString(), mLogoPath, 0, TextHeight, mPaint); 
	     }
	     else
	    	 c.drawTextOnPath("HOUSE BLOCKS", mLogoPath, 0, TextHeight, mPaint); 
     }

	 void DrawButtons(Canvas c) {
		 if (mButtonMan == null) return; 
		 int TextHeight = mViewHeight/13;
		 
		 mPaint.reset();
		 mPaint.setAntiAlias(true);
		 mPaint.setTypeface(CommonResources.mFont);
	     mPaint.setTextSize(TextHeight);
		 mPaint.setTextAlign(Align.CENTER);
		 mPaint.setShadowLayer(1.5f, 0f, 1f, Color.rgb(90, 0, 90));
	     mPaint.setTextSize(TextHeight);
		 ArrayList<Button> Buttons = mButtonMan.GetButtons();

		 for (Button b : Buttons) {
			 Button tb = b; 
			 if (tb.mString != null){
				 if (b.mPressed) 
					 mPaint.setColor(Color.rgb(130, 0, 130));
				 else
					 mPaint.setColor(Color.rgb(255, 255, 255));

				 c.drawText(tb.mString, tb.mX2 / 2, tb.mY, mPaint);
				 
				 mButtonHeight = ((tb.mY2 - tb.mY) / 2);
			 }		 
		 }
	 }

	 void DrawHouseOfTheDay(Canvas c) {
		 if (mTileSize.x == -1) return;
		 int HotWidth  = mHotD.GetRealWidth() * mTileSize.x;
		 int HotHeight = mHotD.GetRealHeight()* mTileSize.y;
		 double EndY = (int)((double) mViewHeight / 2);   //Where the buttons start
		 
	     int TextHeight = mViewHeight/13;
	     EndY -= TextHeight;
	     
		 float Scale = 1f;
		 
		 if (HotHeight > EndY)
			 Scale = ((float)EndY / (float)HotHeight);
			 
	     mScaledTileSize.set((int)(mTileSize.x * Scale), (int)(mTileSize.y * Scale));
	     
	     HotWidth  = mHotD.GetRealWidth() * mScaledTileSize.x;
		 HotHeight = mHotD.GetRealHeight()* mScaledTileSize.y;
		 mPoint.x = (mViewWidth / 2) - (HotWidth / 2);
		 mPoint.y = (int) ((EndY / 2) - (HotHeight / 2));
	
		 mHotD.RenderScaledByMbr(mRenderer, c, mPoint, mScaledTileSize, Scale);
	 }
	
    public boolean onTouchEvent(MotionEvent event) {	
    	if (event.getAction() == MotionEvent.ACTION_DOWN)
    		mButtonMan.DoTouchDown(event.getX(), event.getY() + mButtonHeight);
    	if (event.getAction() == MotionEvent.ACTION_MOVE)
    		mButtonMan.DoTouchDown(event.getX(), event.getY() + mButtonHeight);
    	else if (event.getAction() == MotionEvent.ACTION_UP)
    		OnTouchUp();
    	
    	invalidate();
		return true;
    }

    private void OnTouchUp(){
    	int ButtonCode = mButtonMan.GetPressedButtonCode();
    	
    	if (ButtonCode != ButtonManager.BUTTON_NONE){
    		switch (ButtonCode){
    		case ButtonManager.BUTTON_RESUME:     OnResumeGame(); break;
    		case ButtonManager.BUTTON_NEWGAME:    OnNewGame(); break;
    		case ButtonManager.BUTTON_HELP:       OnHelp(); break; /*((StartMenu)getContext()).finish(); break;*/
    		case ButtonManager.BUTTON_OPTIONS:    OnOptions(); break;
    		case ButtonManager.BUTTON_HIGHSCORES: OnHighscores(); break;
    		}
    	}
    	
    	mButtonMan.DoTouchUp();
    }
    
    private void OnResumeGame() {
    	Game.mRestoreGame = true;
    	Intent i = new Intent(getContext(), Housemaker.class);
    	getContext().startActivity(i);
    }
    
    private void OnNewGame() {
    	Game.mRestoreGame = false;
    	Intent i = new Intent(getContext(), Housemaker.class);
    	getContext().startActivity(i);
    }
    
    private void OnOptions() { 
    	Intent i = new Intent(getContext(), OptionsActivity.class);
    	getContext().startActivity(i);
    }
    
    private void OnHighscores() { 
		if (!Swarm.isInitialized()){
			Swarm.setAllowGuests(true);
			Swarm.init((Activity) getContext(), 
					Integer.valueOf(com.kurovsky.houseoftheday.R.string.SWARM_APP_ID).intValue(), 
					getContext().getString(com.kurovsky.houseoftheday.R.string.SWARM_APP_KEY), 
					LoginListener.INSTANCE);
		}
		else
			Swarm.showLeaderboards();
    }
    
    private void OnHelp() {
    	Intent i = new Intent(getContext(), HelpActivity.class);
    	getContext().startActivity(i);
    }
    
}