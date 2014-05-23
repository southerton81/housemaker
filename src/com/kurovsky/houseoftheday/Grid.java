package com.kurovsky.houseoftheday;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.kurovsky.houseoftheday.GroupsGenerator.HouseGroup;
import com.kurovsky.houseoftheday.options.OptionsActivity;
import com.kurovsky.houseoftheday.soundmanager.SoundManager;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

public class Grid {
	public class Tile {
		public Tile(int type){
			Clear();
			SetType(type);
		}
		public int  GetType()        { return mType; }
		public void SetType(int type){ mType = type; }
		public void Clear(){
			SetType(EMPTY_TILE);
			mIsHiglighted = false;
			mFigureUid = -1;
		}
		private void SetStyle(int style){ mStyle = style; }
		private void SetFigureUid(int FigureUid){ mFigureUid = FigureUid; }
		private void CopyFrom(Tile tile){
			mType = tile.mType;
			mStyle = tile.mStyle;
			mFigureUid = tile.mFigureUid;
		}
		public boolean IsRoofTile(){ return (mType == Grid.ROOF_TYPE); }
		public void SetHighlighted(boolean highlighted){ mIsHiglighted = highlighted; }	
		public int GetFigureUid() { return mFigureUid; }

		public int mType;
		public int mStyle;
		public int mFigureUid;
		public boolean mIsHiglighted;
	}

	public  static final int EMPTY_TILE = -1;
	public  static final int WOOD_TYPE  = 0;
	public  static final int ROOF_TYPE  = 100;
	
	private static int[] mSupportedFigures = null;
	
	public  Tile[][] mTiles; 
	private Tile 	 mTmpTile = new Tile(0);
	public  int 	 mWidth;
	public  int 	 mHeight;
	private int 	 mTmpX;
	private int	  	 mTmpY;
	private int 	 mNumRotations = 0;
	private int		 mCurrRotation = 0;
	private Point	 mShadowPos = new Point();
	private Point	 mShiftedPos = new Point();
	private Point    mEmptyPos = new Point(0,0);
	private Point    mTempPt = new Point(0,0);
	private Rect     mTempRect = new Rect(0,0,0,0);
	
