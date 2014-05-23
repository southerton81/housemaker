package com.kurovsky.houseoftheday.procgraphics;

public class BrickTexture {
	int    mLastY = -1;
	double mBMWidth;
	double mBMHeight;
	double mBorderWidth;
	double mBorderHeight;
	double mRandomness;
	double mShift;
	int    mRow;
	double v;
	gColorSpline mColorSpline;
	double mTurbulenceX = 2.0;
	double mTurbulenceY = 2.0;
	double u;

	public BrickTexture(double BrickWidth, double BrickHeight, double MortarThickness,
			gColorSpline ColorSpline) {
		mBMWidth = BrickWidth + MortarThickness;
		mBMHeight= BrickHeight+ MortarThickness;
		mBorderWidth = MortarThickness/mBMWidth;
		mBorderHeight= MortarThickness/mBMHeight;
		mColorSpline = ColorSpline;
	}
	
	public void SetTurbulence(double tX, double tY) {
		mTurbulenceX = tX;
		mTurbulenceY = tY;
	}
	
	public gColor getColor(int x, int y) {
		if (mLastY != y){
			mLastY = y;
			v = y/mBMHeight; // Varies [0,1] over one brick
			mRow = (int)Math.floor(v);
			v -= mRow;
			mRandomness = Noise.NoiseFunction(mRow)/14;
			mShift = ((mRow % 2 == 0) ? 0 : 0.5 + mRandomness);
		}

	    u = x/mBMWidth; // Varies [0,1] over one brick
		u = (u+mShift) - Math.floor(u+mShift);
		double herW = Noise.HermiteStep(u, 0, mBorderWidth + mRandomness)
		- Noise.HermiteStep(u, 1-mBorderWidth+mRandomness, 1);
		double herH = Noise.HermiteStep(v, 0, mBorderHeight + mRandomness)
		- Noise.HermiteStep(v, 1-mBorderHeight+mRandomness, 1);
		u = 0.1 + herW * herH - Noise.Turbulence(x/mTurbulenceX, y/mTurbulenceY, 5, .5);
		return mColorSpline.Get(u);   
	}

}
