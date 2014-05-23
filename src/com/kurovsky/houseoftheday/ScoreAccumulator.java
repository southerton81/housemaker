package com.kurovsky.houseoftheday;

import java.util.Random;

import com.kurovsky.houseoftheday.helpactivity.NewStageView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.PreferenceManager;

public class ScoreAccumulator {
    Grid     mHouseOfTheDay = new Grid(Game.GRID_DIMENSIONX, Game.GRID_DIMENSIONY);
	String	 mString = new String();
	Point	 mPoint = new Point(0,0);
	Point	 mScoreRenderPoint = new Point(0, 0);
	//Grid 	 mBonusHouse = new Grid(BONUS_DIMENSIONX, BONUS_DIMENSIONY);
	//int 	 mBonusHouseType = 0;
	Random   mRng = new Random();
	Rect	 mGridRc = new Rect(0,0,0,0);
	float 	 mDensity;
	int      mScreenWidth;
	Point	 mTempPoint = new Point(0,0);
	Point	 mScaledTileSize = new Point(0,0);
	int 	 mChainReactions = 0;
	long     mHouseOfTheDayAmount = 0;
	long     mLocalRecord = 0;
	Context  mContext;
	boolean m_NewLevelFlag = false;
	NewStageView m_NewLevelView = new NewStageView();
	
	public enum StageEnum {
		VILLAGE(0, 2, 450, 5, R.string.Level1),
		TOWN(20000, 3, 300, 6, R.string.Level2),
		CITY(40000, 4, 250, 6, R.string.Level3);

		private final int  mStringId;
		private final long mStartBalance;
		private final long mPieceCount;
		private final long mGravity;
		private final long mMinBlocksForHouse;
	
		StageEnum(long startBalance, long pieceCount, long gravity, long minBlocks, int strId) {
			this.mStartBalance = startBalance;
			this.mPieceCount = pieceCount;
			this.mGravity = gravity;
			this.mMinBlocksForHouse = minBlocks;
			this.mStringId = strId;
		}
		
		public long StartBalane() { return mStartBalance; }
		public long PieceCount() { return mPieceCount; }
		public long Gravity() { return mGravity; }
		public long MinBlocksForHouse() { return mMinBlocksForHouse; }
		public int  StringId() { return mStringId; }
	}
	
	StageEnum mCurrentStage = StageEnum.VILLAGE;
	
	long mBalance              = 0;
	long mPiecePriceWhenSold   = 200;
	//long mPiecePriceWhenBought = 200;
	
	ScoreAccumulator(){	
		//CreateBonusHouse();
	}
	
	public void Init(Context context, Rect GridRc, int ScreenWidth, int ScreenHeight){
		mScreenWidth = ScreenWidth;
		mScoreRenderPoint.x = ScreenWidth / 2;
		mScoreRenderPoint.y = (int) (GridRc.top / 1.5);
		mGridRc.set(GridRc);
		mDensity = context.getResources().getDisplayMetrics().density;
		mContext = context;
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		mHouseOfTheDayAmount = 0; //sp.getLong("HotDPrice", 0); 	
		mLocalRecord = sp.getLong("LocalRecord", 0); 
		
		m_NewLevelView.LoadResources(context);
		if (!Game.mRestoreGame)
			ShowNewLevelView(mContext);
	}

	public void ShowNewLevelView(Context con){
		m_NewLevelView.SetStage(mCurrentStage);
		m_NewLevelFlag = true;
	}
	
	public NewStageView GetNewLevelView() {
		return m_NewLevelView;
	}
	
	public boolean IsNewLevelFlagSet(){
		boolean Result = m_NewLevelFlag;
		if (m_NewLevelFlag) m_NewLevelFlag = false;
		return Result;
	}
	
	public void Store(SharedPreferences.Editor prefsEditor) {
		Grid HotD = GetHouseOfTheDay();
	
		if (!HotD.IsEmpty()) {
			HotD.Store(prefsEditor, "HotD");
			prefsEditor.putLong("HotDPrice", 0/*mHouseOfTheDayAmount*/); //Refresh house every time we play
			prefsEditor.putLong("HotDSeed", Renderer.INSTANCE.GetLastSeed());
			prefsEditor.putLong("LocalRecord", mLocalRecord); 
		}
	}

