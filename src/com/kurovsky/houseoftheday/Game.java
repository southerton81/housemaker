package com.kurovsky.houseoftheday;

import com.kurovsky.houseoftheday.buttons.ButtonManager;
import com.kurovsky.houseoftheday.highscores.SubmitScoreDialog;
import com.kurovsky.houseoftheday.procgraphics.Noise;
import com.kurovsky.houseoftheday.soundmanager.SoundManager;
//import com.kurovsky.houseoftheday.townscape.TownScapeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

public class Game implements ButtonManager.Listener {
	
	public static Game INSTANCE                  = null;
	
	public static boolean    mRestoreGame        = false;
	public static final int  WALL_TYPES_COUNT    = 4;
	public static final int  WALL_STYLES_COUNT   = 3;
	public static final int  ROOF_STYLES_COUNT   = 2;
	public static final int  GRID_DIMENSIONX     = 10;
	public static final int  GRID_DIMENSIONY     = 20;
	
	private static final int MODE_ACTIVE_PIECE 	= 0;
	private static final int MODE_EXPLODING    	= 1;
	private static final int MODE_GAME_OVER  	= 2;
	private static final int MODE_FALLING 		= 3;
	private static final int MODE_DISSAPEARING 	= 4;
	private static final int MODE_PAUSE		 	= 5;
	private static final int MODE_NEWLEVEL		= 6;

	private static final int mDownTimeMs = 80;
	private static final int mTimeToLandMs = 500;
	private static final int mWaitTillExplodeMs = 300;	
	private static       int mSensitivityMs = 400;	
	private static       int mSensitivityRollMs = 400;	
	private static final int mSensitivityRotateMs = 200;	
	private static final int mFallingTimeoutMs = 130;
	
	private long mLastTouchDownTimestamp = 0;
	private long mFirstTouchDownTimestamp = -1;
	private long mLastRotateTimestamp = 0;
	private long mLastRollTimestamp = 0;
	private long mDeltaMs = 0; 
	private long mTimer = 0;     // Millisecond accumulator used for speed regulation
	private long mLandTimer = 0; // Millisecond accumulator used for measuring the time between the piece touching the ground and actually finally landing.

	private Grid mGrid = new Grid(GRID_DIMENSIONX, GRID_DIMENSIONY);
	private Grid mActivePiece = new Grid(3,3);
	private Grid mNextPiece = new Grid(3, 3);
	private int  mSlideDirection = 0;
	private Grid mTempPiece;
	private volatile int mMode;
	private volatile int mLastMode;
	private int  mDirection;
	private int  mCntPiece = 0;
	private boolean mIsUserPressedDown = false;
	private boolean mPieceCapturedFlag = false;
	
	private Point mTileSize = null;
	private int   mMovementX = 0;
	private int   mMovementY = 0;
	private int   mRotation = 0;
	private Rect  mPieceRc = new Rect();
	private Rect  mPieceRc2 = new Rect();
	private int   mLastX;
	private Context mContext = null;
	private boolean mIsSellStopped = false;
	private boolean mIsPieceLanded = true;
	private Rect  mGridRc = new Rect(0, 0, 0, 0);
	private Point mPiecePos = new Point();
	private Point mCenterScreenPos = new Point();
	private ShapesGenerator  mShapesGenerator = new ShapesGenerator();
	private ButtonManager 	 mButtonMan = new ButtonManager();
	private ScoreAccumulator mScoreAccumulator = new ScoreAccumulator();
	private long mRestoredSeed = 0;

	public ButtonManager GetBm() {
		return mButtonMan;
	}
	
	void Store(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor PrefsEditor = sp.edit();
		
		mActivePiece.Store(PrefsEditor, "APiece");   
		mGrid.Store(PrefsEditor, "");   
		PrefsEditor.putInt("PieceCount", mCntPiece);
		PrefsEditor.putBoolean("IsGameStored", (mMode != MODE_GAME_OVER));
		PrefsEditor.putLong("Score", mScoreAccumulator.GetBalance());
		PrefsEditor.putLong("Seed", Renderer.INSTANCE.GetLastSeed());
		mScoreAccumulator.Store(PrefsEditor);

		PrefsEditor.commit();
	}
		
