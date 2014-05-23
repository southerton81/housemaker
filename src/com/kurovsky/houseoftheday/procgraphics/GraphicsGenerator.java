package com.kurovsky.houseoftheday.procgraphics;

import java.util.Random;

import com.kurovsky.houseoftheday.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

public class GraphicsGenerator {
	//private Canvas		 mWorkCanvas = new Canvas();
	private Paint 		 mPaint = new Paint();
	private int   		 mCntTypes; 
	private int   		 mCntStyles;	
	private int   		 mWidth; 
	private int   		 mHeight;
	private gColorSpline mColorSpline = new gColorSpline(); 
	private gColorSpline mGlassColorSpline = new gColorSpline(); 
	private Random       mRng;
	private ColorPicker  mColorPicker;
	private Context		 mContext;

	public GraphicsGenerator(long seed, Context context){	
		if (seed == 0) seed = Noise.GenerateTodaysSeed();
		mRng = new Random(seed);
		mColorPicker = new ColorPicker(mRng);
		mPaint.setAntiAlias(false);
		mContext = context;
	}

	public void InitTileSize(int w, int h, float density){
		mWidth = w;
		mHeight = h;
	} 

	public void InitStylesAndTypesCount(int cTypes, int cStyles){
		mCntTypes = cTypes; 
		mCntStyles = cStyles;
	}

