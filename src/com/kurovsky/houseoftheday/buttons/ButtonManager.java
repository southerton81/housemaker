package com.kurovsky.houseoftheday.buttons;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.kurovsky.houseoftheday.Renderer;
import com.kurovsky.houseoftheday.R;

public class ButtonManager {
	public static final int BUTTON_NONE      = 0;
	public static final int BUTTON_MOVELEFT  = 1;
	public static final int BUTTON_MOVERIGHT = 2;
	public static final int BUTTON_ROTATE1   = 3;
	public static final int BUTTON_ROTATE2   = 4;
	public static final int BUTTON_STOPSELL  = 10;
	public static final int BUTTON_LAND	     = 11;
	public static final int BUTTON_PAUSE     = 12;
	
	public static final int BUTTON_RESUME    = 5;
	public static final int BUTTON_NEWGAME   = 6;
	public static final int BUTTON_OPTIONS   = 7;
	public static final int BUTTON_HIGHSCORES= 8;
	public static final int BUTTON_HELP      = 9;
	public static final int BUTTON_PLAY      = 15;
	public static final int BUTTON_SOUND     = 16;
	
	public int mButtonWidth;
	public int mButtonHeight;
	
	public interface Listener{
		public void OnToggleButton(int id, boolean toggled);
	}

	ArrayList<Button> mButtons     = new ArrayList<Button>();
	ArrayList<Listener> mListeners = new ArrayList<Listener>();
	Rect mRect                     = new Rect(0,0,0,0);

	public void AddListener(ButtonManager.Listener listener){
		if (!mListeners.contains(listener))
			mListeners.add(listener);
	}
	
	public void NotifyButtonToggled(int id, boolean toggled){
		for (Listener listener : mListeners)
			listener.OnToggleButton(id, toggled);
	}
	
	public void CreateGameButtons(Context context, Rect GridRc, int ScreenWidth, int ScreenHeight){
		mButtons.clear();
		
		double Density = context.getResources().getDisplayMetrics().density;
		int HorGap = (int) (10 * Density);
		int VerGap = (int) (4 * Density);
		
		int RotateButtonSize = (int) ((ScreenWidth - (HorGap * 4)) / 3);
		int PauseButtonSize = (int) RotateButtonSize / 2;
		
		Button b = new Button(BUTTON_ROTATE1,
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.button),
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.buttonp), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.rotate), null, false);

						
		b.mX = (int) (HorGap);
		b.mY =  (int) (GridRc.bottom + VerGap);
		b.mX2 = b.mX + RotateButtonSize; 
		b.mY2 = (int) (ScreenHeight - VerGap);
		mButtons.add(b);

		Button b2 = new Button(BUTTON_STOPSELL,
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.button),
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.buttonp), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.sell_on), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.sell_off), 
				true);

		b2.mX = (int) ((int) b.mX2 + HorGap);
		b2.mY =  (int) (GridRc.bottom + VerGap);
		b2.mX2 = b2.mX + RotateButtonSize; 
		b2.mY2 = (int) (ScreenHeight - VerGap);
		mButtons.add(b2);

		Button b3 = new Button(BUTTON_LAND, 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.button),
				BitmapFactory.decodeResource(context.getResources(), R.drawable.buttonp), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.down), null, false);
		b3.mX = (int) ((int) b2.mX2 + HorGap);
		b3.mY =  (int) (GridRc.bottom + VerGap);
		b3.mX2 = b3.mX + RotateButtonSize; 
		b3.mY2 = (int) (ScreenHeight - VerGap);
		mButtons.add(b3);
		
		Button bPause = new Button(BUTTON_PAUSE, 
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.button),
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.buttonp), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.pause), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.pause), 
				true);
		
		bPause.mX = (int)ScreenWidth - PauseButtonSize - HorGap;
		bPause.mY = VerGap;
		bPause.mX2 = (int) bPause.mX + PauseButtonSize; 
		bPause.mY2 = (int) GridRc.top - VerGap;
		mButtons.add(bPause);
		
		Button bPlay = new Button(BUTTON_PLAY, 
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.button),
				BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.buttonp), 
				BitmapFactory.decodeResource(context.getResources(), R.drawable.play), null, false);
		bPlay.mVisible = false;
		mButtons.add(bPlay);
	
		mButtonWidth =  RotateButtonSize; 
		mButtonHeight = b2.mY2 - b2.mY;
	}

	public void SetButtonPlayPos(int x1, int y1, int x2, int y2, boolean visible){
		for (Button b : mButtons) {
			if (b.mID == BUTTON_PLAY) {
				b.mX = x1;
				b.mY = y1;
				b.mX2 = x2; 
				b.mY2 = y2;
				b.mVisible = visible;
			}
		}
	}

	public void AddButtons(ArrayList<Button> Buttons){
		mButtons.clear();
		mButtons.addAll(Buttons);
	}
	
	public ArrayList<Button> GetButtons(){
		return mButtons;
	}

	public void RenderButtons(Canvas c){
		for (Button b : mButtons) {
			if (!b.mVisible) continue;
			
			mRect.set(b.mX, b.mY, b.mX2, b.mY2);
			Bitmap Bmp;
			Bitmap BmpLogo = b.mToggled ? b.mLogoToggled : b.mLogo;

			if (b.mToggleButton)
				Bmp = b.mToggled ? b.mButtonPressedBmp : b.mButtonBmp;
			else 
				Bmp = b.mPressed ? b.mButtonPressedBmp : b.mButtonBmp;

			Renderer.INSTANCE.RenderButton(c, b, mRect, Bmp, BmpLogo);
		}
	}
	
	public boolean DoTouchDown(float x, float y) {
		for (Button b : mButtons) {
			mRect.set(b.mX, b.mY, b.mX2, b.mY2);
			if (mRect.contains((int)x, (int)y)){
				
				for (Button bAll : mButtons)
					bAll.SetPressed(false);
				
 				b.SetPressed(true);

 				if (b.mToggleButton)
 					NotifyButtonToggled(b.mID, b.mToggled);
				return true;
			}
		}
		
		return false;
	}

	public int GetPressedButtonCode() {
		for (Button b : mButtons) {
			if (b.mPressed == true)
				return b.mID;
		}
		return BUTTON_NONE;
	}
	
	public void DoTouchUp() {
		for (Button b : mButtons)
			b.SetPressed(false);
	}
	
	public void DoUntoggle(int id){
		for (Button b : mButtons){
			if (b.mID == id)
				b.mToggled = false;
		}
	}
}