	void Restore(){
		if (!mRestoreGame) return;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		if (!sp.getBoolean("IsGameStored", true)) return;
		mCntPiece = sp.getInt("PieceCount", 0);
		mGrid.Restore(sp, "");
		mActivePiece.Restore(sp, "APiece");  
		mScoreAccumulator.Restore(mContext);
		GroupsGenerator.INSTANCE.SetMinPriceForHouse(mScoreAccumulator.GetLevel().MinBlocksForHouse());
		mRestoredSeed = sp.getLong("Seed", 0);
		
		GetNextUid();
		CreateNextPiece();
		mPiecePos.y = -mActivePiece.GetRealHeight();
	}
	
	Game(Context context) {
		Game.INSTANCE = this;
		mContext = context;
		Reset();
		
		if (mRestoreGame)
			Restore();
	}

	void Reset() {
		//TownScapeActivity.Reset();
		mCntPiece = 0;
		mGrid.Clear();
		GroupsGenerator.INSTANCE.Initialize(mGrid, mScoreAccumulator.GetLevel().MinBlocksForHouse());
		CreateNextPiece();
	    mIsPieceLanded = true;
		NewPiece();
	}
	
	boolean GetActivePieceRc(Rect rc){
		if (mMode != MODE_ACTIVE_PIECE) return false;
		mActivePiece.GetMbr(rc);
		rc.offsetTo(mPiecePos.x, mPiecePos.y);
		return true;
	}
	
	private void CreateNextPiece() {	
		mShapesGenerator.GenerateShape(mNextPiece, mCntPiece, mScoreAccumulator.GetLevel());
		mPiecePos.x = (mGrid.mWidth/2) - 1;
		mPiecePos.y = -mNextPiece.GetRealHeight();
		mTimer = 0;
		mLandTimer = 0;
		mSlideDirection = 0;
		GetNextUid();
	}

	private void NewPiece() {
		if (!mIsPieceLanded){ //Continue with this one
			SetMode(MODE_ACTIVE_PIECE);
			return;
		}

		// mActivePiece piece will point to mNextPiece, while mNextPiece will point to old mActivePiece, and overwrite its contents soon
		mActivePiece.Clear();
		mTempPiece = mActivePiece;
		mActivePiece = mNextPiece;
		mNextPiece = mTempPiece;

		CreateNextPiece();
		SetMode(MODE_ACTIVE_PIECE);
		//mScoreAccumulator.PayForPiece(mActivePiece);
		mIsPieceLanded = false;
	}
	
	public int GetNextUid(){
		int PrevCntPiece = mCntPiece;
		mCntPiece++;
		return PrevCntPiece;
	}
	
	void Render(Canvas c) {
		if (mTileSize == null || c == null || mContext == null ) return;
		Renderer.INSTANCE.DrawBackground(c);
		mScoreAccumulator.Render(c, mNextPiece, mTileSize);
		GroupsGenerator.INSTANCE.RenderDissapearingGroups(c);
		mGrid.RenderMainGrid(c);

		if (mMode == MODE_GAME_OVER || mMode == MODE_ACTIVE_PIECE || mMode == MODE_PAUSE){
			if (mPieceCapturedFlag) Renderer.INSTANCE.AddBlur(true);
			mActivePiece.Render(c, mPiecePos);
			Renderer.INSTANCE.AddBlur(false);
		}
		if (mMode == MODE_ACTIVE_PIECE)
			mGrid.RenderPieceShadow(c, mActivePiece, mPiecePos);
		if (mMode == MODE_PAUSE)
			Renderer.INSTANCE.RenderText(c, mContext.getString(R.string.PauseLabel), mCenterScreenPos, mTileSize.y * 2, Color.rgb(253, 196, 45), Color.rgb(90, 0, 90));
		if (mMode == MODE_NEWLEVEL)
			Renderer.INSTANCE.RenderNewLevelView(c, mScoreAccumulator.GetNewLevelView());
		
		mButtonMan.RenderButtons(c);
		EffectsManager.INSTANCE.Render(c);
	}

