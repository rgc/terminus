package edu.buffalo.cse.terminus.client.sensors;

public class CDFFunctions 
{
	//central difference formula first derivative, order 4
	//5 element array 
	public static float CDF1O4(float[] a){
		float f;
		f = ((-a[4])+(8*a[3])-(8*a[1])+a[0])*12;
		
		return(f);
	}

	//shift array
	public static void shifta(float[] a){
		for (int i = 0; i < a.length - 1; i++) {
			a[i+1] = a[i];
		}
	}
}
