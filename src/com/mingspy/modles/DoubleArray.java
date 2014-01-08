package com.mingspy.modles;

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


public class DoubleArray<V> implements Serializable{

	private static final int TRIE_INDEX_ERROR = 0;
	private static final int DA_ROOT = 2;  // DA的根地址
	private static final int DA_POOL_BEGIN = 3;
	private static final int DA_SIGNATURE = 0xabcdef0;
	private static final long serialVersionUID = 4112653922277428101L;
	private int[] base;
	private int[] check;
	private Object [] data;

	private int size;
	public DoubleArray() {
		
	}

	public void init(){
		base = new int[DA_POOL_BEGIN];
		check = new int[DA_POOL_BEGIN];
		data = new Object[DA_POOL_BEGIN];
		base[0] = DA_SIGNATURE;   // 设置标记
	    check[0] = DA_POOL_BEGIN; // 设置单元数组大小
	    base[1] = -1;             // 设置
	    check[1] = -1;			  // 设置起始空闲cell地址
	    base[2] = DA_POOL_BEGIN;  // 根地址设置起始状态为DA_POOL_BEGIN;
	    check[2] = 0;			  // 
	}
	public int getBase(int position) {
		return position < base.length?base[position]:TRIE_INDEX_ERROR;
	}

	public int getCheck(int position) {
		return position < check.length?check[position]:TRIE_INDEX_ERROR;
	}

	
	public void setBase(int position, int value) {
		base[position] = value;
	}


	public void setCheck(int position, int value) {
		check[position]= value;
	}

	public V getData(int index){
		return index < data.length?(V)data[index]:null;
	}
	
	public void setData(int position, V value){
		data[position] = value;
	}
	
	public int getSize() {
		return size;
	}
	
	public void ensureReachableIndex(int limit) {
		while (getSize() <= limit) {
			/*
			 * In essence, we let all enlargement operations to the implementing
			 * class of the backing store. Since this currently is a ArrayList,
			 * simply adding values until we are done will work.
			 */
			base.add(Constants.EMPTY_VALUE);
			check.add(Constants.EMPTY_VALUE);
			data.add(null);
		}
	}

	public  int nextAvailableHop(int forValue) {

		Integer value = new Integer(forValue);
		/*
		 * First we make sure that there exists a free location that is
		 * strictly greater than the value.
		 */
		while (freePositions.higher(value) == null) {
			ensureReachableIndex(base.size() + 1); // This adds to the freePositions store
		}
		
		/*
		 * From the termination condition of the loop above, the next line
		 * CANNOT throw NullPointerException
		 * Note that we return the position minus the value. That is because
		 * the result is the ordinal of the new state which is translated
		 * to a store index. Therefore, since we add the value to the base
		 * to find the next state, here we must subtract.
		 */
		int result = freePositions.higher(value).intValue() - forValue;
		// This assertion must pass thanks to the loop above
		assert result >= 0;
		return result;
	}

	public int nextAvailableMove(SortedSet<Integer> values) {
		// In the case of a single child, the problem is solved.
		if (values.size() == 1) {
			return nextAvailableHop(values.first());
		}

		int minValue = values.first();
		int maxValue = values.last();
		int neededPositions = maxValue - minValue + 1;

		int possible = findConsecutiveFree(neededPositions);
		if (possible - minValue >= 0) {
			return possible - minValue;
		}

		ensureReachableIndex(base.size() + neededPositions);
		return base.size() - neededPositions - minValue;
	}
	
	/**
	 * Finds consecutive free positions in the trie.
	 * 
	 * @param amount
	 *            How many consecutive positions are needed.
	 * @return The index of the first position in the group, or -1 if
	 *         unsuccessful.
	 */
	private int findConsecutiveFree(int amount) {

		assert amount >= 0;
		/*
		 * Quick way out, that also ensures the invariants
		 * of the main loop.
		 */
		if (freePositions.isEmpty()) {
			return -1;
		}

		Iterator<Integer> it = freePositions.iterator();
		Integer from; 		// The location from where the positions begin
		Integer current;	// The next integer in the set
		Integer previous;	// The previously checked index
		int consecutive;	// How many consecutive positions have we seen so far 
		
		from = it.next();	// Guaranteed to succeed, from the if at the start
		previous = from;	// The first previous is the first in the series
		consecutive = 1;	// 1, since from is a valid location
		while(consecutive < amount && it.hasNext()) {
			current = it.next();
			if (current - previous == 1) {
				previous = current;
				consecutive++;
			}
			else {
				from = current;
				previous = from;
				consecutive = 1;
			}
		}
		if (consecutive == amount) {
			return from;
		}
		else {
			return -1;
		}
	}
	public static void main(String[] args) {
		String str = "123";
		System.out.println(str.length());
		System.out.println(str.charAt(0));
		int x = str.charAt(0);
		System.out.println(x);
	}
}
