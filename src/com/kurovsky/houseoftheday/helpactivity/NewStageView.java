package com.kurovsky.houseoftheday.helpactivity;

import java.util.ArrayList;

import com.kurovsky.houseoftheday.CommonResources;
import com.kurovsky.houseoftheday.Game;
import com.kurovsky.houseoftheday.Renderer;
import com.kurovsky.houseoftheday.R;
import com.kurovsky.houseoftheday.ScoreAccumulator;
import com.kurovsky.houseoftheday.buttons.ButtonManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;

public class NewStageView {
	private Point mPos = new Point(0,0);
	private Rect mSourceRc = new Rect(0,0,0,0);
	private Rect mDestRc = new Rect(0,0,0,0);
	private Paint mPaint = new Paint();
	private long mButtonHeight = 0;
	private ButtonManager mButtonMan;
	private String mPlayString;
	private int mTextHeight = 0;
	private int mTextHeightSmall = 0;
	private int mMinBlocksForHouse;
	private long mPieceCnt = 0;
	private int mLevelNameStrId;
	private Context mContext;
	private String mRules;
	private String mRules2;
	private ArrayList<Integer> mMaterialId = new ArrayList<Integer>();

	public void LoadResources(Context context) {
		mContext = context;
		mPlayString = context.getString(R.string.PlayLabel);
		
		mMaterialId.clear();
		mMaterialId.add(Integer.valueOf(R.string.Wood));
		mMaterialId.add(Integer.valueOf(R.string.Bricks));
		mMaterialId.add(Integer.valueOf(R.string.Tile));
		mMaterialId.add(Integer.valueOf(R.string.Stone));
		
		if (CommonResources.mFont == null)
			 CommonResources.mFont = Typeface.createFromAsset(context.getAssets(),"fonts/" + "FredokaOne-Regular.ttf");
	}
	
	public void SetStage(ScoreAccumulator.StageEnum stage){
		mPieceCnt = stage.PieceCount();
		mLevelNameStrId = stage.StringId();
		mMinBlocksForHouse = (int) stage.MinBlocksForHouse();
		StringBuilder RulesBuilder = new StringBuilder();
		mRules = RulesBuilder.append(mContext.getString(R.string.Rules1)).append(" ").append(mMinBlocksForHouse).toString();
		mRules2 = new String(mContext.getString(R.string.Rules2));
	}
	
    public void SizeChanged(int x1, int y1, int x2, int y2){
        mDestRc.set(x1, y1, x2, y2);
       
        mTextHeight = mDestRc.height() / 9;
        mTextHeightSmall = mDestRc.height() / 15;
    }

    public void Draw(Canvas canvas) {	
    	if (CommonResources.DisplayBitmap != null){
    		mSourceRc.set(0, 0, CommonResources.DisplayBitmap.getWidth(), CommonResources.DisplayBitmap.getHeight());
    		canvas.drawBitmap(CommonResources.DisplayBitmap, mSourceRc, mDestRc, null);
    	}
    	
    	//Draw caption
    	mPaint.reset();
    	mPaint.setAntiAlias(true);
    	mPaint.setTypeface(CommonResources.mFont);
    	mPaint.setTextSize(mTextHeight);
    	mPaint.setTextAlign(Align.CENTER);
    	mPaint.setShadowLayer(1.5f, 0f, 1f, Color.rgb(90, 0, 90));
    	mPaint.setColor(Color.rgb(255, 255, 255));

    	String Str = mContext.getString(mLevelNameStrId);
    	int x = mDestRc.left + (mDestRc.width() / 2);
    	canvas.drawText(Str, x, mDestRc.top + mTextHeight, mPaint);

    	mPos.set(x, (int) ((int) mDestRc.top + (mTextHeight * 2            )));
    	mPaint.setTextSize(mTextHeightSmall);    	
    	canvas.drawText(mRules, mPos.x, mPos.y, mPaint);
    	
    	mPos.y += (mTextHeightSmall);
    	canvas.drawText(mRules2, mPos.x, mPos.y, mPaint);
    	
    	//Draw wall tiles
    	mPos.y += (mTextHeightSmall * 1.7);
    	int yStep = (int)Renderer.INSTANCE.mTileSize.y + mTextHeightSmall;

    	int RowsCnt = (int) Math.ceil((double)mPieceCnt / 2.);
    	int x2 = (int) ((mDestRc.width() / 3) + (mDestRc.width() / 2));
    	int PieceType = 0; 
    	int TileGap = (int) (10 * Renderer.INSTANCE.GetDensity());
    	
    	for (int Row = 0; Row < RowsCnt; Row++){
    		try {
    			Integer StringID = mMaterialId.get(PieceType);
    			canvas.drawText(mContext.getString(StringID.intValue()), mDestRc.left + (mDestRc.width() / 4), mPos.y, mPaint);	

    			if (mPieceCnt > (PieceType+1)){
    				StringID = mMaterialId.get(PieceType+1);
    				canvas.drawText(mContext.getString(StringID.intValue()), x2, mPos.y, mPaint);	
    			}
    		}
    		catch (IndexOutOfBoundsException ex){
    		}

    		mPos.x = (int) ((mDestRc.left + (mDestRc.width() / 4)) - (Renderer.INSTANCE.mTileSize.x / 2));
    		mPos.y += TileGap;
    		Renderer.INSTANCE.RenderTile(canvas, PieceType, 0, mPos, false);

    		if (mPieceCnt > (PieceType+1)){
    			mPos.x = (int) (x2 - (Renderer.INSTANCE.mTileSize.x / 2));
    			Renderer.INSTANCE.RenderTile(canvas, PieceType + 1, 0, mPos, false);
    			PieceType++;
    		}

    		mPos.y += yStep;
    		PieceType++;
    	}
    	
    	//Draw roof tile
    	mPos.x = (int) (x - (Renderer.INSTANCE.mTileSize.x / 2));
    	mPos.y += TileGap;
    	
    	canvas.drawText(mContext.getString(R.string.Roof), x, mPos.y, mPaint);
    	
    	mPos.y += TileGap;
    	Renderer.INSTANCE.RenderTile(canvas, 0, 0, mPos, true);
    	
    	//Set button pos
    	int wBtn = Game.INSTANCE.GetBm().mButtonWidth;
    	int xx1 = x - (wBtn/2);
    	int xx2 = xx1 + wBtn; 
    	
    	mPos.y += Renderer.INSTANCE.mTileSize.y + TileGap;
    	int yy1 = (mDestRc.bottom - TileGap) - Game.INSTANCE.GetBm().mButtonHeight;
    	
    	if (yy1 <= mPos.y)
    		yy1 = mPos.y;
    		
    	int yy2 = yy1 + Game.INSTANCE.GetBm().mButtonHeight;
    	
    	if (yy2 > (mDestRc.bottom - 4))
    		yy2 = (mDestRc.bottom - 4);
    	
    	Game.INSTANCE.GetBm().SetButtonPlayPos(xx1, yy1, xx2, yy2, true); 
    }

}
