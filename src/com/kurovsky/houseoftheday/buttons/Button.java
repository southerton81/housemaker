package com.kurovsky.houseoftheday.buttons;

import android.graphics.Bitmap;

public class Button {
	public int mID;
	public int mX;
	public int mY;
	public int mX2;
	public int mY2;
	public boolean mPressed = false;
	public boolean mToggled = false;
	public boolean mToggleButton = false;
	public boolean mVisible = true;
	public Bitmap mButtonBmp = null;
	public Bitmap mButtonPressedBmp = null;
	public Bitmap mLogo = null;
	public Bitmap mLogoToggled = null;
	public String mString;
	
	public Button(int ID, Bitmap Bmp){
		mID = ID;
		mButtonBmp = Bmp;
	}

	public Button(int ID, Bitmap Bmp, Bitmap Bmp2, Bitmap Logo, Bitmap LogoToggled, boolean IsToggleButton){
		mID = ID;
		mButtonBmp = Bmp;
		mButtonPressedBmp = Bmp2;
		mLogo = Logo;
		mLogoToggled = LogoToggled;
		mToggleButton = IsToggleButton;
	}

	public void SetPressed(boolean pressed){
		if (mToggleButton){
			if (pressed == true)
				mToggled = !mToggled;
		}
		else
			mPressed = pressed;
	}

}