	public void GenerateTodaysWalls(BitmapDrawable[][] outDrawables){
		Canvas C = new Canvas();
		for (int t = 0; t < mCntTypes; t++) {	
			Bitmap wallBmp = makeWallBckgnd(t);	
			outDrawables[t][0] = new BitmapDrawable(wallBmp);

			for (int s = 0; s < mCntStyles - 1; s++) {
				Bitmap wndBmp = makeWindow();				
				Bitmap tileBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);	
				C.setBitmap(tileBmp);
				C.drawBitmap(wallBmp, 0, 0, mPaint);
				C.drawBitmap(wndBmp, (mWidth / 2) - (wndBmp.getWidth() / 2), (mHeight / 2) - (wndBmp.getHeight() / 2), mPaint);
				outDrawables[t][s+1] = new BitmapDrawable(tileBmp);
			}
		}
	}

	public void GenerateTodaysRoofs(BitmapDrawable[][] outDrawables){
		Canvas C = new Canvas();
		for (int t = 0; t < mCntTypes; t++) {	
			Bitmap RoofBmp = makeRoofBckgnd();	
			outDrawables[t][0] = new BitmapDrawable(RoofBmp);

			for (int s = 0; s < mCntStyles - 1; s++) {
				Bitmap RoofWndBmp = makeRoofWindow();
				
				Rect Src = new Rect(0,0,RoofWndBmp.getWidth(),RoofWndBmp.getHeight());
				Rect Dst = new Rect(mWidth/4, mHeight/7, mWidth - (mWidth/4), (int) (mHeight - (mHeight/2.5)));

				Bitmap tileBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);	
			
				C.setBitmap(tileBmp);
				C.drawBitmap(RoofBmp, 0, 0, mPaint);
				C.drawBitmap(RoofWndBmp, Src, Dst, mPaint);
				
				outDrawables[t][s+1] = new BitmapDrawable(tileBmp);
			}
		}
	}

	Bitmap makeWallBckgnd(int type){
		if (type == 0)
			return makeWoodBckgnd();
		if (type == 2)
			return makeTileBckgnd();
		return makeBrickBckgnd();
	}

	Bitmap makeRoofBckgnd(){
		Bitmap roofBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		Canvas C = new Canvas();
		C.setBitmap(roofBmp);
		RoofTexture tex = new RoofTexture(mColorPicker.generateRoofColors(mColorSpline));
		/*mRoofHeight = */tex.Draw(C, roofBmp, mPaint, mWidth, mHeight);
		return roofBmp;
	}
	
	Bitmap makeWoodBckgnd(){
		Canvas C = new Canvas();
		Bitmap woodBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		C.setBitmap(woodBmp);
		double brickDivider = Noise.Vary(3, 5, mRng);
		double brickWidth = mWidth / brickDivider;
		double mortarThickness = brickWidth / (double)Noise.Vary(3, 4, mRng);
		double turbulence = Noise.Vary(3, 15, mRng);
		boolean isHorisontalWoodPlanks = mRng.nextBoolean();
		
		WoodTexture tex = new WoodTexture(brickWidth, mortarThickness, isHorisontalWoodPlanks, mColorPicker.generateWoodColors(mColorSpline));
		tex.SetTurbulence(turbulence, turbulence);

		gColor col;
		mPaint.setColor(Color.rgb(255, 255, 255));
		for (int y = 0; y < mHeight; y++)
			for (int x = 0; x < mWidth; x++){
				col = tex.getColor(x, y);
				mPaint.setColor(Color.rgb(col.GetRb(), col.GetGb(), col.GetBb()));
				C.drawPoint(x, y, mPaint);
			}
		return woodBmp;
	}

	Bitmap makeBrickBckgnd(){
		Canvas C = new Canvas();
		Bitmap wallBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		C.setBitmap(wallBmp);
		double brickDivider = Noise.Vary(3, 5, mRng); //mRng.nextInt(3) + 3;
		double brickWidth = mWidth / brickDivider;
	
		brickDivider = Noise.Vary(3, 6, mRng); //mRng.nextInt(4) + 3;
		double brickHeight = mHeight / brickDivider;
		double mortarThickness = brickHeight / (double)Noise.Vary(3, 4, mRng); //(mRng.nextInt(2) + 3);
		double turbulence =  Noise.Vary(3, 14, mRng); // mRng.nextInt(12) + 3;

		BrickTexture tex = new BrickTexture(brickWidth, brickHeight, mortarThickness, mColorPicker.generateBrickColors(mColorSpline));
		tex.SetTurbulence(turbulence, turbulence);

		gColor col;
		mPaint.setColor(Color.rgb(127, 127, 127));
		for (int y = mHeight; y >= 0; y--)
			for (int x = mWidth; x >= 0; x--){
				col = tex.getColor(x, y);
				mPaint.setColor(Color.rgb(col.GetRb(), col.GetGb(), col.GetBb()));
				C.drawPoint(x, y, mPaint);
			}

		return wallBmp;
	}
	
	Bitmap makeTileBckgnd(){
		Canvas C = new Canvas();
		Bitmap wallBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		C.setBitmap(wallBmp);
		double brickDivider = Noise.Vary(4, 6, mRng); 
		double brickWidth = mWidth / brickDivider;
	
		brickDivider = Noise.Vary(2, 4, mRng); 
		double brickHeight = mHeight / brickDivider;
		double mortarThickness = 1;//brickHeight / (double)Noise.Vary(1, 2, mRng); 
		double turbulence = Noise.Vary(2, 5, mRng); 

		if (brickWidth > brickHeight){
			double temp = brickWidth;
			brickWidth = brickHeight;
			brickHeight = temp;
		}
		
		BrickTexture tex = new BrickTexture(brickWidth, brickHeight, mortarThickness, mColorPicker.generateBrickColors(mColorSpline));
		tex.SetTurbulence(turbulence, turbulence);

		gColor col;
		mPaint.setColor(Color.rgb(127, 127, 127));
		for (int y = mHeight; y >= 0; y--)
			for (int x = mWidth; x >= 0; x--){
				col = tex.getColor(x, y);
				mPaint.setColor(Color.rgb(col.GetRb(), col.GetGb(), col.GetBb()));
				C.drawPoint(x, y, mPaint);
			}

		return wallBmp;
	}

	Bitmap makeWindow(){
		Canvas C = new Canvas();
		mPaint.setAntiAlias(true);
		int numLeafs = mRng.nextInt(5) + 1;
		int numVariants = 4;
		if (numLeafs > 3) numVariants = 2; // for bigger windows

		double wndWidth = (double) mWidth / (1. + ((double)(mRng.nextInt(numVariants) + 3) / 10.)); //1.3-1.7
		double wndHeight = (double) mHeight /(1. + ((double)(mRng.nextInt(numVariants) + 4) / 10.)); //1.4-1.8
		double frameThickness = wndHeight / (double)(mRng.nextInt(4) + 9);
		
		Bitmap wndBmp = Bitmap.createBitmap((int)wndWidth, (int)wndHeight, Bitmap.Config.ARGB_8888);
		C.setBitmap(wndBmp);
		WindowTexture tex = new WindowTexture(frameThickness, wndWidth, wndHeight, mColorSpline);		
		tex.Draw(C, mPaint, mRng, numLeafs, mColorPicker.generateGlassColors(mGlassColorSpline));
		mPaint.setAntiAlias(false);
		return wndBmp;
	}
	
	Bitmap makeRoofWindow(){
		int WndIndex = mRng.nextInt(4);

		Bitmap RoofWndBmp;

		switch (WndIndex){
		case 0: RoofWndBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.roofwnd1); break;
		case 1: RoofWndBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.roofwnd2); break;
		case 2: RoofWndBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.roofwnd3); break;
		default: RoofWndBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.roofwnd4); break;
		}

		return RoofWndBmp;
	}
}
