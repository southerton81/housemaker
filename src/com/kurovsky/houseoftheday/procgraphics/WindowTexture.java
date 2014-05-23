package com.kurovsky.houseoftheday.procgraphics;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

public class WindowTexture {

	private class RealPoint{
		RealPoint(double x, double y){
			mX = x;
			mY = y;
		}
		public double mX;
		public double mY;
	}

	gColorSpline mColorSplineFrame;
	RealPoint mWndSize;
	RealPoint mWndBorders;
	RectF 	  mOval;
	double v;
	int mRow;
	int mLastY = -1;

	public WindowTexture(double FrameThickness, double WindowWidth, double WindowHeight,
			gColorSpline ColorSplineFrame){	
		mColorSplineFrame = ColorSplineFrame;
		mWndSize = new RealPoint(WindowWidth, WindowHeight);
		mWndBorders = new RealPoint(FrameThickness/mWndSize.mX, FrameThickness/mWndSize.mY);
		mOval = new RectF();
		mOval.set((float)0, 0, (float)WindowWidth, (float)WindowHeight);
	}

	public void Draw(Canvas c, Paint paint, Random RNG, int numLeafs, gColorSpline ColorSplineGlass){
		makeGlassBckgnd(c, ColorSplineGlass, (int)mWndSize.mX, (int)mWndSize.mY);	

		gColor col;
		for (int y = 0; y < (int)mWndSize.mY; y++)
			for (int x = 0; x < (int)mWndSize.mX; x++){
				col = getColor(x, y);
				if (col != null){
					paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
					c.drawPoint(x, y, paint);
				} 
			}
		
		makeWindowInternalFrames(mColorSplineFrame, paint, c, RNG, numLeafs, mWndSize.mX, mWndSize.mY);
		
		/*col = getColor(0, 0);
		paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
		paint.setStyle(Paint.Style.STROKE);
		c.drawArc(mOval, 180, 180, false, paint);*/
	}
/*	
	public void DrawRoofWindow(Canvas c, Bitmap bmp, Paint paint, Random RNG, int numLeafs, gColorSpline ColorSplineGlass){
		makeGlassBckgnd(c, ColorSplineGlass, (int)mWndSize.mX, (int)mWndSize.mY);	
		
		gColor col;
		for (int y = 0; y < (int)mWndSize.mY; y++)
			for (int x = 0; x < (int)mWndSize.mX; x++){
				col = getColor(x, y);
				if (col != null){
					paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
					c.drawPoint(x, y, paint);
				} 
			}

		paint.setColor(Color.BLACK);
		c.drawArc(mOval, 180, 180, false, paint);
		
		//make upper part transparent
		int color = Color.argb(0, 0, 0, 0);
		for (int x = 0; x < (int)mWndSize.mX; x++){
			for (int y = 0; y < (int)mWndSize.mY; y++){
				if (bmp.getPixel(x, y) == Color.BLACK){
					bmp.setPixel(x, y, color);
				    break;
				}
				bmp.setPixel(x, y, color);
			}
		}

		paint.setStrokeWidth((int)Math.ceil(mWndBorders.mY+1));
		col = getColor((int)mWndSize.mX/2, 0);
		paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
		c.drawArc(mOval, 180, 180, false, paint);

		paint.setStrokeWidth((int)1);
		mOval.inset(5, 6);
		paint.setColor(Color.WHITE);
		c.drawArc(mOval, 180, 180, false, paint);

		//makeWindowInternalFrames(mColorSplineFrame, paint, c, RNG, numLeafs, mWndSize.mX, mWndSize.mY);
		int y = (int) (mWndSize.mY / 4);
		for (int x = 0; x < (int)mWndSize.mX; x++){
				col = getColor(x, y);
				if (col != null){
					paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
					c.drawPoint(x, y, paint);
				} 
			}
	}*/
	
	public gColor getColor(int x, int y) {
		if (mLastY != y){
			mLastY = y;
			v = y / mWndSize.mY; 
			mRow = (int)Math.floor(v);
			v -= mRow;
		}
		double u=x/mWndSize.mX;
		u = u - Math.floor(u);
		double herW = Noise.HermiteStep(u, 0, mWndBorders.mX) - Noise.HermiteStep(u, 1 - mWndBorders.mX, 1);
		double herH = Noise.HermiteStep(v, 0, mWndBorders.mY) - Noise.HermiteStep(v, 1 - mWndBorders.mY, 1);
		u = herW * herH;
		if (u == 1) return null;
		return mColorSplineFrame.Get(u);   
	}
	
	public void makeGlassBckgnd(Canvas c, gColorSpline ColorSplineGlass, int w, int h){
		Paint paint = new Paint();
		paint.setAntiAlias(false);
		for (int y = 0; y < h; y++)
		{
			double t = (double)y/h;  // t is how far along: range [0,1]
			gColor col = ColorSplineGlass.Get(t);
			paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));			
			c.drawLine(0, y, w, y, paint);
		}
	}
	
	public void makeWindowInternalFrames(gColorSpline ColorSplineLeafs, Paint paint, Canvas c,
			Random RNG, int numFrames, double wndWidth, double wndHeight){
		Point lines[] = null;
		int div;
		
		switch (numFrames){
		case 1:	return;
		case 2:
			lines = new Point[1];
			div = RNG.nextInt(3) + 2;
			int isHorizontal = RNG.nextInt(2);
			if (isHorizontal == 1)
				lines[0] = new Point(0, (int)(wndHeight / div));
			else
				lines[0] = new Point((int)(wndWidth / div), 0);				
			break;
		case 3:
			lines = new Point[2];
			div = RNG.nextInt(3) + 2;
			lines[0] = new Point(0, (int)(wndHeight / div));
			div = RNG.nextInt(3) + 2;
			lines[1] = new Point((int)(wndWidth / div), 0);				
			break;
		case 4:
			lines = new Point[3];
			div = RNG.nextInt(3) + 3;
			lines[0] = new Point(0, (int)(wndHeight / div));
			lines[1] = new Point(0, (int)(wndHeight - (wndHeight / div)));		
			div = RNG.nextInt(3) + 3;
			lines[2] = new Point((int)(wndWidth / div), 0);			
			break;
		case 5:
			lines = new Point[4];
			div = RNG.nextInt(3) + 3;
			lines[0] = new Point(0, (int)(wndHeight / div));
			lines[1] = new Point(0, (int)(wndHeight - (wndHeight / div)));		
			div = RNG.nextInt(3) + 3;
			lines[2] = new Point((int)(wndWidth / div), 0);	
			lines[3] = new Point((int)(wndWidth - (wndWidth / div)), 0);
			break;
		}

		if (lines == null) return;
		for (int nLines = 0; nLines < lines.length; nLines++)
		{
			boolean isHorizontal = lines[nLines].x == 0 ? true : false;
			int len = (int) (isHorizontal ? wndWidth : wndHeight);

			gColor col;
			for (int i = 1; i < len - 1; i++){
				double t = (double)i / len;
				col = ColorSplineLeafs.Get(t);
				paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
				if (isHorizontal)
					c.drawPoint(i, lines[nLines].y, paint);
				else
					c.drawPoint(lines[nLines].x, i, paint);
			}
		}				
	}
}
