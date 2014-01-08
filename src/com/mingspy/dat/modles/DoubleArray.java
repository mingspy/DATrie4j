package com.mingspy.dat.modles;

import java.io.Serializable;
import java.util.Arrays;

import com.mingspy.alpha.AlphabetFactory;
import com.mingspy.array.ArrayListIntFactory;
import com.mingspy.array.ListInt;

public class DoubleArray<V> implements Serializable {

	private static final int TRIE_INDEX_ERROR = 0; 
	private static final int DA_ROOT = 2; // root address
	private static final int DA_POOL_BEGIN = 3; // 
	private static final int DA_SIGNATURE = 0xabcdef0;
	private static final long serialVersionUID = 4112653922277428101L;
	/**
	 * The address of free list header.<br>
	 * As r1r2...ri...rm are m free cells.<br>
	 * base[1] = -rm<br>
	 * check[1] = -r1<br>
	 * .....<br>
	 * base[ri]= -r(i-1)<br>
	 * check[ri] = -r(i+1)<br>
	 * .....<br>
	 * base[rm]= -r(m-1)<br>
	 * check[ri] = -1<br>
	 */
	private static final int FREE_LIST_HEAD = 1; //
	
	// How many cell it call allocate.
	private static final int TRIE_INDEX_MAX = 0x0fffffff;

	private int[] base;
	private int[] check;
	//private Object[] data;

	public DoubleArray() {
		init();
	}

	public void init() {
		base = new int[DA_POOL_BEGIN];
		check = new int[DA_POOL_BEGIN];
		//data = new Object[DA_POOL_BEGIN];
		base[0] = DA_SIGNATURE;   // mark of datrie
		check[0] = DA_POOL_BEGIN; // number of cells
		base[1] = -1; // address of last free cell 
		check[1] = -1; // address of first free cell
		base[2] = DA_POOL_BEGIN; // set root's base at DA_POOL_BEGIN
		check[2] = 0; //
	}

	public int getRoot(){
		return DA_ROOT;
	}
	
	private int getBase(int position) {
		return position < base.length ? base[position] : TRIE_INDEX_ERROR;
	}

	private int getCheck(int position) {
		return position < check.length ? check[position] : TRIE_INDEX_ERROR;
	}

	private void setBase(int position, int value) {
		base[position] = value;
	}

	private void setCheck(int position, int value) {
		check[position] = value;
	}

//	private V getData(int index) {
//		return index < data.length ? (V) data[index] : null;
//	}
//
//	private void setData(int position, V value) {
//		data[position] = value;
//	}

	public boolean isWorkable(int s, int c) {
		return getCheck(getBase(s) + c) == s;
	}

	/**
	 * Walk the double-array trie from state s, using input character c. If
	 * there exists an edge from s with arc labeled c, this function returns
	 * TRUE and s is updated to the new state. Otherwise, it returns FALSE and s
	 * is left unchanged.
	 * 
	 * @param s
	 * @param c
	 * @return
	 */
	public boolean walk(Integer s, int c) {
		int next;
		next = getBase(s) + c;
		if (getCheck(next) == s) {
			s = next;
			return true;
		}
		return false;
	}

	private void allocCell(int index) {

		int prev, next;

		prev = -getBase(index);
		next = -getCheck(index);

		/* remove the cell from free list */
		setCheck(prev, -next);
		setBase(next, -prev);

	}

	private void freeCell(int cell) {
		int i, prev;

		/* find insertion point */
		i = -getCheck(FREE_LIST_HEAD);
		while (i != FREE_LIST_HEAD && i < cell)
			i = -getCheck(i);

		prev = -getBase(i);

		/* insert cell before i */
		setCheck(cell, -i);
		setBase(cell, -prev);
		setCheck(prev, -cell);
		setBase(i, -cell);
	}

	private boolean extendPool(int to_index) {
		int new_begin;
		int i;
		int free_tail;

		if (to_index <= 0 || TRIE_INDEX_MAX <= to_index)
			return false;

		if (to_index < base.length)
			return true;
		new_begin = base.length;
		base = Arrays.copyOf(base, to_index + 1);
		check = Arrays.copyOf(check, to_index + 1);
		//data = Arrays.copyOf(data, to_index + 1);

		/* initialize new free list */
		for (i = new_begin; i < to_index; i++) {
			setCheck(i, -(i + 1));
			setBase(i + 1, -i);
		}

		/* merge the new circular list to the old */
		free_tail = -getBase(FREE_LIST_HEAD);
		setCheck(free_tail, -new_begin);
		setBase(new_begin, -free_tail);
		setCheck(to_index, -FREE_LIST_HEAD);
		setBase(FREE_LIST_HEAD, -to_index);

		/* update header cell */
		check[0] = to_index + 1;

		return true;
	}

	public boolean hasChildren(int s) {
		int base;
		int c, max_c;

		base = getBase(s);
		if (TRIE_INDEX_ERROR == base || base < 0)
			return false;

		max_c = Math.min(AlphabetFactory.getAlphabet().subAlphLength(s),
				TRIE_INDEX_MAX - base);
		for (c = 0; c < max_c; c++) {
			if (getCheck(base + c) == s)
				return true;
		}

		return false;
	}

