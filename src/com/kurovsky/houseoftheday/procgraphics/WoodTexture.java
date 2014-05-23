package com.kurovsky.houseoftheday.procgraphics;

public class WoodTexture {
	int    mLastX = 0;
	int    mLastY = -1;
	double mBMWidth;
	double mBMHeight;
	double mBorderWidth;
	double mRandomness;
	double mShift;
	int    mRow;
	double v;
	gColorSpline mColorSpline;
	double mTurbulenceX = 2.0;
	double mTurbulenceY = 2.0;
	boolean mIsHorizontalPlanks = false;

	public WoodTexture(double PlankWidth, double MortarThickness, boolean isHorizontalPlanks,
			gColorSpline ColorSpline) {
		mIsHorizontalPlanks = isHorizontalPlanks;
		mBMWidth = PlankWidth + MortarThickness;
		mBorderWidth = MortarThickness/mBMWidth;
		mColorSpline = ColorSpline;
	}

	public void SetTurbulence(double tX, double tY) {
		mTurbulenceX = tX;
		mTurbulenceY = tY;
		if (mIsHorizontalPlanks)
			mTurbulenceX *= 5;
		else 
			mTurbulenceY *= 5;
	}

	public gColor getColor(int x, int y) {
		if (mIsHorizontalPlanks)
			if (mLastY != y){
				mLastY = y;
				v = y / mBMWidth;
				mRow = (int)Math.floor(v);
				v -= mRow;
		}

		double u;
		double herH = 1.;
		double herW = 1.;
		if (mIsHorizontalPlanks)
			herH = Noise.HermiteStep(v, 0, mBorderWidth);
		else{
			u = x / mBMWidth;
			u = u - Math.floor(u);
			herW = Noise.HermiteStep(u, 0, mBorderWidth);
		}
		
		u = 0.1 + herW * herH - Noise.Turbulence(x/mTurbulenceX, y/mTurbulenceY, 7, .5);
		return mColorSpline.Get(u);
	}
}


