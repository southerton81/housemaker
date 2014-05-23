package com.kurovsky.houseoftheday.procgraphics;

public class gColorSpline {
	public enum SplineInterpolationType {LinearCSI,HermiteCSI,bSplineCSI,CatmullRomCSI};
	SplineInterpolationType InterpolationType;
	
	gColor mColor = new gColor(0,0,0,0);
	int Count;
	gColor[] Colors;
	double[] mPositions;
	
	gColorSpline(){
	}

	gColorSpline(int count, gColor[] colors){
		Count = count;
		Colors = colors;
		mPositions = null;
		InterpolationType=SplineInterpolationType.CatmullRomCSI;
	}
	
	gColorSpline(int count, gColor[] colors, double[] Positions, SplineInterpolationType interpolationType){
		Count = count;
		Colors = colors;
		mPositions = Positions;
		InterpolationType=interpolationType;
	}
	
	void Init(int count, gColor[] colors, double[] Positions, SplineInterpolationType interpolationType){
		Count = count;
		Colors = colors;
		mPositions = Positions;
		InterpolationType=interpolationType;
	}
	
	void Init(int count, gColor[] colors){
		Count = count;
		Colors = colors;
		mPositions = null;
		InterpolationType=SplineInterpolationType.CatmullRomCSI;
	}

	gColor Get(double t) {
		return Get(t, Count, Colors, mPositions, InterpolationType);
	}

	gColor Get(double t, int Count, gColor[] Colors, double[] Positions/*=0*/, SplineInterpolationType interpolationType/*=CatmullRomCSI*/) {
		if (interpolationType == null)
			interpolationType = InterpolationType;
		
		if(Count==0){
			mColor.SetRGB(t, t, t, 0.0);
			return mColor; // return t as a greyscale level if no Colors defined
		}
		if(Count==1){ 
			mColor.Set(Colors[0].GetRGBA());
			return mColor;
		}
		
		if(t<0.0001) t = .05;
		if(t>0.9999) t = .95;

		/*if(t<0.0001)
			return Colors[0];
		if(t>0.9999) 
			return Colors[Count-1];*/

		int b,c; // Index to Colors and Samples. The sample will be interpolated between b and c using a and d (defined later) as control points for splines (a<=b<=c<=d)
		if(Positions != null) {
			c=0; // Find the Color below t:
			while((c<Count) && (t>Positions[c])) ++c;
			if(c==Count) b=--c; // After Last Entry (Not found)
			else if(c==0) b=c; // Before First Entry
			else b=c-1;       // Normal: position found between entries
			if((b==c) // Linear Interpolation with undefined ends...
					&& ((InterpolationType==SplineInterpolationType.LinearCSI) // ...simply fill to the end with the nearest color:
							|| (InterpolationType==SplineInterpolationType.HermiteCSI))) return Colors[b];
			double Low =(c==0       ? 0 : Positions[b]);
			double High=(b==Count-1 ? 1 : Positions[c]);
			t=(Low==High) ? 0 : Noise.Parameterize(t, Low,High);
		}else{ // Positions==0 Colors (are equally spaced)
			t*=Count-1; // Find which Color to use as a base
			b=(int) Math.floor(t);
			t-=b; // Now t is a parameter between the correct two Colors
			if((t<=0.0001) // At one end...
					&& ((InterpolationType!=SplineInterpolationType.bSplineCSI))) return Colors[b]; // ...and not a bSpline
			c=b+1;
		}
		double bR=Colors[b].GetRd(); // These just help make the following code shorter:
		double bG=Colors[b].GetGd();
		double bB=Colors[b].GetBd();
		double bA=Colors[b].GetAd();
		double cR=Colors[c].GetRd();
		double cG=Colors[c].GetGd();
		double cB=Colors[c].GetBd();
		double cA=Colors[c].GetAd();
		switch(InterpolationType) {
		case HermiteCSI: 
			t=Noise.HermiteCurve(t); // break; fall through: Antialias (ease) sharp changes with a Hermite Curve.
		case LinearCSI: {
			double r=1-t;
			return mColor;//!!! Не перенес gColor(gColor::Blend(bR,r,cR,t), gColor::Blend(bG,r,cG,t), gColor::Blend(bB,r,cB,t), gColor::Blend(bA,r,cA,t));
		} }
		int a=b-1; // Reflect the second point back before the first to create a new beginning:
		double aR=(b!=0 ? Colors[a].GetRd() : Noise.Interpolate(-1, bR,cR));
		double aG=(b!=0 ? Colors[a].GetGd() : Noise.Interpolate(-1, bG,cG));
		double aB=(b!=0 ? Colors[a].GetBd() : Noise.Interpolate(-1, bB,cB));
		double aA=(b!=0 ? Colors[a].GetAd() : Noise.Interpolate(-1, bA,cA));
		int d=c+1; // Reflect the second to last point forward past the last point to create a new end:
		double dR=(d<Count ? Colors[d].GetRd() : Noise.Interpolate(2, bR,cR));
		double dG=(d<Count ? Colors[d].GetGd() : Noise.Interpolate(2, bG,cG));
		double dB=(d<Count ? Colors[d].GetBd() : Noise.Interpolate(2, bB,cB));
		double dA=(d<Count ? Colors[d].GetAd() : Noise.Interpolate(2, bA,cA));

		if(InterpolationType==SplineInterpolationType.bSplineCSI) 
			mColor.SetRGB(
					Noise.Saturate(Noise.InterpolateBSpline(t, aR,bR,cR,dR)),
					Noise.Saturate(Noise.InterpolateBSpline(t, aG,bG,cG,dG)),
					Noise.Saturate(Noise.InterpolateBSpline(t, aB,bB,cB,dB)),
					Noise.Saturate(Noise.InterpolateBSpline(t, aA,bA,cA,dA)));
		else  
			mColor.SetRGB( // CatmullRomCSI:
					Noise.Saturate(Noise.InterpolateCatmullRom(t, aR,bR,cR,dR)),
					Noise.Saturate(Noise.InterpolateCatmullRom(t, aG,bG,cG,dG)),
					Noise.Saturate(Noise.InterpolateCatmullRom(t, aB,bB,cB,dB)),
					Noise.Saturate(Noise.InterpolateCatmullRom(t, aA,bA,cA,dA)));

		return mColor;
	}

}