	private boolean checkFreeCell(int s) {
		return extendPool(s) && getCheck(s) < 0;
	}

	/**
	 * @brief Insert a branch from trie node
	 * 
	 * @param s
	 *            : the state to add branch to
	 * @param c
	 *            : the character for the branch label
	 * 
	 * @return the index of the new node
	 * 
	 *         Insert a new arc labelled with character @a c from the trie node
	 *         represented by index @a s in double-array structure @a d. Note
	 *         that it assumes that no such arc exists before inserting.
	 */
	public int insertBranch(int s, int c) {
		int base, next;
		base = getBase(s);

		if (base > 0) {
			next = base + c;
			/* if already there, do not actually insert */
			if (getCheck(next) == s)
				return next;

			/*
			 * if (base + c) > TRIE_INDEX_MAX which means 'next' is overflow, or
			 * cell [next] is not free, relocate to a free slot
			 */
			if (base > TRIE_INDEX_MAX - c || !checkFreeCell(next)) {

				int new_base;
				/* relocate BASE[s] */
				ListInt symbols = findAllChildren(s);
				symbols.add(c);
				new_base = findFreeBase(symbols);
				symbols = null;

				if (TRIE_INDEX_ERROR == new_base)
					return TRIE_INDEX_ERROR;

				relocateBase(s, new_base);
				next = new_base + c;
			}
		} else {
			int new_base;
			ListInt symbols = ArrayListIntFactory.instance().newListInt();
			symbols.add(c);
			new_base = findFreeBase(symbols);
			symbols = null;

			if (TRIE_INDEX_ERROR == new_base)
				return TRIE_INDEX_ERROR;

			setBase(s, new_base);
			next = new_base + c;
		}
		allocCell(next);
		setCheck(next, s);

		return next;
	}

	private void relocateBase(int s, int new_base) {
		int old_base;
		int i;

		old_base = getBase(s);
		ListInt symbols = findAllChildren(s);

		for (i = 0; i < symbols.size(); i++) {
			int old_next = old_base + symbols.get(i);
			int new_next = new_base + symbols.get(i);
			int old_next_base = getBase(old_next);

			/* allocate new next node and copy BASE value */
			allocCell(new_next);
			setCheck(new_next, s); // set new state as children of s.
			setBase(new_next, old_next_base); // save old next state to new one.

			/*
			 * old_next node is now moved to new_next so, all cells belonging to
			 * old_next must be given to new_next
			 */
			/* preventing the case of TAIL pointer */
			if (old_next_base > 0) {
				int c, max_c;

				max_c = Math.min(
						AlphabetFactory.getAlphabet().subAlphLength(
								old_next_base), TRIE_INDEX_MAX - old_next_base);
				for (c = 0; c < max_c; c++) {
					if (getCheck(old_next_base + c) == old_next)
						setCheck(old_next_base + c, new_next);
				}
			}

			/* free old_next node */
			freeCell(old_next);
		}

		symbols = null;

		/* finally, make BASE[s] point to new_base */
		setBase(s, new_base);
	}

	private int findFreeBase(ListInt children) {
		int first_child;
		

		/* find first free cell that is beyond the first symbol */
		first_child = children.get(0);
		int s = -getCheck(FREE_LIST_HEAD);
		while (s != FREE_LIST_HEAD && s < (int) first_child + DA_POOL_BEGIN) {
			s = -getCheck(s);
		}
		if (s == FREE_LIST_HEAD) {
			for (s = first_child + DA_POOL_BEGIN;; ++s) {
				if (!extendPool(s))
					return TRIE_INDEX_ERROR;
				if (getCheck(s) < 0)
					break;
			}
		}

		/* search for next free cell that fits the symbols set */
		while (!fitAllChildren(s - first_child, children)) {
			/* extend pool before getting exhausted */
			if (-getCheck(s) == FREE_LIST_HEAD) {
				if (!extendPool(base.length))
					return TRIE_INDEX_ERROR;
			}

			s = -getCheck(s);
		}

		return s - first_child;

	}

	/**
	 * Check if the base can save all children.
	 * @param base
	 * @param children
	 * @return
	 */
	private boolean fitAllChildren(int base, ListInt children) {
		int i;
		for (i = 0; i < children.size(); i++) {
			int sym = children.get(i);

			/*
			 * if (base + sym) > TRIE_INDEX_MAX which means it's overflow, or
			 * cell [base + sym] is not free, the symbol is not fit.
			 */
			if (base > TRIE_INDEX_MAX - sym || !checkFreeCell(base + sym))
				return false;
		}
		return true;
	}

	/**
	 * Find all children of s.<br>
	 * @param s
	 * @return
	 */
	private ListInt findAllChildren(int s) {
		int base;
		int c, max_c;

		ListInt children = ArrayListIntFactory.instance().newListInt();
		base = getBase(s);
		max_c = Math.min(AlphabetFactory.getAlphabet().subAlphLength(base),
				TRIE_INDEX_MAX - base);
		for (c = 0; c < max_c; c++) {
			if (getCheck(base + c) == s)
				children.add(c);
		}

		return children;
	}
}
