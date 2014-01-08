package com.mingspy.alpha;

import java.io.Serializable;

public interface IAlphabet extends Serializable{
	/**
	 * Size of all character that may appear after the char.<br>
	 * For example: if in our alphabet, there are only 4 words:<br>
	 * abandon,are,air,abort<br>
	 * then, subAlphLength(a) = 4, as b,n,r,i appear after a.<br>
	 * subAlphLength(z) = 0, as z not appear in our alphabet.<br>
	 * subAlphLength(b) = 2, as a,o appear after b.<br>
	 * subAlphLength(r) = 1, as t appear after r. <br>
	 * 
	 * @param ch
	 * @return
	 */
	public int subAlphLength(int ch);
	/**
	 * get the hashed inner code of a char.
	 * @param ch
	 * @return
	 */
	public int getInnerCode(int ch);
	
	/**
	 * translate a string to inner codes.
	 * @param str
	 * @return
	 */
	public int [] getInnerCodes(String str);
}