	void Update(long deltaMs) {
		if (mTileSize == null || mMode == MODE_PAUSE) return;

		if (mMode != MODE_NEWLEVEL)
			mTimer += deltaMs;

		mDeltaMs = deltaMs;
		if (mMode == MODE_EXPLODING) 			OnModeExploding();
		else if (mMode == MODE_ACTIVE_PIECE) 	OnModeActivePiece();
		else if (mMode == MODE_DISSAPEARING) 	OnModeDissapearing();
		else if (mMode == MODE_FALLING) 		OnModeFalling();

		EffectsManager.INSTANCE.Update(deltaMs);
	} 

	private void LandPiece() {  
		int SoundResId = com.kurovsky.houseoftheday.R.raw.brick;
		switch (mActivePiece.GetFirstTileType()) {
			case Grid.ROOF_TYPE :
				SoundResId = com.kurovsky.houseoftheday.R.raw.roof; break;
			case Grid.WOOD_TYPE : 
				SoundResId = com.kurovsky.houseoftheday.R.raw.wood; break;
			default:
				SoundResId = com.kurovsky.houseoftheday.R.raw.brick; break; 
		}
		 
		SoundManager.INSTANCE.PlaySound(mContext, SoundResId); 
		mIsPieceLanded = true;
		
		if (mPiecePos.y < 0) { 
			SetMode(MODE_GAME_OVER);
			return;
		}
		
		mActivePiece.AddToGrid(mGrid, mPiecePos.x, mPiecePos.y);
		mTimer = 0;
		if (mGrid.RemoveRoofsFromGround())
			SoundManager.INSTANCE.PlaySound(mContext, com.kurovsky.houseoftheday.R.raw.breakdown);
		SetMode(MODE_EXPLODING);
	}

	private void SetMode(int mode) {
		if (mode == MODE_GAME_OVER)
			SubmitScoreDialog.Show((Activity)mContext, mScoreAccumulator.GetBalance());

		if (mMode == MODE_NEWLEVEL)
			mButtonMan.SetButtonPlayPos(0,0,0,0,false);
		
		mMode = mode;
	}

	private void applyUserInput() {
		mDirection = ((mMovementX < 0) ? -1 : (mMovementX > 0) ? 1 : 0);

		if (mMovementX != 0 && MovePiece(mDirection, 0, 0)) {
			
			// Reset land timer, to ensure user can slide pieces along the floor when necessary
			// Note: this only applies to moving it repeatedly in one direction to avoid the piece never landing.
			
			if (mSlideDirection == mMovementX || mSlideDirection == 0) {
				mSlideDirection = mMovementX;
				mLandTimer = 0;
			}
			mMovementX -= mDirection;
		}

		if (mRotation != 0) {
			if      (MovePiece(0, 0, mRotation))  mRotation = 0;
			else if (MovePiece(1, 0, mRotation))  mRotation = 0;
			else if (MovePiece(-1, 0, mRotation)) mRotation = 0;
			else if (MovePiece(0, 1, mRotation))  mRotation = 0;
			else if (MovePiece(0, 2, mRotation))  mRotation = 0;
			mLandTimer = 0;
		}
	}

	private boolean MovePiece(int x, int y, int rotation) {
		if (x == 0 && y == 0 && rotation == 0) return true;
		
		mActivePiece.Rotate(rotation);
		if (!mActivePiece.IsCollidingWith(mGrid, mPiecePos.x + x, mPiecePos.y + y)) {
			mPiecePos.offset(x, y);
			return true;
		}

		if (rotation != 0){	
			//Try to move up and rotate
			
			if (!mActivePiece.IsCollidingWith(mGrid, mPiecePos.x + x, mPiecePos.y + y - 1)){
				mPiecePos.offset(x, y - 1);		
				//Log.d("rotation!=0", "returning true");			
				return true;
			}
			
			//Try to move left and rotate
			if (mActivePiece.mWidth == 3){
				if (!mActivePiece.IsCollidingWith(mGrid, mPiecePos.x + x - 1, mPiecePos.y + y)){
					mPiecePos.offset(x - 1, y);	
					return true;
				}
			}

		}
		
		mActivePiece.Rotate(-rotation);
		return false;
	}


