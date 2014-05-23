package com.kurovsky.houseoftheday;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainView extends SurfaceView implements SurfaceHolder.Callback {
	private ThreadHm      mThread = null;
	private Context       mContext;
	private SurfaceHolder mHolder;
	private boolean       mIsActive = false; 

	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// register our interest in hearing about changes to our surface
		mHolder = getHolder();
		mHolder.addCallback(this);
		mContext = context;

		setFocusable(true); // make sure we get key events
		requestFocus();
	}
	
	protected void StoreGame(){
		if (mThread != null)
			mThread.StoreGame();
	}

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mThread = new ThreadHm(holder, mContext, new Handler());
		mThread.setPriority(Thread.NORM_PRIORITY);
		mThread.setSurfaceSize(width, height);
		mThread.start();
		mIsActive = true;
	}

	public void surfaceCreated(SurfaceHolder arg0) {
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (mThread == null) return;
		mIsActive = false;
		boolean retry = true;
		mThread.setRunning(false);
		while (retry) {
			try {
				mThread.join();
				retry = false;

			} catch (InterruptedException e) {
				Log.i("InterruptedException", "MainView::surfaceDestroyed");
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsActive || mThread == null) 
			return false;
		if (event.getAction() == MotionEvent.ACTION_MOVE ||
				event.getAction() == MotionEvent.ACTION_DOWN ||
				event.getAction() == MotionEvent.ACTION_UP)
			EventQueue.INSTANCE.addEvent(event);
		return true;
	}
	
	//public boolean onKeyDown(int keyCode, KeyEvent msg) {
	//	if (!mIsActive || mThread == null) return false;
	//	return mThread.doKeyDown(keyCode, msg);
	//}

	//public boolean onKeyUp(int keyCode, KeyEvent msg) {
	//	if (!mIsActive || mThread == null) return false;
	//	return mThread.doKeyUp(keyCode, msg);
	//}

	//public void OnRoll(float roll){
	//	if (!mIsActive || mThread == null) return;
	//	mThread.doRoll(roll);
	//}
}
