package com.kurovsky.houseoftheday;

import android.graphics.Canvas;
import android.graphics.Point;

public final class GroupsGenerator {
	public final static GroupsGenerator INSTANCE   = new GroupsGenerator();
	public static final int 		    NOLINE_ID  = -1;
	public static final int 		    NOGROUP_ID = -1;
	public static final int 		    DissapearingGridStartAlpha = 255;
	public static final int             DissapearingGridFinishAlpha = 70;

	public class HouseGroup	{
		HouseGroup(int cMaxLines){
			mLineNumbers = new int[cMaxLines];
			SetInvalid();
		}
		void CopyFrom(HouseGroup GroupOther){
			SetLines(GroupOther.mLineNumbers, GroupOther.mMaxY, GroupOther.mPrice);
		}
		void SetLines(int[] LineNumbers, int MaxY, int Price){
			for (int i = 0; i < LineNumbers.length; i++){
				mLineNumbers[i] = LineNumbers[i];
				if (LineNumbers[i] == NOGROUP_ID) {
					mIndexLastLine = i - 1;
					break;
				}
			}
			mPrice = Price;
			mMaxY = MaxY;
		}
		void SetInvalid(){
			mLineNumbers[0] = NOGROUP_ID;
			mIndexLastLine = -1;
		}
		boolean IsValid(){
			return mLineNumbers[0] != NOGROUP_ID;
		}
		boolean IsIntersect(HouseGroup GroupOther){
			for (int i = 0; i <= mIndexLastLine; i++){
				for (int n = 0; n <= GroupOther.mIndexLastLine; n++){
					if (mLineNumbers[i] == GroupOther.mLineNumbers[n])
						return true;
				}
			}
			return false;
		}
		void GetLeftTop(Point pt){
			pt.x = mLines[mLineNumbers[0]].mX;
			pt.y = mLines[mLineNumbers[0]].mY;
		}
		void GetLeftBottom(Point pt){
			pt.x = mLines[mLineNumbers[0]].mX;
			pt.y = mMaxY;
		}
		void GetRightBottom(Point pt){
			pt.x = mLines[mLineNumbers[mIndexLastLine]].mX;
			pt.y = mMaxY;
		}
		void GetRightTop(Point pt){
			pt.x = mLines[mLineNumbers[mIndexLastLine]].mX;
			pt.y = mLines[mLineNumbers[mIndexLastLine]].mY;
		}	
		void GetCenter(Point pt){
			pt.x = mLines[mLineNumbers[0]].mX + 
			(Math.abs(mLines[mLineNumbers[mIndexLastLine]].mX - mLines[mLineNumbers[0]].mX + 1) / 2);
			pt.y = mLines[mLineNumbers[mIndexLastLine]].mY + 
			(Math.abs(mLines[mLineNumbers[mIndexLastLine]].mY2 - mLines[mLineNumbers[mIndexLastLine]].mY + 1) / 2);
		}	
		HouseLine GetLine(int Index){
			if (Index > mIndexLastLine)
				return mLines[mLineNumbers[Index]];
			return mLines[mLineNumbers[Index]];
		}
		int GetIndexLastLine(){
			return mIndexLastLine;
		}
		
		private int[] mLineNumbers;
		public  int   mIndexLastLine;
		private int   mMaxY;
		public  int   mPrice; 
	}

	public class HouseLine {
		public HouseLine(){
			Reset();
		}	
		public void Reset(){
			mType = Grid.EMPTY_TILE;
			mX = -1;
		}	
		public boolean IsEmpty(){
			return (mX == -1);
		}
		
		public int mX;
		public int mY;
		public int mY2;
		public int mHeight;
		public int mType;
	}

	private HouseGroup[] mGroups;
	private HouseLine[]  mLines;
	private int[] 	     mIndexLinesToTheRight;
	private int[]		 mTempPath;
	private Grid 		 mGrid;	
	private Grid 		 mDissapearingGrid = null;
	private int          mDissapearingGridAlpha;
	private int 	     mTempIndex;
	private int 		 mMaxPrice;
	private int 		 mTempPrice;
	private int 		 mEndLineId;
	private int 		 mMinY;
	private int 		 mMaxY;
	private int 		 mStartLineId;
	private int 		 mMaxLines;
	private boolean 	 mLinesExist;
	private int          mTempPathCounter;
	private int          mCurrGroupIndex;
	private int			 mCurrLineType;
	private long 	     mMinBlocksForHouse = 6;
	
	private GroupsGenerator(){
	}
	
	public boolean GenerateGroups(){
		mGrid.ClearHighlighted();
		if (!GenerateLines()) return false;	
		GenerateGroupsFromLines();
		return mGroups[0].IsValid();
	}
	
