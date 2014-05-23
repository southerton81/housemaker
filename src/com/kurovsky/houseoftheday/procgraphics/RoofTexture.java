package com.kurovsky.houseoftheday.procgraphics;

import com.kurovsky.houseoftheday.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class RoofTexture {
	gColorSpline mColorSpline;
	double u;
	double mTurbulenceX = 5.0;
	double mTurbulenceY = 5.0;

	public RoofTexture(gColorSpline ColorSpline) {
		mColorSpline = ColorSpline;
	}

	public int Draw(Canvas c, Bitmap bmp, Paint paint, int width, int height){
		gColor col;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++){
				u = Noise.Turbulence(x/mTurbulenceX, y/mTurbulenceY, 5, .5);
				col = mColorSpline.Get(u);   
				paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
				c.drawPoint(x, y, paint);
			}
		
		col = mColorSpline.Get(1);
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.argb(col.GetAb(), col.GetRb(), col.GetGb(), col.GetBb()));
		
		int tileW = width / 4;
		int tileH = height / 4;	
		Path tilePath = new Path(); 
			
		int currX = 0;
		int currY = tileH;
		int endY = tileH;
		int i = 0;
		int lastX = 0, lastY = 0;
		
		while(currY < height){
			i++;
			tilePath.moveTo(currX, endY);
		    currY += tileH;
		    if (currY > height) break;
		    
			lastX = currX;
			while (currX < width){
				tilePath.quadTo(currX + tileW / 2, currY, currX + tileW, endY);
				currX += tileW;
			}
			
			currX = (i % 2 == 0) ?  0 : -tileW / 2; 
			lastY = endY;
			endY = currY;
		}	
		
		paint.setAntiAlias(true);
		c.drawPath(tilePath, paint);
	
		paint.setAntiAlias(false);
		paint.setColor(Color.BLACK); 
		tilePath.reset();
		lastY += 1;
		tilePath.moveTo(lastX, lastY);
		while (lastX < width){
			tilePath.quadTo(lastX + tileW / 2, lastY + tileH, lastX + tileW, lastY);
			lastX += tileW;
		}
		c.drawPath(tilePath, paint);

		//make transparent lower part
		int minY = height - 1;
		int color  = Color.argb(0, 0, 0, 0);
		for (int x = 0; x < width; x++){
			for (int y = height-1; y > 0; y--){
				if (bmp.getPixel(x, y) == Color.BLACK){
					bmp.setPixel(x, y, color);

					if (bmp.getPixel(x, y-1) != Color.BLACK){
						if (y < minY) minY = y;
						break;
					}
				}
				bmp.setPixel(x, y, color);
			}
		}

		
		return minY;
	}
	
	
	
}