	public Grid(int width, int height){
		mWidth = width;
		mHeight = height;
		mTiles = new Tile[width][height];
		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++)
				mTiles[x][y] = new Tile(EMPTY_TILE);
	}
	
	public void Store(SharedPreferences.Editor sp, String gridName){
		StringBuilder str = new StringBuilder();

		sp.putString(gridName + "gridWidth", String.valueOf(mWidth));
		sp.putString(gridName + "gridHeight", String.valueOf(mHeight));

		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++){
				str.append(mTiles[x][y].mType).append(",");
			}
		sp.putString(gridName+ "tileType", str.toString());
		str.delete(0, str.length());

		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++){
				str.append(mTiles[x][y].mStyle).append(",");
			}
		sp.putString(gridName + "tileStyle", str.toString());
		str.delete(0, str.length());

		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++){
				str.append(mTiles[x][y].GetFigureUid()).append(",");
			}
		sp.putString(gridName + "tileFigureUid", str.toString());
		str.delete(0, str.length());	
	}

	public void Restore(SharedPreferences sp, String gridName){
		try {
			String savedString;

			savedString = sp.getString(gridName + "gridWidth", "2");
			mWidth = Integer.parseInt(savedString);

			savedString = sp.getString(gridName + "gridHeight", "2");
			mHeight = Integer.parseInt(savedString);

			savedString = sp.getString(gridName+ "tileType", "");
			StringTokenizer st = new StringTokenizer(savedString, ",");

			for (int x = 0; x < mWidth; x++)
				for (int y = 0; y < mHeight; y++)
					mTiles[x][y].SetType(Integer.parseInt(st.nextToken()));

			savedString = sp.getString(gridName + "tileStyle", "");
			st = new StringTokenizer(savedString, ",");

			for (int x = 0; x < mWidth; x++)
				for (int y = 0; y < mHeight; y++)
					mTiles[x][y].SetStyle(Integer.parseInt(st.nextToken()));

			savedString = sp.getString(gridName + "tileFigureUid", "");
			st = new StringTokenizer(savedString, ",");

			for (int x = 0;  x < mWidth; x++)
				for (int y = 0; y < mHeight; y++)
					mTiles[x][y].SetFigureUid(Integer.parseInt(st.nextToken()));

		}
		catch (NoSuchElementException ex){
			Log.v("Grid.Restore()", gridName + " nexToken failed");
		}
	}
	
	public int GetWidth(){
		return mWidth;
	}
	
	public int GetHeight(){
		return mHeight;
	}
	
	public void SetSize(int w, int h){
		mWidth = w;
		mHeight = h;
	}

	public void Clear(){
		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++)
				mTiles[x][y].Clear();
	}
	
	public void ClearHighlighted(){
		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++)
				mTiles[x][y].SetHighlighted(false);
	}
	
	public void ClearTile(int x, int y){
		if (!IsValid(x,y)) return;	
	    mTiles[x][y].Clear();
	}
	
	public void SetNumRotations(int numRotaions){
		mCurrRotation = 0;
		mNumRotations = numRotaions;
		Rotate(0);
	}

	public boolean IsCollidingWith(Grid g, int myX, int myY){
		//if (myY < 0) myY = (myY + 1);
		for (int x = 0;  x < mWidth; x++){
			for (int y = 0; y < mHeight; y++){
				if (IsTileExists(x,y)){
					mTmpX = x + myX;
					mTmpY = y + myY;

					if (!g.IsValid(mTmpX, 0)) //if y<0 we can continue but x can be well out of grid
						return true;

					if (mTmpY < 0) continue;

					if (!g.IsValid(mTmpX, mTmpY) || g.IsTileExists(mTmpX, mTmpY))
						return true;
				}
			}
		}
		return false;
	}

	// Copy any non-empty tiles from this grid into the specified target (with offset)
	public void AddToGrid(Grid g, int myX, int myY){ 
		for (int x=0; x<mWidth; x++)
			for (int y=0; y<mHeight; y++){
				if (IsTileExists(x,y) && g.IsValid(x+myX, y+myY))
					g.SetTile(x+myX, y+myY, GetTile(x,y));
			}
	}
	

	public int AddToGridBottom(Grid g, int startX){
		GetMbr(mTempRect);
		
		for (int x=mTempRect.left; x<mTempRect.right; x++){
			int StartY = g.GetHeight() - mTempRect.height();
		
			for (int y=mTempRect.top; y<mTempRect.bottom; y++){	
				if (IsTileExists(x,y) && g.IsValid(startX, StartY))
					g.SetTile(startX, StartY, GetTile(x,y));
			
				StartY++;
			}

			startX++;
		}
		
		return startX;
	}

	public Tile GetTile(int x, int y){
		return mTiles[x][y];
	}

	public boolean IsValid(int x, int y){
		return (x>=0 && y>=0 && x<mWidth && y<mHeight);
	}

	public boolean IsTileExists(int x, int y){
		if (!IsValid(x, y)) return false;		
		return (mTiles[x][y].mType != Grid.EMPTY_TILE);
	}

	public void SetTile(int x, int y, Tile tile){
		if (!IsValid(x, y)) return;		
		mTiles[x][y].CopyFrom(tile);
	}
	
	public void SetTileHighlighted(int x, int y){
		if (!IsValid(x, y)) return;		
		mTiles[x][y].SetHighlighted(true);
	}

	public void SetTile(int x, int y, int type, int style){
		if ((x >= mWidth) || (y >= mHeight)) return;
		mTiles[x][y].SetType(type);
		mTiles[x][y].SetStyle(style);
	}

	public void Rotate(int rotation){	
		if (mNumRotations <= 1 || rotation == 0) return;

		rotation = (mCurrRotation + 4 + rotation) % mNumRotations - mCurrRotation;
		rotation = rotation & 3;
		mCurrRotation = (mCurrRotation + rotation) % mNumRotations;

		while (rotation != 0){
			rotation -= 1;
			// Rotate the arrangement of tiles through 90 degrees
			for (int x=0; x<mWidth/2; x++){
				for (int y=0; y<(mHeight+1)/2; y++){
					mTmpTile.CopyFrom(GetTile(x,y));

					SetTile(x, y, GetTile(y,mWidth-1-x));
					SetTile(y, mWidth-1-x, GetTile(mWidth-1-x,mWidth-1-y));
					SetTile(mWidth-1-x,mWidth-1-y, GetTile(mWidth-1-y,x));	               
					SetTile(mWidth-1-y,x, mTmpTile);
				}
			}
		}
	}

	public void SetFigureUid(int uid){
		for (int x=0; x<mWidth; x++) 
			for (int y=0; y<mHeight; y++){
				if (mTiles[x][y ].mType != Grid.EMPTY_TILE)
					mTiles[x][y].SetFigureUid(uid);	
			}
	}

	boolean IsTileConnectedToFigure(int x, int y){
		int MyFigureUid = mTiles[x][y].GetFigureUid();

		if (IsValid(x + 1, y) && mTiles[x+1][y].GetFigureUid() == MyFigureUid)  return true;
		if (IsValid(x - 1, y) && mTiles[x-1][y].GetFigureUid() == MyFigureUid)  return true;
		if (IsValid(x, y + 1) && mTiles[x][y+1].GetFigureUid() == MyFigureUid)  return true;
		if (IsValid(x, y - 1) && mTiles[x][y-1].GetFigureUid() == MyFigureUid)  return true;

		if (IsValid(x+1, y+1) && mTiles[x+1][y+1].GetFigureUid() == MyFigureUid) return true;
		if (IsValid(x-1, y-1) && mTiles[x-1][y-1].GetFigureUid() == MyFigureUid) return true;
		if (IsValid(x+1, y-1) && mTiles[x+1][y-1].GetFigureUid() == MyFigureUid) return true;
		if (IsValid(x-1, y+1) && mTiles[x-1][y+1].GetFigureUid() == MyFigureUid) return true;
		return false;
	}

	boolean IsSameFigureUidExists(int x, int y){
		for (int xx = 0;  xx < mWidth; xx++)
			for (int yy = 0; yy < mHeight; yy++){
				if ((xx == x) && (yy == y)) continue;
				if (mTiles[xx][yy].GetFigureUid() == mTiles[x][y].GetFigureUid())	
					return true;
			}
		return false;
	}
	
	boolean IsFigureSupported(int x, int y, Game game){
		if (mTiles[x][y].mType == EMPTY_TILE) return true;
		
		/*Supported mFigureUid @ x, y*/
		if (Arrays.binarySearch(mSupportedFigures, mTiles[x][y].mFigureUid) >= 0){

			/*Figure broken? Check nearby tiles for the same FigureUid*/
			if (IsTileConnectedToFigure(x, y)) return true;
			
			//If MyFigureUid isn't just one left in a grid then change its FigureUid to break figure
			if (IsSameFigureUidExists(x, y)){
				mTiles[x][y].SetFigureUid(game.GetNextUid());
				return false;
			}
			
			return true;
		}
		
		return false;
	}

	boolean IsSupportedFromBelow(int x, int y){
		if (y == (mHeight-1)) return true;
		return  ((mTiles[x][y+1].mType != EMPTY_TILE) && /*Supported mFigureUid @ x, y + 1*/
				(Arrays.binarySearch(mSupportedFigures, mTiles[x][y + 1].mFigureUid) >= 0)) ;
	}
	
	public boolean MakeFall(Game game){   
		boolean falling = false;

		if (mSupportedFigures == null)
			mSupportedFigures = new int[(Game.GRID_DIMENSIONX * Game.GRID_DIMENSIONY * 2) + 1];
		Arrays.fill(mSupportedFigures, -1);

		while (true){
			boolean changed = false;

			// Iterate from the bottom towards the top, finding unsupported groups which will need to fall
			for (int y=mHeight-1; y>=0; y--){
				for (int x=0; x<mWidth; x++){
					// If the this tile's group isn't supported, check if it should be.
					// (Either because it's at the bottom of the playing area, or it's resting on another supported group)

					if (!IsFigureSupported(x, y, game) && IsSupportedFromBelow(x,y)){

						/* Add to supported FigureIds */
					
						/* Just in case */
						int DublicateIndex = Arrays.binarySearch(mSupportedFigures, mTiles[x][y].GetFigureUid());
						if (DublicateIndex >= 0){
							mSupportedFigures[DublicateIndex] = -1; 
							Arrays.sort(mSupportedFigures);
						}

						int FreeSlotIndex = Arrays.binarySearch(mSupportedFigures, -1);
						if (FreeSlotIndex < 0) continue;
						
						if (!IsTileConnectedToFigure(x, y)) 
							if (IsSameFigureUidExists(x, y))
								mTiles[x][y].SetFigureUid(game.GetNextUid());
							
						mSupportedFigures[FreeSlotIndex] = mTiles[x][y].GetFigureUid();
						
						Arrays.sort(mSupportedFigures);
						changed = true;
					}
				}
			}

			// Keep re-scanning the tiles until we don't find any more supported ones
			if (!changed) break;
		}

		for (int y=mHeight-1; y>=0; y--)
			for (int x=0; x<mWidth; x++) 	
			{
				if ((mTiles[x][y].mType != EMPTY_TILE) && /*!Supported mFigureUid @ x, y*/
						(Arrays.binarySearch(mSupportedFigures, mTiles[x][y].mFigureUid) < 0)){	
					SetTile(x, y+1, mTiles[x][y]);
					ClearTile(x, y);		
					falling = true;
				}
			}

		return falling;
	}
	
	void GetMbr(Rect rc){
		rc.set(0,0,0,0);
		for (int x = 0; x < mWidth; x++)
			for (int y = 0; y < mHeight; y++){
				if (mTiles[x][y].mType != Grid.EMPTY_TILE)
					rc.union(x, y, x+1, y+1);	
			}
	}
	
	boolean IsEmpty(){
		for (int x = 0; x < mWidth; x++)
			for (int y = 0; y < mHeight; y++){
				if (mTiles[x][y].mType != Grid.EMPTY_TILE)
					return false;
			}
		return true;
	}

	public boolean RemoveRoofsFromGround() {
		boolean IsRoofCrushed = false;
		int Ground = mHeight - 1;
		for (int x = 0; x < mWidth; x++){
			if (mTiles[x][Ground].IsRoofTile()){
				ClearTile(x, Ground);
				EffectsManager.INSTANCE.AddSmokeFragment(x, Ground);
				IsRoofCrushed = true;
			}
		}
		return IsRoofCrushed;
	}

	public int GetRealHeight() {
		int Height = 0;
		int RetHeight = 777;
		
		for (int x = 0;  x < mWidth; x++){
			Height = GetRealHeightForColumn(x);
			
			if (RetHeight == 777)
				RetHeight = Height;
			else if (RetHeight < Height)
				RetHeight = Height;
		}
	
		return RetHeight;
	}
	
	public int GetRealWidth() {
		int Width = 0;
		int RetWidth = 777;
		
		for (int y = 0; y < mHeight; y++){
			Width = GetRealWidthForRow(y);
			
			if (RetWidth == 777)
				RetWidth = Width;
			else if (RetWidth < Width)
				RetWidth = Width;
		}
		
		return RetWidth;
	}
	
	public int GetRealHeightForColumn(int column) {
		int Height = 0;
		for (int y = 0; y < mHeight; y++) {
			if (IsTileExists(column, y)) Height++;
		}
		return Height;
	}
	
	public int GetRealWidthForRow(int row) {
		int Width = 0;
		for (int x = 0; x < mWidth; x++) {
			if (IsTileExists(x, row)) Width++;
		}
		return Width;
	}
	
	//Rendering routines
	public void RenderMainGrid(Canvas c){
		//Renderer.INSTANCE.mLowerRoofs = true;
		Render(c, mEmptyPos); 
	}
	
	public void RenderDissapearingGrid(Canvas c, int alpha){
		Renderer.INSTANCE.SetAlpha(alpha);
		//Renderer.INSTANCE.mLowerRoofs = true;
		
		mTempPt.set(Renderer.INSTANCE.mGridRc.left, Renderer.INSTANCE.mGridRc.top);
		RenderScaled(c, mTempPt, Renderer.INSTANCE.mTileSize, (float)1.1);
		
		//Render(c, mEmptyPos);
		//Renderer.INSTANCE.mLowerRoofs = false;
		Renderer.INSTANCE.SetAlpha(255);
	}
	
	public void Render(Canvas c, Point pos){
		for (int x=0; x<mWidth; x++)
			for (int y=0; y<mHeight; y++){
				if (!mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = x + pos.x;
					mShiftedPos.y = y + pos.y;
					Renderer.INSTANCE.RenderTile(c, mTiles[x][y], mShiftedPos);
				}
			}

		for (int x=0; x<mWidth; x++) //Draw roofs
			for (int y=0; y<mHeight; y++){
				if (mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = x + pos.x;
					mShiftedPos.y = y + pos.y;
					Renderer.INSTANCE.RenderTile(c, mTiles[x][y], mShiftedPos);
				}  
			}
	}

	public void RenderScaled(Canvas c, Point pos, Point tileSize, float scale){
		for (int x=0; x<mWidth; x++)
			for (int y=0; y<mHeight; y++){
				if (!mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = (int) ((x * tileSize.x) + pos.x);
					mShiftedPos.y = (int) ((y * tileSize.y) + pos.y);
					Renderer.INSTANCE.RenderTileScaled(c, mTiles[x][y], mShiftedPos, scale);
				}
			}

		for (int x=0; x<mWidth; x++) //Draw roofs
			for (int y=0; y<mHeight; y++){
				if (mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = (int) ((x * tileSize.x) + pos.x);
					mShiftedPos.y = (int) ((y * tileSize.y) + pos.y);
					Renderer.INSTANCE.RenderTileScaled(c, mTiles[x][y], mShiftedPos, scale);
				}
			}
	}
	
	public void RenderScaledByMbr(Renderer r, Canvas c, Point pos, Point tileSize, float scale){
		GetMbr(mTempRect);
		int StartX = mTempRect.left;
		int StartY = mTempRect.top;
		
		for (int x=0; x<mWidth; x++)
			for (int y=0; y<mHeight; y++){
				if (!mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = (int) (((x - StartX) * tileSize.x) + pos.x);
					mShiftedPos.y = (int) (((y - StartY) * tileSize.y) + pos.y);
					r.RenderTileScaled(c, mTiles[x][y], mShiftedPos, scale);
				}
			}

		for (int x=0; x<mWidth; x++) //Draw roofs
			for (int y=0; y<mHeight; y++){
				if (mTiles[x][y].IsRoofTile() && mTiles[x][y].mType != Grid.EMPTY_TILE){
					mShiftedPos.x = (int) (((x - StartX) * tileSize.x) + pos.x);
					mShiftedPos.y = (int) (((y - StartY) * tileSize.y) + pos.y);
					r.RenderTileScaled(c, mTiles[x][y], mShiftedPos, scale);
				}
			}
	}

	public void RenderPieceShadow(Canvas c, Grid piece, Point pos) {
		if (!OptionsActivity.mShowPieceShadow) return;
		if (piece.IsCollidingWith(this, pos.x, pos.y + piece.GetRealHeight())) return;
		Renderer.INSTANCE.SetAlpha(100);
		int TestY = pos.y + piece.GetRealHeight() + 1;
		while (TestY < (mHeight+1)){
			if (piece.IsCollidingWith(this, pos.x, TestY)){
				mShadowPos.set(pos.x, TestY - 1);

				piece.Render(c, mShadowPos);
				break;
			}
			TestY++;
		}
		Renderer.INSTANCE.SetAlpha(255);
	}
	
	public int GetFirstTileType() {
		for (int x=0; x<mWidth; x++) 
			for (int y=0; y<mHeight; y++)
				if (IsTileExists(x, y))
					return mTiles[x][y].mType;
		return EMPTY_TILE;
	}
	
	public boolean CopyFrom(Grid other){
		if ((other.mWidth != mWidth)  || (other.mHeight != mHeight))
			return false;
	
		for (int x = 0;  x < mWidth; x++)
			for (int y = 0; y < mHeight; y++)
				mTiles[x][y].CopyFrom(other.GetTile(x, y));
		return true;
	}
	
}