	public void HighlightGroups(){
		HouseGroup Group;
		HouseLine Line;
		int y;
		
		for (int groupIndex = 0; groupIndex < mMaxLines; groupIndex++){
		    Group = mGroups[groupIndex];
			if (!Group.IsValid()) break;
			
			for (int i = 0; i <= Group.mIndexLastLine; i++){
				Line = Group.GetLine(i);
				y = Line.mY;
				mGrid.SetTileHighlighted(Line.mX, y-1);
				while (y <= Group.mMaxY){
					mGrid.SetTileHighlighted(Line.mX, y);
					y++;
				}
			}
		}
	}
	
	public int RemoveGroupsFromGrid(ScoreAccumulator scoreAccum){
		HouseGroup Group;
		HouseLine Line;
		int y;
		int TilesRemoved = 0;
		
		mDissapearingGrid.Clear();
		mDissapearingGridAlpha = DissapearingGridStartAlpha;
		
		for (int groupIndex = 0; groupIndex < mMaxLines; groupIndex++){
		    Group = mGroups[groupIndex];
			if (!Group.IsValid()) break;
			
			for (int i = 0; i <= Group.mIndexLastLine; i++){
				Line = Group.GetLine(i);
				y = Line.mY;
				
				mDissapearingGrid.SetTile(Line.mX, y-1, mGrid.GetTile(Line.mX, y-1));
				mGrid.ClearTile(Line.mX, y-1); //Roof

				while (y <= Group.mMaxY){
					mDissapearingGrid.SetTile(Line.mX, y, mGrid.GetTile(Line.mX, y));
					mGrid.ClearTile(Line.mX, y);
				
					//EffectsManager.INSTANCE.AddExplosionFragment(Line.mX, y);
					
					y++;
					TilesRemoved++;
				}
			}
			
			boolean IsLevelUp = scoreAccum.GetProfitForGroup(this, Group);
		}
		mGroups[0].SetInvalid(); //Clear all groups
		SetMinPriceForHouse(scoreAccum.GetLevel().MinBlocksForHouse());
		
		return TilesRemoved;
	}
	
	private void GenerateGroupsFromLines(){ 
		mCurrGroupIndex = 0; 
		mGroups[0].SetInvalid();
		for (int i = 0; i < mMaxLines; i++){
			mCurrLineType = mLines[i].mType;
			if (mCurrLineType == Grid.EMPTY_TILE) break;
			mMaxPrice = 0;
			mMinY = mLines[i].mY;
			mMaxY = mLines[i].mY2;
			mStartLineId = i;
			
			CreateGroupStartingFromLine(i);
			if (mGroups[mCurrGroupIndex].IsValid()){ //Group was created
				if (!DeleteCheaperGroup(mCurrGroupIndex)){
					++mCurrGroupIndex;
					mGroups[mCurrGroupIndex].SetInvalid();
				}
			}
		}
	}

	boolean DeleteCheaperGroup(int GroupIndex){
		HouseGroup Group = mGroups[GroupIndex];
		for (int i = GroupIndex - 1; i >= 0; i--){
			if (mGroups[i].IsIntersect(Group)){
				if (Group.mPrice > mGroups[i].mPrice)
					mGroups[i].CopyFrom(Group);
				Group.SetInvalid();
				return true;
			}
		}	
		return false;
	}

	private int CreateGroupStartingFromLine(int LineId){
		if (mMaxPrice == 0){
			mMaxPrice = mLines[LineId].mHeight;
			mTempPrice = mMaxPrice;
			mEndLineId = LineId;
			mTempPathCounter = 0; mTempPath[mTempPathCounter] = LineId; ++mTempPathCounter; mTempPath[mTempPathCounter] = -1;
			if (mMaxPrice >= mMinBlocksForHouse) mGroups[mCurrGroupIndex].SetLines(mTempPath, mMaxY, mMaxPrice);
		}
		else {
			mTempPrice += GetLinePrice(LineId);
			mMinY = mLines[LineId].mY; 
			if (mLines[LineId].mY2 < mMaxY) mMaxY = mLines[LineId].mY2;
			
			mTempPath[mTempPathCounter] = LineId; ++mTempPathCounter; mTempPath[mTempPathCounter] = -1;
			
			if (mTempPrice > mMaxPrice){ 
				mMaxPrice = mTempPrice;
				mEndLineId = LineId;
				if (mMaxPrice >= mMinBlocksForHouse) 
					mGroups[mCurrGroupIndex].SetLines(mTempPath, mMaxY, mMaxPrice);
			}
		}
		
		int cLines = GetLinesToTheRight(LineId);
		if (cLines == 0) return mEndLineId;

	    int LastPathCounter = -1;
		if (cLines > 1) LastPathCounter = mTempPathCounter;

		for (int nLine = 0; nLine < cLines; nLine++){
			if (nLine > 0){
				mMinY = mLines[LineId].mY;
				mMaxY = mLines[LineId].mY2;
				GetLinesToTheRight(LineId); // Restore mIndexLinesToTheRight after overwrite in recursion
			}
			int LastPrice = mTempPrice; 
			CreateGroupStartingFromLine(mIndexLinesToTheRight[nLine]);
			mTempPrice = LastPrice; // Restore mTempPrice after overwrite in recursion
			if (LastPathCounter != -1){ // Restore mTempPath
				mTempPath[LastPathCounter] = -1; 
				mTempPathCounter = LastPathCounter;
			}
		}
		return mEndLineId;
	}