	public void Restore(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		mBalance = (sp.getLong("Score", mBalance));
		
		for (StageEnum stage : StageEnum.values())
			if (stage.StartBalane() < mBalance)
				mCurrentStage = stage;
	}
	
	public long GetBalance(){
		return mBalance;
	}

	void AddChainReactionCount(){
		mChainReactions++;
	}
	
	void ClearChainReactionsCount(){
		mChainReactions = 0;
	}
	
	long GetCurrentGravity(){
		return mCurrentStage.Gravity();
	}
	
	/*public void PayForPiece(Grid piece){
		for (int x = 0; x < piece.mWidth; x++)
			for (int y = 0; y < piece.mHeight; y++){
				if (piece.IsTileExists(x, y))
					mBalance -= mPiecePriceWhenBought;
			}
	}*/

	public boolean GetProfitForGroup(GroupsGenerator gGen, GroupsGenerator.HouseGroup group){
		int Bonus = 1;
		
		int Amount = (int) (group.mPrice * mPiecePriceWhenSold);
		
		if (group.mPrice > 7)
			Amount *= 1.2;
		else if (group.mPrice > 8)
			Amount *= 1.4;
		else if (group.mPrice > 9)
			Amount *= 1.5;
		else if (group.mPrice > 12)
			Amount *= 1.6;
		else if (group.mPrice > 14)
			Amount *= 1.7;
		else if (group.mPrice > 16)
			Amount *= 2;
		else if (group.mPrice > 20)
			Amount *= 3;
		
		//if (IsBonusHouse(group)) {
		//	CreateBonusHouse();
		//	Bonus = 3;	
		//}
		
		Bonus += mChainReactions;
		
		if (Amount >= mHouseOfTheDayAmount) {
			mHouseOfTheDayAmount = Amount;
			SetNewHouseOfTheDay(gGen);
		}
		
		Amount *= Bonus;
		mBalance += Amount;
		if (mBalance > mLocalRecord)
			mLocalRecord = mBalance;
		group.GetCenter(mPoint);
		RenderProfit(Amount, Bonus, mPoint);
		return LevelUp();
	}
	
	boolean LevelUp(){
		StageEnum NextStage = null;
		if (mCurrentStage == StageEnum.VILLAGE)
			NextStage = StageEnum.TOWN;
		else if (mCurrentStage == StageEnum.TOWN)
			NextStage = StageEnum.CITY;
		
		if (NextStage == null) return false;
				
		if (NextStage.StartBalane() <= mBalance){
			mCurrentStage = NextStage;
			ShowNewLevelView(mContext);
			return true;
		}
		
		return false;
	}
	
	public void Render(Canvas c, Grid nextPiece, Point tileSize){
		RenderScore(c);
		//RenderBonusHouse(c, tileSize);
		RenderNextPiece(c, nextPiece, tileSize);
		
		/*if (mLastLevel != mCurrLevel){
			//RenderLevelName();
			mLastLevel = mCurrLevel;
		}*/
	}

	private void RenderScore(Canvas c) {
		mString = String.valueOf((long)mBalance);
		mString += "$";
		Renderer.INSTANCE.RenderText(c, mString, mScoreRenderPoint, (int) (mGridRc.top/1.5), 
				Color.rgb(253, 196, 45), Color.rgb(90, 0, 90)); 
	}
	
	private void RenderNextPiece(Canvas c, Grid nextPiece, Point tileSize) {
		float Scale = (float).8;
		mScaledTileSize.set((int)(tileSize.x * Scale), (int)(tileSize.y * Scale));

		int offset = (int) (5 * mDensity);
		float MaxHeight = 3 * mScaledTileSize.y;
		int CenterByY = (int)((MaxHeight - (nextPiece.GetRealHeight() * mScaledTileSize.y)) / 2);
		mTempPoint.set(offset, offset + CenterByY);
		nextPiece.RenderScaled(c, mTempPoint, mScaledTileSize, Scale);
	}

	void RenderProfit(int profit, int bonus, Point centerPt){	
		if (bonus > 1) { 
			mString = "BONUS X " + String.valueOf(bonus);
			int y = (centerPt.y > 1) ? (centerPt.y - 2) : (centerPt.y + 4);
			EffectsManager.INSTANCE.AddFloatingUpTextFragment(centerPt.x, y, mString);
		}

		mString = "$ ";
		mString += String.valueOf((long)profit);
		EffectsManager.INSTANCE.AddTextFragment(centerPt.x, centerPt.y, mString);
	}
	
