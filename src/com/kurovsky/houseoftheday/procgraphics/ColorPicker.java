package com.kurovsky.houseoftheday.procgraphics;

import java.util.ArrayList;

import com.kurovsky.houseoftheday.procgraphics.gColorSpline.SplineInterpolationType;

import java.util.Random;

import android.graphics.Color;

public class ColorPicker {
	private static final int mRoofHues[] = {360, 290, 194, 132, 28};
	private gColor		     mColors[];
	private Random			 mpRng;

	ArrayList<gColor> mBrickHuesBasic   = new ArrayList<gColor>();
	ArrayList<gColor> mBrickHuesLight   = new ArrayList<gColor>();
	ArrayList<gColor> mBrickHuesLighter = new ArrayList<gColor>();

	ColorPicker(Random rng){
		mpRng = rng;
		float hsv[] = new float[3];
		
		//mBrickHuesBasic
		hsv[0] = Noise.Vary(0, 20, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 127, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(30, 50, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 127, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(70, 150, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 127, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(190, 270, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 127, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(290, 330, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 127, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));
		
		//Grays
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(0, 50, mpRng)/255;
		hsv[2] = (float)Noise.Vary(60, 100, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));
		
		//Blacks
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(100, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(0, 40, mpRng)/255;
		mBrickHuesBasic.add(new gColor(Color.HSVToColor(hsv)));
		
		
		//mBrickHuesLight
		hsv[0] = Noise.Vary(0, 20, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(30, 50, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(70, 150, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(190, 270, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(290, 330, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));

		//Grays
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(0, 50, mpRng)/255;
		hsv[2] = (float)Noise.Vary(150, 190, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));
		
		//Blacks
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(80, 130, mpRng)/255;
		hsv[2] = (float)Noise.Vary(80, 130, mpRng)/255;
		mBrickHuesLight.add(new gColor(Color.HSVToColor(hsv)));
		
		//mBrickHuesLighter		
		hsv[0] = Noise.Vary(0, 20, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(30, 50, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));
		
		hsv[0] = Noise.Vary(70, 150, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(190, 270, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));

		hsv[0] = Noise.Vary(290, 330, mpRng);
		hsv[1] = (float)Noise.Vary(200, 255, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));

		//Grays
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(0, 50, mpRng)/255;
		hsv[2] = (float)Noise.Vary(210, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));
		
		//Blacks
		hsv[0] = 0;
		hsv[1] = (float)Noise.Vary(0, 120, mpRng)/255;
		hsv[2] = (float)Noise.Vary(190, 255, mpRng)/255;
		mBrickHuesLighter.add(new gColor(Color.HSVToColor(hsv)));
	}
	
	gColorSpline generateBrickColors(gColorSpline colorSpline){
		mColors = new gColor[3];
		mColors[0] = Noise.RemoveOneColorFromList(mBrickHuesBasic, mpRng);
		mColors[1] = Noise.RemoveOneColorFromList(mBrickHuesLight, mpRng);
		mColors[2] = Noise.RemoveOneColorFromList(mBrickHuesLighter, mpRng);
		colorSpline.Init(3, mColors);
		return colorSpline;
	}

	gColorSpline generateWoodColors(gColorSpline colorSpline){
		int colorsCnt = 3;
		mColors = new gColor[colorsCnt];
		int r = 0, g = 0, b = 0;

		for (int cnt = 0; cnt < colorsCnt; cnt++){
			if (cnt == 0) {
				r = mpRng.nextInt(125 - 80) + 80;
				g = mpRng.nextInt(70 - 40) + 40;
				b = 0;
			}
			if (cnt == 1) {
				r = mpRng.nextInt(200 - 125) + 125;
				g = mpRng.nextInt(100 - 70) + 70;
				b = 0;
			}
			if (cnt == 2) {
				r = mpRng.nextInt(255 - 200) + 80;
				g = mpRng.nextInt(170 - 100) + 40;
				b = mpRng.nextInt(90);
			}
			gColor.ClampColor(r,g,b);
			mColors[cnt] = new gColor(r,g,b,255);
		}
		colorSpline.Init(colorsCnt, mColors);
		return colorSpline;
	}

	gColorSpline generateGlassColors(gColorSpline colorSpline){
		int numColors = 2;
		gColor Colors[] = new gColor[numColors];
		double Positions[] = {0, 1};
		int r = mpRng.nextInt(100) + 155;
		int b = mpRng.nextInt(100) + 155;
		int g = mpRng.nextInt(100) + 155;
		Colors[0] = new gColor(r, g, b, 255);
		r = mpRng.nextInt(100) + 200;
		b = mpRng.nextInt(100) + 200;
		g = mpRng.nextInt(100) + 200;
		gColor.ClampColor(r, g ,b);
		Colors[1] = new gColor(r, g, b, 255);
		
		colorSpline.Init(numColors, Colors, Positions, SplineInterpolationType.CatmullRomCSI);
		return colorSpline;
	}
	
	gColorSpline generateRoofColors(gColorSpline colorSpline){
		
		mColors = new gColor[3];
		mColors[0] = Noise.RemoveOneColorFromList(mBrickHuesBasic, mpRng);
		mColors[1] = Noise.RemoveOneColorFromList(mBrickHuesLight, mpRng);
		mColors[2] = Noise.RemoveOneColorFromList(mBrickHuesLighter, mpRng);
		colorSpline.Init(3, mColors);
		return colorSpline;
		
		/*int NumColors = 2;
		gColor Colors[] = new gColor[NumColors];
		float hue = (float)Noise.SelectOne(mRoofHues, mpRng);
		
		float hsv[] = new float[3];
		hsv[0] = hue - Noise.Vary(0, 10, mpRng);
		hsv[1] = (float)Noise.Vary(70, 90, mpRng)/255;
		hsv[2] = (float)Noise.Vary(75, 85, mpRng)/255;
		Colors[0] = new gColor(Color.HSVToColor(hsv));
		
		hsv[0] = hue - Noise.Vary(0, 10, mpRng);
		hsv[1] = (float)Noise.Vary(40, 50, mpRng)/255;
		hsv[2] = (float)Noise.Vary(35, 50, mpRng)/255;
		Colors[1] = new gColor(Color.HSVToColor(hsv));

		double Positions[] = {0, 1};
		colorSpline.Init(NumColors, Colors, Positions, SplineInterpolationType.CatmullRomCSI);
		return colorSpline;*/
	}
}