	private void OnModeActivePiece() {
		if (mScoreAccumulator.IsNewLevelFlagSet())
			SetMode(MODE_NEWLEVEL);
		else {
			applyUserInput();

			long Gravity = mScoreAccumulator.GetCurrentGravity();

			while (mTimer > Gravity || (mIsUserPressedDown && mTimer >= mDownTimeMs)) {
				if (MovePiece(0, 1, 0)) {
					if (mIsUserPressedDown) mTimer = 0;
					else mTimer -= Gravity;
					mSlideDirection = 0;
					mLandTimer = 0;
				} else {
					mLandTimer += mDeltaMs;
					if (mLandTimer > mTimeToLandMs || mIsUserPressedDown) LandPiece();
					else mTimer = Math.min(mTimer, Gravity);
					break;
				}
			}
		}
	}

	private void OnModeDissapearing() {
		if (!GroupsGenerator.INSTANCE.IsDissapearingGridVisible())
			SetMode(MODE_FALLING);	
	}

	private void OnModeFalling(){	
		if (mTimer >= mFallingTimeoutMs){
			mTimer = 0;
			if (!mGrid.MakeFall(this)) {
					SetMode(MODE_EXPLODING);
			}
		}
		if (mGrid.RemoveRoofsFromGround())
			SoundManager.INSTANCE.PlaySound(mContext, com.kurovsky.houseoftheday.R.raw.breakdown);
	}

	private void OnModeExploding() {
		if (mTimer < mWaitTillExplodeMs) return;
		mTimer = 0;
		if (GroupsGenerator.INSTANCE.GenerateGroups()){
			if (mIsSellStopped){
				GroupsGenerator.INSTANCE.HighlightGroups();
				NewPiece();	
			}
			else{
				GroupsGenerator.INSTANCE.RemoveGroupsFromGrid(mScoreAccumulator);	
				mScoreAccumulator.AddChainReactionCount();	
				SetMode(MODE_DISSAPEARING);
				SoundManager.INSTANCE.PlaySound(mContext, com.kurovsky.houseoftheday.R.raw.sell);
			}

			//!!!TownScapeActivity.AddHouse(GroupsGenerator.INSTANCE.GetDissapearingGrid());	
		}
		else{
			mScoreAccumulator.ClearChainReactionsCount();
			NewPiece();	
		}
	}
	
	public void SetSurfaceSize(int width, int height) {	
		mGridRc.setEmpty();
		mCenterScreenPos.set(width / 2, height / 2);
		
		long Seed = ((mRestoredSeed == 0) ? Noise.GenerateTodaysSeed() : mRestoredSeed);
		mTileSize = Renderer.INSTANCE.SetupImages(Seed, mContext, width, height, mGridRc);
		
		Renderer.INSTANCE.LoadBackgrounds(mContext);
		EffectsManager.INSTANCE.SetupImages(mContext, mTileSize, mGridRc);
		mButtonMan.CreateGameButtons(mContext, Renderer.INSTANCE.GetExternalGridRc(), width, height);
		mButtonMan.AddListener(this);
		mScoreAccumulator.Init(mContext, Renderer.INSTANCE.GetExternalGridRc(), width, height);
	}

