package com.mingspy.dat;

import com.mingspy.dat.modles.DoubleArray;

public class DATrie<V> implements IDATrie<V> {

	/**
	 * for serialize.
	 */
	private static final long serialVersionUID = -4772138178626776718L;
	
	private DoubleArray<V> da = new DoubleArray<V>();

	@Override
	public boolean add(String key, V value) {
		boolean changed = false;
		
		return changed;
	}


	@Override
	public boolean remove(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V find(String key) {
		SearchState state = runPrefix(key);
		if(state.result == SearchResult.PERFECT_MATCH){
			//return da.getData(state.finishedAtState);
		}
		return null;
	}


	@Override
	public SearchResult containsPrefix(String prefix) {
		return runPrefix(prefix).result;
	}

	/**
	 * This method, at its core, walks a path on the trie. Given a string, it
	 * decides whether it is contained as a prefix of other strings, if it is
	 * contained as a standalone string or if it is not present. Particularly:
	 * <li>If the string is contained but has not been inserted</li>
	 * 
	 * @param prefix The string to walk on the trie
	 * @return The result of the search
	 */
	protected SearchState runPrefix(String prefix) {
		
		return null;
	}
	
	/**
	 * Utility class to represent the necessary state after the end
	 * of a search. The walking algorithm besides deciding on the
	 * search result outcome is also useful to find the last valid
	 * index of an input string. This class represents just that.
	 */
	protected static class SearchState {
		/**
		 * The searched for string
		 */
		protected String prefix;

		/**
		 * The index within the prefix string that the search ended.
		 * If it was exhausted without reaching a leaf node it is
		 * equal to prefix.size()
		 */
		protected int index;

		/**
		 * The index in the base array of the state at which the
		 * walking algorithm concluded.
		 */
		protected int finishedAtState;

		/**
		 * The result of the search. It is also reproducible by
		 * the other fields of this class.
		 */
		protected SearchResult result;
	}

	public static void main(String[] args) {
		DATrie<String> dat = new DATrie<String>();
		//dat.add("123", "123");
		//dat.add("1234", "1234");
		//dat.add("1235", "1235");
		dat.add("中国", "中国");
		dat.add("中国人", "中国人");
		System.out.println(dat.find("123"));
		System.out.println(dat.find("1234"));
		System.out.println(dat.find("1235"));
		System.out.println(dat.find("中国"));
		System.out.println(dat.find("中国人"));
		dat.add("123", "321");
		System.out.println(dat.find("123"));
	}

}
