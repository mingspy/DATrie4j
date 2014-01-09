package com.mingspy.alpha;

public class AlphabetFactory {
	private static Alphabet alphabet = new RoughAlphabet();
	public static Alphabet getAlphabet(){
		return alphabet;
	}
}
