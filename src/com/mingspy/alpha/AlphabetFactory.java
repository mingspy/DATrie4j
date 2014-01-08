package com.mingspy.alpha;

public class AlphabetFactory {
	private static IAlphabet alphabet = new RoughAlphabet();
	public static IAlphabet getAlphabet(){
		return alphabet;
	}
}
