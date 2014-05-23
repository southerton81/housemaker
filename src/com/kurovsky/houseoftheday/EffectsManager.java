package com.kurovsky.houseoftheday;

import java.util.Random;

import com.kurovsky.houseoftheday.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class EffectsManager{

	public final static EffectsManager INSTANCE = new EffectsManager();

	private final static int    mMaxExplosionFragments = 50;
	private ExplosionFragment[] mExplosionFragments;
	private SmokeFragment[] 	mSmokeFragments;
	private TextFragment[]    	mTextFragments;
	private float               mDensity = 1;
	private int                 mFreeFragmentIndex = 0;
	private int                 mFreeSmokeIndex = 0;
	private int                 mFreeTextIndex = 0;
	private Random              mRandomNumberGenerator = new Random();
	private Bitmap              mFragmentBitmap = null;
	private Bitmap              mSmokeBitmap = null;
	private Paint               mPaint = new Paint();
	private Rect                mFragmentRect = new Rect();
	private Rect                mSmokeRect = new Rect();
	private int                 mVelMin = 3;
	private int                 mVelChange = 1;
	
	private Rect                mDestRect = new Rect();
	private Point               mTileSize = new Point(0, 0);
	private Point               mGridOrigin = new Point();

	public Point GetTileSize(){
		return mTileSize;
	}
	
	private class TextFragment{
		Point  mPos = new Point();
		long   mTimeAccumulator;
		String mText;
		boolean mIsValid = false;
		boolean mIsFloatingUpText = false;

		void InitInGridCoords(int posX, int posY, String text){
			mTimeAccumulator = 0;
			mPos.x = (mGridOrigin.x + (posX * mTileSize.x));
			mPos.y = (mGridOrigin.y + (posY * mTileSize.y));
			mPos.x += (mTileSize.x/2);
			mText = text;
			mIsValid = true;
		}	
		
		/*void InitInScreenCoords(int posX, int posY, String text){
			mTimeAccumulator = 0;
			mPos.x = posX;
			mPos.y = posY;
			mText = text;
			mIsValid = true;
		}*/	
		
		public void Invalidate(){
			mIsValid = false;
			mIsFloatingUpText = false;
		}

		public boolean Update(long deltaMs){
			mTimeAccumulator += deltaMs;
			return (mTimeAccumulator < 1500);
		}

		void Render(Canvas c) {
			if (!mIsValid) return;

			if (mIsFloatingUpText) 
				RenderFloatingUpText(c);
			else 
				RenderText(c);
		}
		
		void RenderText(Canvas c){
			float factor =  ((float) mTimeAccumulator / (float)3000.);
			int Size = (int) ((mTileSize.y * 2) * (1. + factor));
			
			Renderer.INSTANCE.RenderText(c, mText, mPos, Size,
					Color.rgb(253, 196, 45), Color.rgb(90, 0, 90)); 
		}
		
		void RenderFloatingUpText(Canvas c){
			mPos.y -= (mTileSize.y / 6);
			int alpha = (int) (255 - (mTimeAccumulator / 30));
			int Size = (int) (mTileSize.y * 2);
			
			Renderer.INSTANCE.RenderText(c, mText, mPos, (int)Size, 
					Color.argb(alpha, 255, 109, 12), Color.argb(alpha, 77, 77, 77)); 
		}
	}

	public class ExplosionFragment{//!!! DOESNT WORK MAN
		int mPosX;
		int mPosY;
		int mVelX;
		int mVelY;
		long mTimeAccumulator;
		boolean mIsValid = false;

		public void Init(int posX, int posY, int velX, int velY, int colour){
			mPosX = (mGridOrigin.x + (posX * mTileSize.x));
			mPosY = (mGridOrigin.y + (posY * mTileSize.y));
			mPosX += (mTileSize.x/2);
			
			mVelX = velX;
			mVelY = velY;
			mIsValid = true;
			mTimeAccumulator = mRandomNumberGenerator.nextInt(500);
		}

		public void Invalidate(){
			mIsValid = false;
		}

		public boolean Update(long deltaMs){
			mTimeAccumulator += deltaMs;
		//	mPosX += (int) (mVelX);
		//	mPosY += (int) (mVelY);

		//	mVelX = mVelX > 0 ? mVelX + mVelChange : mVelX - mVelChange;
		//	mVelY = mVelY > 0 ? mVelY + mVelChange : mVelY - mVelChange;
			return (mTimeAccumulator < 5500);
		}

		public void Render(Canvas c){
			if (!mIsValid) return;
			mDestRect.set(mPosX, mPosY, mPosX + mFragmentRect.width(), mPosY + mFragmentRect.height());
			//int inset = (int) Math.round(((double)mTimeAccumulator/1000.) * 10.);
			//inset *= mDensity;
			//mDestRect.inset(inset, inset); // Make smaller	
			c.drawBitmap(mFragmentBitmap, mFragmentRect, mDestRect, mPaint);
		}
	}
	
	public class SmokeFragment extends ExplosionFragment{			
		public void Init(int posX, int posY){
			mPosX = (mGridOrigin.x + (posX * mTileSize.x));
			mPosY = (mGridOrigin.y + (posY * mTileSize.y));
			mIsValid = true;
			mTimeAccumulator = mRandomNumberGenerator.nextInt(500);
		}
		
		public void Invalidate(){
			mIsValid = false;
		}
		
		public boolean Update(long deltaMs){
			mTimeAccumulator += deltaMs;
			return (mTimeAccumulator<1000);
		}
		
		public void Render(Canvas c){
			if (!mIsValid) return;
			mDestRect.set(mPosX, mPosY, mPosX + mTileSize.x, mPosY + mTileSize.y);
			mDestRect.offset(0, mTileSize.y/2);

			float factor = ((float) mTimeAccumulator / (float)3000.);
			int insetX = (int) (mTileSize.x * (1. + factor));
			int insetY = (int) (mTileSize.y * (1. + factor));
			
			mDestRect.inset(-insetX/2, -insetY/2);	//Make bigger
			c.drawBitmap(mSmokeBitmap, mSmokeRect, mDestRect, mPaint);
		}
	}

	EffectsManager(){
		mExplosionFragments = new ExplosionFragment[mMaxExplosionFragments];	
		for (int i = 0; i < mMaxExplosionFragments; i++) mExplosionFragments[i] = new ExplosionFragment();
		
		mSmokeFragments = new SmokeFragment[mMaxExplosionFragments];
		for (int i = 0; i < mMaxExplosionFragments; i++) mSmokeFragments[i] = new SmokeFragment();
		
		mTextFragments = new TextFragment[mMaxExplosionFragments];
		for (int i = 0; i < mMaxExplosionFragments; i++) mTextFragments[i] = new TextFragment();
	}

	void SetupImages(Context context, Point tileSize, Rect gridRect){
		mDensity = context.getResources().getDisplayMetrics().density;
		
		mVelMin = (int) Math.round(mDensity * mVelMin);
		mVelChange = (int) Math.round(mDensity * mVelChange);
		
		mTileSize.set(tileSize.x, tileSize.y);
		mGridOrigin.set(gridRect.left, gridRect.top);
		
		mSmokeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smoke);
		mFragmentBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.star);
		
		mSmokeRect.set(0,0, mSmokeBitmap.getWidth(), mSmokeBitmap.getHeight());
		mFragmentRect.set(0,0, mFragmentBitmap.getWidth(), mFragmentBitmap.getHeight());

		for (int i=0; i<mMaxExplosionFragments; i++) mExplosionFragments[i].Invalidate();
		for (int i=0; i<mMaxExplosionFragments; i++) mSmokeFragments[i].Invalidate();
		for (int i=0; i<mMaxExplosionFragments; i++) mTextFragments[i].Invalidate();
	}
	
	void AddExplosionFragment(int posX, int posY){
		int velX = mRandomNumberGenerator.nextInt(5) + mVelMin;
		int velY = mRandomNumberGenerator.nextInt(5) + mVelMin;
		
		if (mRandomNumberGenerator.nextBoolean()) velX = -velX;
		if (mRandomNumberGenerator.nextBoolean()) velY = -velY;
		
		mExplosionFragments[mFreeFragmentIndex].Init(posX, posY, velX, velY, 0);
		mFreeFragmentIndex++;
		if (mFreeFragmentIndex == mMaxExplosionFragments) mFreeFragmentIndex = 0;
	}
	
	void AddSmokeFragment(int posX, int posY){
		mSmokeFragments[mFreeSmokeIndex].Init(posX, posY);
		mFreeSmokeIndex++;
		if (mFreeSmokeIndex == mMaxExplosionFragments) mFreeSmokeIndex = 0;
	}
	
	void AddTextFragment(int posX, int posY, String text){
		mTextFragments[mFreeTextIndex].InitInGridCoords(posX, posY, text);
		mFreeTextIndex++;
		if (mFreeTextIndex == mMaxExplosionFragments) mFreeTextIndex = 0;
	}
	
	void AddFloatingUpTextFragment(int posX, int posY, String text){
		mTextFragments[mFreeTextIndex].InitInGridCoords(posX, posY, text);
		mTextFragments[mFreeTextIndex].mIsFloatingUpText = true;
		mFreeTextIndex++;
		if (mFreeTextIndex == mMaxExplosionFragments) mFreeTextIndex = 0;
	}

	void Update(long deltaMs){
		for (int i=0; i<mMaxExplosionFragments; i++)
			if (!mExplosionFragments[i].Update(deltaMs)) 
				mExplosionFragments[i].Invalidate();
		
		for (int i=0; i<mMaxExplosionFragments; i++)
			if (!mSmokeFragments[i].Update(deltaMs))                  
				mSmokeFragments[i].Invalidate();
		
		for (int i=0; i<mMaxExplosionFragments; i++)
			if (!mTextFragments[i].Update(deltaMs))                  
				mTextFragments[i].Invalidate();
	}

	void Render(Canvas c){
		for (int i=0; i<mMaxExplosionFragments; i++)
			mExplosionFragments[i].Render(c);
		
		for (int i=0; i<mMaxExplosionFragments; i++)
			mSmokeFragments[i].Render(c);
		
		for (int i=0; i<mMaxExplosionFragments; i++)
			mTextFragments[i].Render(c);
	}
}
