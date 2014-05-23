package com.kurovsky.houseoftheday;

import android.view.MotionEvent;

public class EventQueue {

	public final static EventQueue INSTANCE = new EventQueue(50);
	
	private final int mSize;

	private final long[] mDownTimes;
	private final long[] mEventTimes;
	private final int[] mActions;
	private final float[] mXs;
	private final float[] mYs;

	private volatile int mHead;
	private volatile int mTail;

	public EventQueue(int size) {
		mSize = size;
		mDownTimes = new long[size];
		mEventTimes = new long[size];
		mActions = new int[size];
		mXs = new float[size];
		mYs = new float[size];
		mHead = 0;
		mTail = 0;
	}

	public boolean addEvent(MotionEvent event) {
		final int head = mHead;
		final int tail = mTail;
		int nextTail = tail + 1;
		if (nextTail == mSize) nextTail = 0;
		if (nextTail == head) return false;
		mDownTimes[tail] = event.getDownTime();
		mEventTimes[tail] = event.getEventTime();
		mActions[tail] = event.getAction();
		mXs[tail] = event.getX();
		mYs[tail] = event.getY();
		mTail = nextTail;
		return true;
	}

	public MotionEvent nextEvent() {
		int head = mHead;
		if (head == mTail) return null;
		MotionEvent event = MotionEvent.obtain(mDownTimes[head], mEventTimes[head], mActions[head], mXs[head], mYs[head], 0);
		mHead = head == mSize - 1 ? 0 : head + 1;
		return event;
	}

}
