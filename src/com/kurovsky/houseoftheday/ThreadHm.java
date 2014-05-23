package com.kurovsky.houseoftheday;

import com.kurovsky.houseoftheday.soundmanager.SoundManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.JetPlayer;
import android.media.JetPlayer.OnJetEventListener;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class ThreadHm extends Thread { //implements OnJetEventListener {
	private Game mGame; 
	private volatile boolean mRun = true;
	private volatile boolean mStoreGame = false;
	private SurfaceHolder mSurfaceHolder;
	private long mSleepTime;
	private long mDesiredFrameTime = 42;
	private long mBeforeTime;
	private long mDeltaMs;
	private Canvas c = null;
	private Context mContext;

	public ThreadHm(SurfaceHolder surfaceHolder, Context context, Handler handler) {
		mSurfaceHolder = surfaceHolder;
		mContext = context;
		mGame = new Game(mContext);
	}

	public void StoreGame(){
		if (mGame != null) mGame.Store();
		//mStoreGame = true;
	}

	public boolean isRunning() {
		return mRun;
	}

	public void setRunning(boolean b) {
		mRun = b;
	}

	public void run() {
		mBeforeTime = System.nanoTime()/1000000L;
		
		SoundManager.INSTANCE.PlayMusic(mContext, com.kurovsky.houseoftheday.R.raw.polstillgreen);
		
		while (mRun) {
			ProcessEvents();
			
			try {
				//lock canvas so nothing else can use it
				c = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) {
					//clear the screen with the black painter.
					//c.drawRect(0, 0, c.getWidth(), c.getHeight(), blackPaint);
					//This is where we draw the game engine.

					mDeltaMs = (System.nanoTime()/1000000L) - mBeforeTime;
					mBeforeTime += mDeltaMs;
					if (mDeltaMs < 0) mDeltaMs = 0;
					if (mDeltaMs > 100) mDeltaMs = 100;

					mGame.Update(mDeltaMs);
					mGame.Render(c);
					mGame.TryCancelMovement();
				}
			} 
			finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}

			//Sleep time. Time required to sleep to keep game consistent
			//This starts with the specified delay time (in milliseconds) then subtracts from that the
			//actual time it took to update and render the game. This allows our game to render smoothly.
			mSleepTime = mDesiredFrameTime - mDeltaMs;

			try {
				if(mSleepTime <= 0){
					Thread.sleep(3);
				}
				else
					Thread.sleep(mSleepTime);
			} catch (InterruptedException ex) {}
		}

		SoundManager.INSTANCE.Release();
	}

	/*private void DoStoreGame(){
		if (mStoreGame){
			mGame.Store();
			mStoreGame = false;
		}	
	}*/
		
	//boolean doKeyDown(int keyCode, KeyEvent msg) {
	//	synchronized (mSurfaceHolder) {
	//		return mGame.DoKeyDown(keyCode);
	//	}
	//}

	//boolean doKeyUp(int keyCode, KeyEvent msg) {
	//	synchronized (mSurfaceHolder) {
	//		return mGame.DoKeyUp(keyCode);
	//	}
	//}

	//void doRoll(float roll) {
	//	synchronized (mSurfaceHolder) {
	//		mGame.DoRoll(roll);
	//	}
	//}

	//void doTouchUp() {
	//	synchronized (mSurfaceHolder) { 
	//		mGame.DoTouchUp();
	//	}
	//}

	//void doTouchDown(float x, float y) {
	//	synchronized (mSurfaceHolder) {
	//		mGame.DoTouchDown(x, y);
	//	}
	//}
	
	//void DoTouchMove(float x, float y) {
	//	synchronized (mSurfaceHolder) {
	//		mGame.DoTouchMove(x, y);
	//	}
	//}

	private void ProcessEvents() {
		MotionEvent event = EventQueue.INSTANCE.nextEvent();
		int count = 0;
		while (event != null && count < 30) {
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				mGame.DoTouchDown(event.getX(), event.getY());

			if (event.getAction() == MotionEvent.ACTION_MOVE)
				mGame.DoTouchMove(event.getX(), event.getY());

			if (event.getAction() == MotionEvent.ACTION_UP)
				mGame.DoTouchUp();

			event.recycle();
			count++;
			event = EventQueue.INSTANCE.nextEvent();
		}
	}

	void setSurfaceSize(int width, int height) {
		mGame.SetSurfaceSize(width, height);
	}

}
