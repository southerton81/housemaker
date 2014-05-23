package com.kurovsky.houseoftheday.procgraphics;

public final class gColor {
	double R,G,B,A;

	double FromByte(int B) {return B*1/255.0;}
	int sgn(double c) {return (Integer) (c<=-1/256.0 ? -1 : (c>=1/256.0));} // to Compare a double value with zero using a tolerance of 1/256.0
	int Compare(double a, double b) {return sgn(b-a);}
	gColor(int r, int   g, int   b, int   a/*=0*/) {
		R = (FromByte(r));
		G = (FromByte(g));
		B = (FromByte(b));
		A = (FromByte(a));

	} // 0-255
	/*gColor(float  r, float  g, float  b, float a) {
		R = ((r));
		G = ((g));
		B = ((b));
		A = ((a));
	}// 0-1
	
	gColor(double r,  double g, double b, double a)  {
		R = ((r));
		G = ((g));
		B = ((b));
		A = ((a));
	}// 0-1*/
	
	gColor(int argb) {
		R = FromByte((argb>>16)&0xFF);
		G = FromByte((argb>>8)&0xFF);
		B = FromByte(argb&0xFF);
		A = FromByte(255);
	}

	void SetRGB (int   r, int   g, int   b, int   a/*=0*/) {R=FromByte(r); G=FromByte(g); B=FromByte(b); A=FromByte(a);} // 0-255
	void SetRGB (float r, float g, float b, float a/*=0*/) {R=r; G=g; B=b; A=a;} // 0-1
	void SetRGB (double r, double g, double b, double a/*=0*/) {R=r; G=g; B=b; A=a;} // 0-1
	void SetBGR     (long    Color/*=NO_COLOR*/) {SetBGRA(Color);}
	void SetBGRA    (long    Color/*=NO_COLOR*/) {
		R=FromByte((int) (Color&0xFF));
		G=FromByte((int) ((Color>>=8)&0xFF)); 
		B=FromByte((int) ((Color>>=8)&0xFF)); 
		A=FromByte((int) ((Color>>=8)&0xFF));
	}
	void SetRGB     (long    Color/*=NO_COLOR*/) {SetRGBA(Color);}
	void SetRGBA    (long    Color/*=NO_COLOR*/) {SetBGRA(Color); double t=R; R=B; B=t;}
	void Set           (long Color/*=NO_COLOR*/) {SetRGBA(Color);}
	void SetLevel(int Level) {R=G=B=FromByte(Level);}
	void SetR(int   c) {R=FromByte(c);}
	void SetG(int   c) {G=FromByte(c);}
	void SetB(int   c) {B=FromByte(c);}
	void SetA(int   c) {A=FromByte(c);}
	void SetR(float  c) {R=c;}
	void SetG(float  c) {G=c;}
	void SetB(float  c) {B=c;}
	void SetA(float  c) {A=c;}
	void SetR(double c) {R=c;}
	void SetG(double c) {G=c;}
	void SetB(double c) {B=c;}
	void SetA(double c) {A=c;}
	void SetFromHue(double H) { // H is Interval [0,1]
	  if(H<1/3.) {//         _    _
	    R=2-H*6;  //   Red: | \__/ |
	    G=H*6;    //        0 __   1
	    B=0;      // Green: |/  \__|
	  }else if(H<2/3.) { // 0   __ 1
	    R=0;      //  Blue: |__/  \|
	    G=4-H*6;  //        0 |  | 1
	    B=H*6-2;  //         1/3 2/3
	  }else{
	    R=H*6-4;
	    G=0;
	    B=(1-H)*6;
	  }
	  if(R>1) R=1;
	  if(G>1) G=1;
	  if(B>1) B=1;
	}
	// Hue, Saturation, Lightness all in the Interval [0,1]
	void SetFromHSL(double H, double S, double L) {
	  SetFromHue(H);
	  R=Noise.Signed(R)*S+1;
	  G=Noise.Signed(G)*S+1;
	  B=Noise.Signed(B)*S+1;
	  if(L<0.5) {
	    R*=L;
	    G*=L;
	    B*=L;
	  }else{
	    R=R*(1-L)+Noise.Signed(L);
	    G=G*(1-L)+Noise.Signed(L);
	    B=B*(1-L)+Noise.Signed(L);
	  }
	}
	// Hue, Saturation, Value, all in the Interval [0,1]
	void SetFromHSV(double H, double S, double V){
	  SetFromHue(H);
	  R=V*(1-S*(1-R));
	  G=V*(1-S*(1-G));
	  B=V*(1-S*(1-B));
	}
	
	long    GetRGB ()      {return (((GetRb()<<8)|GetGb())<<8)|GetBb();} // True-color bitmap data uses this.
	long    GetRGBA()      {return (GetAb()<<24)|GetRGB();}
	long    GetBGR ()      {return (((GetBb()<<8)|GetGb())<<8)|GetRb();} // Windows Device Context colors use this.
	long    GetBGRA()      {return (GetAb()<<24)|GetBGR();}
	int     GetRb()        {return (int) (Math.round(255*R));} // Red   [0-255] Round is used to prevent casting from clearing the processor pipeline! (See Global.h)
	int     GetGb()        {return (int) (Math.round(255*G));} // Green [0-255]
	int     GetBb()        {return (int) (Math.round(255*B));} // Blue  [0-255]
	int     GetAb()        {return (int) (Math.round(255*A));} // Alpha [0-255]
	int     GetRi()        {return GetRb();} // Red   [0-255]
	int     GetGi()        {return GetGb();} // Green [0-255]
	int     GetBi()        {return GetBb();} // Blue  [0-255]
	int     GetAi()        {return GetAb();} // Alpha [0-255]
	float   GetRf()        {return (float) (R);} // Red   [0-1]
	float   GetGf()        {return (float) (G);} // Green [0-1]
	float   GetBf()        {return (float) (B);} // Blue  [0-1]
	float   GetAf()        {return (float) (A);} // Alpha [0-1]
	double  GetRd()        {return R;} // Red   [0-1]
	double  GetGd()        {return G;} // Green [0-1]
	double  GetBd()        {return B;} // Blue  [0-1]
	double  GetAd()        {return A;} // Alpha [0-1]
	
	static void ClampColor(int r, int g, int b){
		r = r>255?255:r;	
		g = g>255?255:g;	
		b = b>255?255:b;
	}

}
