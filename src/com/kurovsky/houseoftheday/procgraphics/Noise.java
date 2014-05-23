package com.kurovsky.houseoftheday.procgraphics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public final class Noise {

	//Variant form mines.lulmylumpuy.com

	static public double NoiseFunction(long x) {
		x=(x<<13)^x; 
		return (double)Signed(((x*(x*x*15731+789221)+1376312589) & 2147483647)/(double)2147483647);
	} // returns in the interval [0,1]

	static double SmoothedNoise(long x) {
		return NoiseFunction(x)/2 + NoiseFunction(x-1)/4 + NoiseFunction(x+1)/4;
	}

	static double InterpolatedNoise(double x) {
		long X=(long)Math.floor(x);
		return Interpolate(x-X, SmoothedNoise(X), SmoothedNoise(X+1));
	}

	static double Parameterize(double x, double a, double b) {
		if ((b - a) == 0)
			return 0.5;
		return (x-a)/(b-a);
	} // x is normally between a and b. returns 0 if x==a, 1 if x==b, 0.5 if x is in the middle of a and b etc.


	static double Interpolate(double t, double a, double b) {
		return (a+(b-a)*t);
	} // The reverse of Parameterize: if t=0.5, returns the value in the middle of a and b.

	static double InterpolateBSpline(double t, double a, double b, double c, double d) {
		return 1/6.0*(t*(t*(t*(-a+3*b-3*c+d)+(3*a-6*b+3*c))+3*(c-a))+(a+4*b+c));
	}

	static double InterpolateCatmullRom(double t, double a, double b, double c, double d) {
		if(t==0) return b;
		if(t==1) return c;
		return 0.5*(t*(t*(t*((d-a)+3*(b-c))+(2*a - 5*b + 4*c -d))-a+c))+b;
	}

	static double Saturate(double it){
		return Math.min(Math.max(0.0,it), 1.0);
	} // Convention from GPU shaders

	static double Signed(double t) {
		return 2*t-1;
	} // From Unsigned

	static double HermiteCurve(double t) {
		return t*t*(3-2*t);
	} // 3t²-2t³ Called "Ease" in some art packages

	// returns 0 when x<a, rising to 1 at b with a Hermite curve, holding at 1 thereafter.
	static double HermiteStep(double x, double a, double b) {
		if (a==b) return 0;
		if (x<a) return 0;
		if (x>b) return 1;
		double T = (x-a)/(b-a);	
		return T*T*(3-2*T);
	}

	// Two Dimensional Perlin Noise:

	static double NoiseFunction(long x, long y) {
		return NoiseFunction(x+57*y);
	} // returns in the interval [-1,1]

	static double SmoothedNoise(long x, long y) {
		return (NoiseFunction(x-1, y-1)+NoiseFunction(x+1, y-1)+NoiseFunction(x+1, y+1)+NoiseFunction(x-1, y+1))/16
		+ (NoiseFunction(x-1, y  )+NoiseFunction(x+1, y  )+NoiseFunction(x  , y-1)+NoiseFunction(x  , y+1))/8
		+  NoiseFunction(x,y)/4;
	}

	static double Turbulence(double x, double y, int Octaves, double Persistence) { // Persistence [0-1]. 1=All octaves added equally.
		double Result=0;
		long Frequency=1; // Can't see amplitudes less than 1/256.
		for(double Amplitude=1; (Octaves-- > 0) && (Amplitude>1/256.); Amplitude*=Persistence, Frequency<<=1) {
			Result+=Math.abs(Amplitude * InterpolatedNoise(Frequency * x, Frequency * y));
		}
		return Result;
	}

	static double InterpolatedNoise(double x, double y) {
		long X= (long)Math.floor(x);
		long Y= (long)Math.floor(y);
		return Interpolate(y-Y, Interpolate(x-X, SmoothedNoise(X,Y  ), SmoothedNoise(X+1,Y  )),
				Interpolate(x-X, SmoothedNoise(X,Y+1), SmoothedNoise(X+1,Y+1)));
	}
	
	public static int Vary(int start, int end, Random RNG){
		end++;
		int d = end - start;
		return (RNG.nextInt(d) + start);
	}
	
	static int SelectOne(int chooseFrom[], Random RNG){
		int cnt = chooseFrom.length;
		return chooseFrom[RNG.nextInt(cnt)];
	}
	
	static gColor RemoveOneColorFromList(ArrayList<gColor> list, Random RNG){
		int cnt = list.size();
		int selected = RNG.nextInt(cnt);
	    gColor color = list.get(selected);
		list.remove(selected);
		return color;
	}
	
	public static long GenerateTodaysSeed(){
		Calendar calendar = Calendar.getInstance();
		int Day = calendar.get(Calendar.DAY_OF_MONTH);
		int Month = calendar.get(Calendar.MONTH) + 1;
		int Year = calendar.get(Calendar.YEAR) - 2000;
		long Seed = CombineNumbers(Day, Month);
		Seed = CombineNumbers(Seed, Year);
		Seed = CombineNumbers(Seed, calendar.get(Calendar.HOUR));
		Seed = CombineNumbers(Seed, calendar.get(Calendar.MINUTE));
		Seed = CombineNumbers(Seed, calendar.get(Calendar.SECOND));
		return Seed;
	}

	static long CombineNumbers(long a, long b) {
		int times = 1;
		while (times <= b)
			times *= 10;
		return a * times + b;
	} 
}
