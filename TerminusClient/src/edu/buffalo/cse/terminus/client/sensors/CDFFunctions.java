package edu.buffalo.cse.terminus.client.sensors;

public class CDFFunctions 
{
	//central difference formula first derivative, order 2
	//3 element array 
	public static float CDF1O2(float[] a,float t){
		float f;
		f = (a[2]-a[0])/(2*t);
		
		return(f);
	}
	
	public static float CDF1O2(float[] a,double t){
		float f;
		f = (float) ((a[2]-a[0])/(2*t));
		
		return(f);
	}
	
	//central difference formula first derivative, order 4
	//5 element array 
	public static float CDF1O4(float[] a,float t){
		float f;
		f = ((-a[4])+(8*a[3])-(8*a[1])+a[0])/(12*t);
		
		return(f);
	}
	
	public static float CDF1O4(float[] a,double t){
		float f;
		f = (float) (((-a[4])+(8*a[3])-(8*a[1])+a[0])/(12*t));
		
		return(f);
	}
	
	//central difference formula first derivative, order 6
	//7 element array 
	public static float CDF1O6(float[] a,float t){
		float f;
		f = (a[6]-(9*a[5])+(45*a[4])-(45*a[2])+(9*a[1])-(a[0]))/(60*t);
		
		return(f);
	}
	
	public static float CDF1O6(float[] a,double t){
		float f;
		f = (float) ((a[6]-(9*a[5])+(45*a[4])-(45*a[2])+(9*a[1])-(a[0]))/(60*t));
		
		return(f);
	}
	
	//central difference formula first derivative, order 8
	//9 element array 
	public static float CDF1O8(float[] a,float t){
		float f;
		f = (-(a[8]/280)+(4*a[7]/105)-(a[6]/5)+(4*a[5]/5)-(4*a[3]/5)+(a[2]/5)-(4*a[1]/105)+(a[0]/280))/t;
		
		return(f);
	}
	
	public static float CDF1O8(float[] a,double t){
		float f;
		f = (float) ((-(a[8]/280)+(4*a[7]/105)-(a[6]/5)+(4*a[5]/5)-(4*a[3]/5)+(a[2]/5)-(4*a[1]/105)+(a[0]/280))/t);
		
		return(f);
	}

	//shift array
	public static void shifta(float[] a){
		for (int i = 0; i < a.length - 1; i++) {
			a[i+1] = a[i];
		}
	}
	//shift time array
	public static void shifta(long[] a){
		for (int i = 0; i < a.length - 1; i++) {
			a[i+1] = a[i];
		}
	}
	
	//average number of seconds between events
	public static float avgt(long[] time){
		float t=0;
		for (int i = 0; i < time.length - 2; i++) {
			t+=(time[i]-time[i+1]);
		}
		t/=(time.length - 2);
		t/=1000000000;
		return t;
	}
}
