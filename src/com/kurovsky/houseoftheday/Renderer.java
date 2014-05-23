//ttt
package com.kurovsky.houseoftheday;

import com.kurovsky.houseoftheday.buttons.Button;
import com.kurovsky.houseoftheday.helpactivity.NewStageView;
import com.kurovsky.houseoftheday.options.OptionsActivity;
import com.kurovsky.houseoftheday.procgraphics.GraphicsGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color; 
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.os.Debug;
import android.util.Log;

public final class Renderer {
	public final static Renderer INSTANCE = new Renderer();
	
	//private boolean            mLowerRoofs = true;
	private Paint              mPaint = new Paint();
	private BlurMaskFilter     mBlurMaskFilter = new BlurMaskFilter(5, Blur.OUTER);
	private float 			   mDensity = 1;
	private BitmapDrawable[][] mWallDrawables = null; 
	private BitmapDrawable[][] mRoofDrawables = null; 
	public Point 			   mTileSize = new Point(-1, -1);
	private int 			   mX;
	private int 		       mY;
	public  Rect               mGridRc = new Rect(0, 0, 0, 0);
	private Rect               mExternalGridRc = new Rect(0, 0, 0, 0);
	private Rect               mBmpRc = new Rect(0, 0, 0, 0);
	private Matrix             mMatrix = new Matrix();	
	private Typeface		   mMainFont = null ;
	private int                mAlpha = 255;
	private Rect               SourceRect = new Rect(0,0,0,0);
	private Rect               SourceRectFrame = new Rect(0,0,0,0);
	private Rect               DestRect = new Rect(0,0,0,0);
	private Rect               SourceRectDisplay = new Rect(0,0,0,0);
	private long			   mLastSeed = 0;
	private Rect 			   mTextBounds = new Rect();
	private int				   mScreenWidth;
	private boolean			   m_AddBlurFlag = false;

	Renderer(){
		mPaint.setAntiAlias(false);
	}
	
	public long GetLastSeed(){
		return mLastSeed;
	}
	
	public void SetAlpha(int alpha){
		mAlpha = alpha;
	}

	public Rect GetExternalGridRc(){
		return mExternalGridRc;
	}
	
	protected void displayMemoryUsage(String message) {
	    int usedKBytes = (int) (Debug.getNativeHeapAllocatedSize() / 1024L);
	    String usedMegsString = String.format("%s usedMemory = Memory Used: %d KB", message, usedKBytes);
	    Log.d("MEM", usedMegsString);
	}
	
	public float GetDensity(){
		return mDensity;
	}

	public Point SetupImages(long seed, Context context, int w, int h, Rect GridRc){	
		GridRc.left = w/10;		
		GridRc.right = w - (w/10);
		GridRc.top = (h/7);
		GridRc.bottom = h - (h/7);
		mScreenWidth = w;
		
		mDensity = context.getResources().getDisplayMetrics().density;
		SetTilesizeAndAdjustGrid(GridRc);
		mGridRc.set(GridRc);
		mExternalGridRc.set(mGridRc);
		mExternalGridRc.inset((int)(-10 * mDensity), (int)(-10 * mDensity));

		if ((mWallDrawables != null) && (mLastSeed == seed))
			return mTileSize;
		
		GenerateTiles(seed, context);
		return mTileSize;
	}
	
	public void LoadBackgrounds(Context context)
	{
		if (mMainFont == null) 
			mMainFont = Typeface.createFromAsset(context.getAssets(),"fonts/" + "FredokaOne-Regular.ttf");

		//BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inPurgeable = true;
		//options.inInputShareable = true;

		if (CommonResources.BackgroundBitmap == null)
			CommonResources.BackgroundBitmap = BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.fon);