	public boolean DoKeyDown(int keyCode) {
    	if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_Q) {
    		mMovementX = -1;
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_W) {
    		mMovementX = 1;
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
    		mMovementY = 1;
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
    		mRotation = 1;
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
    		mRotation = -1;
    		return true;
    	}
        return false;
	}

	public boolean DoKeyUp(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_Q
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_W) {
			mMovementX = 0;
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			mMovementY = 0;
			return true;
		}	 
		else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_DPAD_UP){
			mRotation = 0;
			return true;
		}
		return false;
	}

	/*public void DoRoll(float roll){
		if (roll < 15 && roll > -15) {
			mMovementX = 0;
			mLastRollTimestamp = 0;
			mSensitivityRollMs = 400;
		}
		else {
			long Now = System.nanoTime()/1000000L;
			long Period = Now - mLastRollTimestamp;
			if (Period < mSensitivityRollMs) return;
			
			if (mLastRollTimestamp != 0) mSensitivityRollMs = 100;	
			mLastRollTimestamp = Now;

			if (roll > 15)
				mMovementX = -1;
			else 
				mMovementX = 1;
		}
	}*/

	void TryLandPiece(){
		if (!mIsUserPressedDown) return;
		long Now = System.nanoTime()/1000000L;
		long Period = Now - mFirstTouchDownTimestamp;
		if (Period < 200)
			OnPressLand();
	}
	
	public void DoTouchUp() {
		TryLandPiece();
		
		int KeyCode = mButtonMan.GetPressedButtonCode();
		if (KeyCode == ButtonManager.BUTTON_PLAY)
			SetMode(MODE_ACTIVE_PIECE);

		mButtonMan.DoTouchUp();
		mMovementX = 0;
		mMovementY = 0;
		mRotation = 0;
		mSensitivityMs = 400;
		mLastTouchDownTimestamp = 0;
		mFirstTouchDownTimestamp = -1;
		mIsUserPressedDown = false;
		mPieceCapturedFlag = false;
	}

	public void DoTouchMove(float x, float y) {
		DoTouchDown(x, y);
	}
	
	public void DoTouchDown(float x, float y) {
		if (mTileSize == null) return;
		
		if (mFirstTouchDownTimestamp == -1)
			mFirstTouchDownTimestamp = System.nanoTime()/1000000L;
		
		long Now = System.nanoTime()/1000000L;
		long Period = Now - mLastTouchDownTimestamp;
		if (Period < mSensitivityMs) return;
		
		if (mLastTouchDownTimestamp != 0) mSensitivityMs = 100;	
		mLastTouchDownTimestamp = Now;
		
		if (mButtonMan.DoTouchDown(x,y))
			DoTouchButton();
		else if (y < Renderer.INSTANCE.GetExternalGridRc().bottom)
			DoTouchGrid(x,y);
	}
	
	void DoTouchButton(){	
		int KeyCode = mButtonMan.GetPressedButtonCode();
		if (KeyCode == ButtonManager.BUTTON_MOVELEFT)
			mMovementX = -1;
		else if (KeyCode == ButtonManager.BUTTON_MOVERIGHT)
			mMovementX = 1;
		else {
			long Now = System.nanoTime()/1000000L;
			if (Now - mLastRotateTimestamp < mSensitivityRotateMs) return;
			mLastRotateTimestamp = Now;

			if (KeyCode == ButtonManager.BUTTON_ROTATE1){
				mRotation = 1;	
				mTimer = 0;
			}
			else if (KeyCode == ButtonManager.BUTTON_ROTATE2){
				mRotation = -1;
				mTimer = 0;
			}
			else if (KeyCode == ButtonManager.BUTTON_LAND)
				OnPressDown();
		}	
	}
	
	public void OnToggleButton(int id, boolean toggled){
		if (id == ButtonManager.BUTTON_STOPSELL) {
			if (toggled) 
				mIsSellStopped = true;
			else if (!toggled && mIsSellStopped){
				mIsSellStopped = false;
				mButtonMan.DoUntoggle(ButtonManager.BUTTON_PAUSE);
				SetMode(MODE_EXPLODING);
			}
		}
		else if (id == ButtonManager.BUTTON_PAUSE) 
			OnPause();
	}

	void OnPause() {
		if (mMode == MODE_PAUSE) UnpauseGame(); else PauseGame();
	}		

	public void UnpauseGame(){
		SetMode(mLastMode);	
	}

	public void PauseGame(){
		mLastMode = mMode;
		SetMode(MODE_PAUSE);
	}
	
	void OnPressLand(){
		if (mMode != MODE_ACTIVE_PIECE) return;
		while (MovePiece(0, 1, 0));
		AddLandingSmoke();
		LandPiece();
	}
	
	void OnPressDown(){
		if (mMode != MODE_ACTIVE_PIECE) return;
		mIsUserPressedDown = true;
	}
	
	void AddLandingSmoke(){
		int w = mActivePiece.GetWidth();
		int h = mActivePiece.GetHeight();

		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
			{
				if (!mActivePiece.IsTileExists(x, y)) continue;
				if (mActivePiece.GetTile(x, y).IsRoofTile()) return;
				int GridX = mPiecePos.x + x;
				int GridY = mPiecePos.y + y + 1;
				if (mGrid.IsTileExists(GridX, GridY))
					if (!mGrid.GetTile(GridX, GridY).IsRoofTile())
						EffectsManager.INSTANCE.AddSmokeFragment(GridX, GridY - 1);
			}
	}
	
	public void TryCancelMovement() {
		if (mTileSize == null) return;
    	if (mMovementX == 0 && mMovementY == 0) return;    	
    	if (!GetActivePieceRc(mPieceRc)) return;
    	mPieceRc.left = (mPieceRc.left * mTileSize.x) + mGridRc.left;
    	mPieceRc.right = (mPieceRc.right * mTileSize.x) + mGridRc.right;
    	mPieceRc.top = (mPieceRc.top * mTileSize.y) + mGridRc.top;
    	mPieceRc.bottom = (mPieceRc.bottom * mTileSize.y) + mGridRc.bottom;
        if (mMovementX != 0 && mLastX > mPieceRc.left && mLastX < (mPieceRc.left + mTileSize.x))
    		mMovementX = 0;
	}

	void DoTouchGrid(float x, float y){	
		if (!GetActivePieceRc(mPieceRc)) return;
		
		mPieceRc.left = (mPieceRc.left * mTileSize.x) + mGridRc.left;
		mPieceRc.top = (mPieceRc.top * mTileSize.y) + mGridRc.top;
		mPieceRc.right = (mPieceRc.right * mTileSize.x) + mGridRc.left;;
		mPieceRc.bottom = (mPieceRc.bottom * mTileSize.y) + mGridRc.top;

		mPieceRc2.set(mPieceRc);
		mPieceRc2.inset(-mPieceRc2.width()/2, -mPieceRc2.height()/2);
		
		if (mPieceRc2.contains((int)x, (int)y) || mPieceCapturedFlag)
			OnCapturePiece(mPieceRc, x, y);
		else {
			int centerX = mPieceRc.centerX();
			SetMovement(centerX, x, y);
		}

		mTimer = 0;
	}

	void SetMovement(int centerX, float x, float y){
		//if (x < PieceRc.right && x > PieceRc.left && y > PieceRc.bottom)
			//mMovementY = 1;
		if (x >= centerX)
			mMovementX = 1;
		else if (x < centerX)
			mMovementX = -1;
	
		mLastX = (int)x;	
	}
	
	private void OnCapturePiece(Rect pieceRc, float x, float y) {
		GetActivePieceRc(mPieceRc);
		
		x -= mGridRc.left;
		y -= mGridRc.top;
		
		int TileNumberHor = (int) Math.round(x / mTileSize.x);
		int TileNumberVer = (int) Math.round(y / mTileSize.y);
		
		int OffsetHor = (int) -(mPiecePos.x - TileNumberHor);
		int OffsetStepHor = (OffsetHor > 0 ? 1 : -1); 

		int OffsetVer = (int) -(mPiecePos.y - TileNumberVer);
		//int OffsetStepVer = (OffsetVer > 0 ? 1 : -1); 
		
		if (OffsetHor != 0 && MovePiece(OffsetStepHor, 0, 0))
			mSensitivityMs = 0;
		//else if (OffsetVer != 0 && MovePiece(0, OffsetStepVer, 0))
			//mSensitivityMs = 0;
		
		mPieceCapturedFlag = true;
	}

}

