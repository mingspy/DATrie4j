package com.mingspy.alpha;

import com.mingspy.alpha.Alphabet;

public class RoughAlphabet extends Alphabet
{

    /**
     * In Unicode Chinese start from 0x4e00 to 0x952f.<br>
     * and there are 256 English char.
     */
    private final static int AlphLength = 0x952f - 0x4e00 + 256;
    @Override
    public int subAlphLength(int ch)
    {
        return AlphLength;
    }

    @Override
    public int getInnerCode(int ch)
    {
        if(ch < 256) {
            return ch;
        } else if(ch >= 0x4e00) {
            return 256 + ch - 0x4e00;
        }
        return -1;
    }


    @Override
    protected int[] translate(String str)
    {
        int [] ret = new int[str.length() + 1];
        for(int i = 0; i < str.length(); i++) {
            ret[i] = getInnerCode(str.charAt(i));
        }
        ret[ret.length - 1] = 0;
        return ret;
    }

}
