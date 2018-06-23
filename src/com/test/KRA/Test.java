package com.test.KRA;

import java.util.Arrays;

import com.tools.KRA.Utils;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int A = 1634881706;
		int C = 1634881706;
		String k = "RAIN";
		System.out.println(Arrays.toString(Utils.Hash(A, C, k)));
		System.out.println(Arrays.toString(Utils.Hash(A, C, k)));
		byte[] b = new byte[2];
		System.out.println(b.length);
	}

}
