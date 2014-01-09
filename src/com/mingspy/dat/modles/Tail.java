package com.mingspy.dat.modles;

import java.io.Serializable;
import java.util.Arrays;

import com.mingspy.array.ListInt;

public class Tail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8043632029060603336L;

	public class TailBlock {
		public int next_free;
		public Object data;
		public ListInt suffix;
	}

	private static final int TAIL_START_BLOCKNO = 1;

	private int num_tails = 1;
	private TailBlock[] tails = new TailBlock[1];
	private int first_free;

	public Tail(){
	}
	/**
	 * @brief Get suffix
	 * 
	 * @param t
	 *            : the tail data
	 * @param index
	 *            : the index of the suffix
	 * 
	 * @return an allocated string of the indexed suffix.
	 * 
	 *         Get suffix from tail with given index. The returned string is
	 *         allocated. The caller should free it with free().
	 */
	public ListInt getSuffix(int index) {
		index -= TAIL_START_BLOCKNO;
		return (index < num_tails) ? tails[index].suffix : null;
	}

	/**
	 * @brief Set suffix of existing entry
	 * 
	 * @param t
	 *            : the tail data
	 * @param index
	 *            : the index of the suffix
	 * @param suffix
	 *            : the new suffix
	 * 
	 *            Set suffix of existing entry of given index in tail.
	 */
	public boolean setSuffix(int index, ListInt suffix) {
		index -= TAIL_START_BLOCKNO;
		if (index < num_tails) {
			/*
			 * suffix and tails[index].suffix may overlap; so, dup it before
			 * it's overwritten
			 */
			tails[index].suffix = suffix;

			return true;
		}
		return false;
	}

	/**
	 * @brief Add a new suffix
	 * 
	 * @param t
	 *            : the tail data
	 * @param suffix
	 *            : the new suffix
	 * 
	 * @return the index of the newly added suffix.
	 * 
	 *         Add a new suffix entry to tail.
	 */
	public int addSuffix(ListInt suffix) {
		int new_block;

		new_block = allocBlock();
		setSuffix(new_block, suffix);

		return new_block;
	}

	private int allocBlock() {
		int block;

		if (0 != first_free) {
			block = first_free;
			first_free = tails[block].next_free;
		} else {
			block = num_tails;
			tails = Arrays.copyOf(tails, ++num_tails);
			tails[block] = new TailBlock();
		}
		tails[block].next_free = -1;
		tails[block].data = null;
		tails[block].suffix = null;

		return block + TAIL_START_BLOCKNO;
	}

	private void freeBlock(int block) {
		int i, j;
		block -= TAIL_START_BLOCKNO;
		if (block >= num_tails)
			return;

		tails[block].data = null;
		tails[block].suffix = null;

		/* find insertion point */
		j = 0;
		for (i = first_free; i != 0 && i < block; i = tails[i].next_free)
			j = i;

		/* insert free block between j and i */
		tails[block].next_free = i;
		if (0 != j)
			tails[j].next_free = block;
		else
			first_free = block;
	}

	/**
	 * @brief Get data associated to suffix entry
	 * 
	 * @param t
	 *            : the tail data
	 * @param index
	 *            : the index of the suffix
	 * 
	 * @return the data associated to the suffix entry
	 * 
	 *         Get data associated to suffix entry index in tail data.
	 */
	public Object getData(int index) {
		index -= TAIL_START_BLOCKNO;
		return (index < num_tails) ? tails[index].data : null;
	}

	/**
	 * @brief Set data associated to suffix entry
	 * 
	 * @param t
	 *            : the tail data
	 * @param index
	 *            : the index of the suffix
	 * @param data
	 *            : the data to set
	 * 
	 * @return booleanean indicating success
	 * 
	 *         Set data associated to suffix entry index in tail data.
	 */
	public boolean setData(int index, Object data) {
		index -= TAIL_START_BLOCKNO;
		if (index < num_tails) {
			tails[index].data = data;
			return true;
		}
		return false;
	}

	/**
	 * @brief Delete suffix entry
	 * 
	 * @param t
	 *            : the tail data
	 * @param index
	 *            : the index of the suffix to delete
	 * 
	 *            Delete suffix entry from the tail data.
	 */
	public void delete(int index) {
		freeBlock(index);
	}

	/**
	 * @brief Walk in tail with a string
	 * 
	 * @param t
	 *            : the tail data
	 * @param s
	 *            : the tail data index
	 * @param suffix_idx
	 *            : pointer to current character index in suffix
	 * @param str
	 *            : the string to use in walking
	 * @param len
	 *            : total characters in str to walk
	 * 
	 * @return total number of characters successfully walked
	 * 
	 *         Walk in the tail data t at entry s, from given character position
	 *         *suffix_idx, using len characters of given string str. On
	 *         return,*suffix_idx is updated to the position after the last
	 *         successful walk, and the function returns the total number of
	 *         character succesfully walked.
	 */
	int walkStr(int s, Short suffix_idx, int[] str, int len) {

		ListInt suffix = getSuffix(s);
		if (suffix == null || suffix.size() == 0)
			return 0;

		int i = 0;
		short j = suffix_idx;
		while (i < len) {
			if (str[i] != suffix.get(j))
				break;
			++i;
			/* stop and stay at null-terminator */
			if (0 == suffix.get(j))
				break;
			++j;
		}
		suffix_idx = j;
		return i;
	}

	/**
	 * @brief Walk in tail with a character
	 * 
	 * @param t
	 *            : the tail data
	 * @param s
	 *            : the tail data index
	 * @param suffix_idx
	 *            : pointer to current character index in suffix
	 * @param c
	 *            : the character to use in walking
	 * 
	 * @return true indicating success Walk in the tail data t at entry s, from
	 *         given character position suffix_idx, using given character c. If
	 *         the walk is successful, it returns true, and *suffix_idx is
	 *         updated to the next character. Otherwise, it returns false, and
	 *         *suffix_idx is left unchanged.
	 */
	public boolean walkChar(int s, IntState suffix_idx, int c) {
		ListInt suffix;
		int suffix_char;

		suffix = getSuffix(s);
		if (suffix == null || suffix.size() == 0)
			return false;

		suffix_char = suffix.get(suffix_idx.getState());
		if (suffix_char == c) {
			if (0 != suffix_char)
				suffix_idx.incOne();;
			return true;
		}
		return false;
	}

	public long getMemoryUsed(){
		long sum =  num_tails * 8;
		for(int i = 0; i < num_tails;i++){
			if(tails[i] != null){
				sum += 20;
				if(tails[i].suffix != null){
					sum += tails[i].suffix.capability() * 4;
				}
				
				if(tails[i].data != null){
					sum += 10; // average.
				}
			}
		}
		return sum;
	}
}