	int GetLinesToTheRight(int LineId){
		int targetX = mLines[LineId].mX+1; 
		int IndexCurr = 0; 
		mIndexLinesToTheRight[0] = GroupsGenerator.NOLINE_ID;
		for (LineId++; LineId < mMaxLines; LineId++){
			if (mLines[LineId].IsEmpty()) break;
			if (mLines[LineId].mX == targetX){
				if ((mLines[LineId].mY > mMaxY) || ((mLines[LineId].mY + mLines[LineId].mHeight) < mMinY)) //Is this line out of scope?
					continue;
				if (mLines[LineId].mType != mCurrLineType)
					continue;
				
				mIndexLinesToTheRight[IndexCurr++] = LineId;

				if (IndexCurr < mMaxLines)
					mIndexLinesToTheRight[IndexCurr] = GroupsGenerator.NOLINE_ID; // Set end of lines array "flag"
				continue;
			}
			if (targetX < mLines[LineId].mX)
				break;
		}
		return IndexCurr;
	}
	
	int GetLinePrice(int LineId){
		if (mLines[LineId].mY2 == mMaxY)
			return mLines[LineId].mHeight;
		if (mLines[LineId].mY2 > mMaxY)
			return mLines[LineId].mHeight - (mLines[LineId].mY2 - mMaxY);
		else { //if (mLines[LineId].mY2 < mMaxY)
			int numLines = mLines[LineId].mX - mLines[mStartLineId].mX;
			int numLost = ((mLines[LineId].mY2 - mMaxY) * numLines);
			return numLost + mLines[LineId].mHeight; 
		}
	}
	
	private boolean GenerateLines(){
		mLinesExist = false;
		for (int i = 0; i < mMaxLines; i++) mLines[i].Reset();
		
		mTempIndex = 0;
		for (int x=0; x<mGrid.mWidth; x++){
			for (int y=0; y<mGrid.mHeight; y++){
				if (!mGrid.mTiles[x][y].IsRoofTile()) continue;

				while (y < (mGrid.mHeight-1)){
					++y;
					if (mGrid.mTiles[x][y].IsRoofTile()){
						--y;
						break;
					}
					else if (mGrid.mTiles[x][y].mType == Grid.EMPTY_TILE)
						break;

					if (mLines[mTempIndex].mType == Grid.EMPTY_TILE){ // Create new group
						mLines[mTempIndex].mType = mGrid.mTiles[x][y].mType;
						mLines[mTempIndex].mX = x;
						mLines[mTempIndex].mY = y;
						mLines[mTempIndex].mY2 = y;
						mLines[mTempIndex].mHeight = 1;
						mLinesExist = true;
					}
					else if (mLines[mTempIndex].mType == mGrid.mTiles[x][y].mType){ // Add to group
						mLines[mTempIndex].mY2++;
						mLines[mTempIndex].mHeight++;
					}
					else break;
				}
				if (mLines[mTempIndex].mType != Grid.EMPTY_TILE) // Group was created so advance to create next group
					++mTempIndex;
			}
		}
		return mLinesExist;
	}

	public void Initialize(Grid grid, long minBlocksForHouse){
		mMinBlocksForHouse = minBlocksForHouse;
		mGrid = grid;
		mDissapearingGrid = new Grid(grid.mWidth, grid.mHeight);
		
		int MaxGroupsInRow = mGrid.mHeight / 2;
		mMaxLines = (MaxGroupsInRow * mGrid.mWidth) + 1;
		mLines = new HouseLine[mMaxLines];
		for (int i = 0; i < mMaxLines; i++)
			mLines[i] = new HouseLine();
		
		mGroups = new HouseGroup[mMaxLines];	
		for (int i = 0; i < mMaxLines; i++)
			mGroups[i] = new HouseGroup(mMaxLines);
		
		mIndexLinesToTheRight = new int[mMaxLines];
		for (int i = 0; i < mMaxLines; i++)
			mIndexLinesToTheRight[i] = GroupsGenerator.NOLINE_ID;

		mTempPath = new int[mGrid.mWidth * 10];
	}
	
	void SetMinPriceForHouse(long minPrice){
		mMinBlocksForHouse = (int) minPrice;
	}
	
	public void RenderDissapearingGroups(Canvas c){	
		if (!mDissapearingGrid.IsEmpty()){
			mDissapearingGrid.RenderDissapearingGrid(c, mDissapearingGridAlpha);
			mDissapearingGridAlpha -= 10;
		}
		if (mDissapearingGridAlpha <= DissapearingGridFinishAlpha) mDissapearingGrid.Clear();	
	}

	public boolean IsDissapearingGridVisible() {
		return mDissapearingGrid.IsEmpty();
	}
	
	public Grid GetDissapearingGrid(){
		return mDissapearingGrid;
	}
}