		if (CommonResources.FrameBitmap == null)
			CommonResources.FrameBitmap = BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.frame);

		if (CommonResources.DisplayBitmap == null)
			CommonResources.DisplayBitmap = BitmapFactory.decodeResource(context.getResources(), com.kurovsky.houseoftheday.R.drawable.display);

		SourceRectDisplay.set(0,0,CommonResources.DisplayBitmap.getWidth(),CommonResources.DisplayBitmap.getHeight());
		SourceRectFrame.set(0,0,CommonResources.FrameBitmap.getWidth(),CommonResources.FrameBitmap.getHeight());
		SourceRect.set(0,0,CommonResources.BackgroundBitmap.getWidth(),CommonResources.BackgroundBitmap.getHeight());
	}

	void GenerateTiles(long seed, Context context) {
		if (mWallDrawables != null)
			for (int x = 0; x < Game.WALL_TYPES_COUNT; x++)
				for (int y = 0; y < Game.WALL_STYLES_COUNT; y++)
					mWallDrawables[x][y].getBitmap().recycle();
	
		if (mRoofDrawables != null)
			for (int x = 0; x < 1; x++)
				for (int y = 0; y < Game.ROOF_STYLES_COUNT; y++)
					mRoofDrawables[x][y].getBitmap().recycle();
		
		GraphicsGenerator graphicsGenerator = new GraphicsGenerator(seed, context);
		graphicsGenerator.InitTileSize(mTileSize.x, mTileSize.y, mDensity);

		graphicsGenerator.InitStylesAndTypesCount(Game.WALL_TYPES_COUNT, Game.WALL_STYLES_COUNT);
		mWallDrawables = new BitmapDrawable[Game.WALL_TYPES_COUNT][Game.WALL_STYLES_COUNT];
		graphicsGenerator.GenerateTodaysWalls(mWallDrawables);

		graphicsGenerator.InitStylesAndTypesCount(1, Game.ROOF_STYLES_COUNT);
		mRoofDrawables = new BitmapDrawable[1][Game.ROOF_STYLES_COUNT];
		graphicsGenerator.GenerateTodaysRoofs(mRoofDrawables);
		
		mLastSeed = seed;
	}

	private void SetTilesizeAndAdjustGrid(Rect gridRc) {
	    int TileSizeX = (int) Math.floor(gridRc.width() / Game.GRID_DIMENSIONX);
	    int TileSizeY = (int) Math.floor(gridRc.height() / Game.GRID_DIMENSIONY);
	    mTileSize.set(TileSizeX, TileSizeY);
	    
	    int newGridWidth = TileSizeX * Game.GRID_DIMENSIONX;
	    int newGridHeight = TileSizeY * Game.GRID_DIMENSIONY;
	    
	    if (gridRc.width() != newGridWidth)
	    	gridRc.left += (gridRc.width() - newGridWidth) / 2;
	    if (gridRc.height() != newGridHeight)
	    	gridRc.bottom = (gridRc.top + newGridHeight);
	}

	public void DrawBackground(Canvas canvas){
		if (mWallDrawables == null || canvas == null) return;
		if (CommonResources.BackgroundBitmap != null){
			if (DestRect == null) return;
			DestRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
			canvas.drawBitmap(CommonResources.BackgroundBitmap, SourceRect, DestRect, null);	
		}
		if (CommonResources.FrameBitmap != null)
			canvas.drawBitmap(CommonResources.FrameBitmap, SourceRectFrame, mExternalGridRc, null);			

		if (CommonResources.DisplayBitmap != null)
			canvas.drawBitmap(CommonResources.DisplayBitmap, SourceRectDisplay, mGridRc, null);

		RenderGridLines(canvas);
	}

	public void RenderGridLines(Canvas c){
		if (mTileSize.x == -1) return;
		if (!OptionsActivity.mGridLinesVisible) return;
		mPaint.reset();
		mPaint.setColor(Color.WHITE);
		int StartAlpha = 127;
		int StepAlpha = 4;
		mPaint.setAlpha(StartAlpha);
		int Gap = (int) (3 * mDensity);
		
		for (int x = mGridRc.left + mTileSize.x; x < (mGridRc.right - mTileSize.x) ; x += mTileSize.x){
			for (int t = mGridRc.top; t < mGridRc.bottom; t+= mTileSize.y)
				c.drawLine(x, t + Gap, x, t + mTileSize.y - Gap, mPaint);
			
			StartAlpha -= StepAlpha;
			mPaint.setAlpha(StartAlpha);
		}
		
		StartAlpha = 127;
		mPaint.setAlpha(StartAlpha);
		
		for (int y = mGridRc.top + mTileSize.y; y < mGridRc.bottom; y += mTileSize.y){
			for (int t = mGridRc.left; t < (mGridRc.right - mTileSize.x); t+= mTileSize.x)	
				c.drawLine(t + Gap, y, t + mTileSize.x - Gap, y, mPaint);
			
			StartAlpha -= StepAlpha;
			mPaint.setAlpha(StartAlpha);
		}
		mPaint.setAlpha(255);
	}

	public void RenderTile(Canvas c, int type, int style, Point pos, boolean isRoof){
		if (mTileSize.x == -1) return;
		if ((mWallDrawables == null) || (mRoofDrawables == null))  return;
		if (pos.y<0) return;
		
		Bitmap bmp;
		if (isRoof)
			bmp = mRoofDrawables[type][style].getBitmap();
		else 
			bmp = mWallDrawables[type][style].getBitmap();

		c.save(Canvas.MATRIX_SAVE_FLAG);
		mMatrix.reset();
		c.concat(mMatrix);
		mMatrix.postTranslate(pos.x, pos.y);
		mPaint.reset();
		c.drawBitmap(bmp, mMatrix, mPaint);
		c.restore();
		mPaint.setAlpha(255);
		mMatrix.reset();
	}
	
	public void RenderTile(Canvas c, Grid.Tile tile, Point pos){
		if (mTileSize.x == -1) return;
		if ((mWallDrawables == null) || (mRoofDrawables == null))  return;
		
		if (pos.y<0) return;

		mX = (pos.x * mTileSize.x) + mGridRc.left;
		mY = (pos.y * mTileSize.y) + mGridRc.top;
		Bitmap bmp;
		if (tile.IsRoofTile()){
			bmp = mRoofDrawables[0][tile.mStyle].getBitmap();
			/*if (mLowerRoofs)*/ mY += mTileSize.y / 4;
		}
		else bmp = mWallDrawables[tile.mType][tile.mStyle].getBitmap();

		c.save(Canvas.MATRIX_SAVE_FLAG);
		mMatrix.reset();
		c.concat(mMatrix);

		if ((pos.x % 2) != 0) { // mirror odd tiles for better looking connection
			mMatrix.setScale(-1, 1);
			mMatrix.postTranslate(bmp.getWidth() + mX, mY);
		}
		else mMatrix.postTranslate(mX, mY);
		
		mPaint.reset();
		if (tile.mIsHiglighted)
			mPaint.setAlpha(127);
		else 
			mPaint.setAlpha(mAlpha);
		
		if (m_AddBlurFlag){
			mPaint.setMaskFilter(mBlurMaskFilter);
			c.drawBitmap(bmp, mMatrix, mPaint);
			mPaint.setMaskFilter(null);
		}
		
		c.drawBitmap(bmp, mMatrix, mPaint);
		
		c.restore();
		mPaint.setAlpha(255);
	}
	
	public void RenderTileScaled(Canvas c, Grid.Tile tile, Point pos, float scale){
		if (mTileSize.x == -1) return;
		if ((mWallDrawables == null) || (mRoofDrawables == null))  return;

		mX = pos.x;
		mY = pos.y;
		
		Bitmap bmp;
		if (tile.IsRoofTile()){
			bmp = mRoofDrawables[0][tile.mStyle].getBitmap();
			mY += (mTileSize.y * scale) / 4;
		}
		else 
			bmp = mWallDrawables[tile.mType][tile.mStyle].getBitmap();

		c.save(Canvas.MATRIX_SAVE_FLAG);
		mMatrix.reset();
		c.concat(mMatrix);

		if ((pos.x % 2) != 0){ // mirror odd tiles for better looking connection
			mMatrix.setScale(-scale, scale);
			mMatrix.postTranslate((bmp.getWidth()*scale) + mX, mY);
		}
		else {
			mMatrix.setScale(scale, scale);
			mMatrix.postTranslate(mX, mY);
		}
		
		c.drawBitmap(bmp, mMatrix, null);
		c.restore();
	}
	
	public void RenderButton(Canvas c, Button b, Rect bRc, Bitmap Bmp, Bitmap LogoBmp){
		if (Bmp == null) return;
		mPaint.reset();
		mPaint.setFilterBitmap(true);

		mBmpRc.set(0,0,Bmp.getWidth(),Bmp.getHeight());   
		c.drawBitmap(Bmp, mBmpRc, bRc, mPaint);
		
		if (LogoBmp != null) RenderButtonLogo(c, LogoBmp, mBmpRc, bRc);	
	}	
	
	private void RenderButtonLogo(Canvas c, Bitmap logo, Rect BmpRc, Rect bRc){
		mBmpRc.set(0,0,logo.getWidth(),logo.getHeight());
		
		int Multiplier = (int) (10 * mDensity);
		
		if (((mBmpRc.width() + Multiplier) >= bRc.width()) || ((mBmpRc.height() + Multiplier) >= bRc.height())) {
			int X = bRc.centerX() - (mBmpRc.width() / 4);
			int Y = bRc.centerY() - (mBmpRc.height() / 4);
			bRc.set(X, Y, X + (mBmpRc.width() / 2), Y + (mBmpRc.height() / 2));
		}
		else {
			int X = bRc.centerX() - (mBmpRc.width() / 2);
			int Y = bRc.centerY() - (mBmpRc.height() / 2);
			bRc.set(X, Y, X + mBmpRc.width(), Y + mBmpRc.height());
		}
		c.drawBitmap(logo, mBmpRc, bRc, mPaint);
	}
	
	public void RenderText(Canvas c, String string, Point pt, int height, int colourtext, int colourshadow){
		if (mMainFont == null) return;
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(mMainFont);
		mPaint.setTextSize(height);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setShadowLayer(1f, 0f, 1f, colourshadow); 
		mPaint.setColor(colourtext);
		//c.drawText(string, pt.x, pt.y, mPaint);
		
		//Multiline text in screen bounds
		String[] lines = string.split("\n");
	
		int xoff = pt.x;
		int yoff = 0;
		for (int i = 0; i < lines.length; ++i) {
			xoff = pt.x;
			mPaint.getTextBounds(lines[i], 0, lines[i].length(), mTextBounds);

			int MinX = xoff - (mTextBounds.width() / 2);
			int MaxX = xoff + (mTextBounds.width() / 2);
					
			if (MinX <= 0)
				xoff += (Math.abs(MinX) + 10);
			else if  (MaxX >= mScreenWidth)
				xoff -= ((MaxX - mScreenWidth) + 10);

			c.drawText(lines[i], xoff, pt.y + yoff, mPaint);
			yoff += (mTextBounds.height() * 1.5);
		}
	}
	
	public void RenderCurveText(Canvas c, String string, RectF oval, int height, int colourtext, int colourshadow){
		if (mMainFont == null) return;
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(mMainFont);
		mPaint.setTextSize(height);
		mPaint.setTextAlign(Align.CENTER);
		//mPaint.setShadowLayer(1f, 0f, 1f, colourshadow); 
		mPaint.setColor(colourtext);

		Path mArc = new Path();
		mArc.addArc(oval, -180, 180); 

		//c.drawArc(oval, -180, 180, false, mPaint);
		c.drawTextOnPath("BONUS", mArc, 0, 20, mPaint); 
	}

	public void RenderNewLevelView(Canvas c, NewStageView newLevelView) {
		if (newLevelView == null) return;
		newLevelView.SizeChanged(mGridRc.left, mGridRc.top, mGridRc.right, mGridRc.bottom);		
		newLevelView.Draw(c);
	}

	public void AddBlur(boolean b) {
		m_AddBlurFlag = b;
	}
	
}