	void SetNewHouseOfTheDay(GroupsGenerator gGen){
		Grid DissapearingHouse = gGen.GetDissapearingGrid();
		if (DissapearingHouse != null){
			mHouseOfTheDay.CopyFrom(DissapearingHouse);
		}
	}
	
	public Grid GetHouseOfTheDay(){
		return mHouseOfTheDay;	
	}
	
	public StageEnum GetLevel(){
		return mCurrentStage;	
	}
	
	/*
	void RenderLevelName(){
		mString = "Level ";
		mString += (String.valueOf((long)mCurrLevel));
		EffectsManager.INSTANCE.AddTextFragment(Renderer.INSTANCE.GetExternalGridRc().centerX(), Renderer.INSTANCE.GetExternalGridRc().centerY(), mString);
	}
	boolean IsBonusHouse(GroupsGenerator.HouseGroup group){
		if (mBonusHouseType != group.GetLine(0).mType) return false;
		int IndexLastLine = group.GetIndexLastLine();
		if (mBonusHouse.GetRealWidth() != (IndexLastLine + 1)) return false;
		
		for (int w = 0; w <= IndexLastLine; w++){
			int Height = (mBonusHouse.GetRealHeightForColumn(w) - 1);
			if (Height != group.GetLine(w).mHeight) return false;		
		}

		return true;
	}
	private void RenderBonusHouse(Canvas c, Point tileSize) {
	float Scale = (float).8;
	int offset = (int) (5 * mDensity);
	mScaledTileSize.set((int)(tileSize.x * Scale), (int)(tileSize.y * Scale));

	Renderer.INSTANCE.SetAlpha(170);
	mTempPoint.set((int)((mScreenWidth - offset) - (mBonusHouse.GetRealWidth() * mScaledTileSize.x)), offset);
	mBonusHouse.RenderScaled(c, mTempPoint, mScaledTileSize, Scale);		
	Renderer.INSTANCE.SetAlpha(255);
}

void CreateBonusHouse(){
int Price;
do {
	Price = GenerateBonusHouse();
}
while (Price < GroupsGenerator.INSTANCE.GetMinPriceForGroup());	
}

int GenerateBonusHouse() {	
int Price = 0;
mBonusHouse.Clear();

int PossibleWallTypesCount = Game.WALL_TYPES_COUNT;
if (mCurrLevel < 6) PossibleWallTypesCount--;

mBonusHouseType = mRng.nextInt(PossibleWallTypesCount);

int width = Noise.Vary(3, BONUS_DIMENSIONX, mRng);
if (width > BONUS_DIMENSIONX) width = BONUS_DIMENSIONX;
if (mCurrLevel < 5) width = 3;

for (int w = 0; w < width; w++){
	int height = Noise.Vary(2, BONUS_DIMENSIONY, mRng);			
	if (height > BONUS_DIMENSIONY) height = BONUS_DIMENSIONY;

	for (int h = BONUS_DIMENSIONY-1; h > (BONUS_DIMENSIONY-height); h--){
		int WallStyle = mRng.nextInt(Game.WALL_STYLES_COUNT);
		mBonusHouse.SetTile(w, h, mBonusHouseType, WallStyle);
		Price++;
	}
	mBonusHouse.SetTile(w, (BONUS_DIMENSIONY-height), Grid.ROOF_TYPE, 0);
}

if (GroupsGenerator.INSTANCE.GetMinPriceForGroup() > Price) {
	for (int w = 0; w < width; w++){
		for (int h = BONUS_DIMENSIONY-1; h >= 1; h--){
			if (mBonusHouse.GetTile(w, h).IsRoofTile()){
				int WallStyle = mRng.nextInt(Game.WALL_STYLES_COUNT);
				mBonusHouse.SetTile(w, h, mBonusHouseType, WallStyle);
				mBonusHouse.SetTile(w, h - 1, Grid.ROOF_TYPE, 0);
				Price++;
				if (GroupsGenerator.INSTANCE.GetMinPriceForGroup() <= Price)
					return Price;
			}
		}
	}
}

return Price;
}*/	
	
}
