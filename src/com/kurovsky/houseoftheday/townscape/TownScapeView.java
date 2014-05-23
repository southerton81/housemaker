package com.kurovsky.houseoftheday.townscape;

import com.kurovsky.houseoftheday.Grid;
import com.kurovsky.houseoftheday.Renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class TownScapeView extends View {

	private Point mPos = new Point(0,0);

	public TownScapeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
    }
    
    public void onDraw(Canvas canvas) {	 
    	Grid Grid1 = TownScapeActivity.GetTownScapeGrid1();
    	
    	
    	Grid1.RenderScaled(canvas, mPos, Renderer.INSTANCE.mTileSize, 1.f);
    }

}
