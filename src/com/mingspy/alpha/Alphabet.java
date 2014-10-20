package com.mingspy.alpha;

import java.io.Serializable;

public abstract class Alphabet implements Serializable
{
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
    public abstract int subAlphLength(int ch);
    /**
     * get the hashed inner code of a char.
     * @param ch
     * @return
     */
    public abstract int getInnerCode(int ch);

    /**
     * translate a string to inner codes, where the last one must be zero.<br>
     * int [] codes = getInnerCodes("xxxx");<br>
     * assert(codes[codes.length -1] == 0);<br>
     * @param str
     * @return
     */
    public final int [] getInnerCodes(String str)
    {
        int [] result = null;
        if(str != null) {
            result = translate(str);
            if(result != null && result[result.length - 1] != 0) {
                throw new RuntimeException("The code not end with zero.");
            }
        }
        return result;
    }

    protected abstract int[] translate(String str);
}
