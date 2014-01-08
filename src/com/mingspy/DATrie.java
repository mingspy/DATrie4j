package com.mingspy;

import java.util.TreeSet;

import com.mingspy.modles.Constants;
import com.mingspy.modles.DoubleArray;
import com.mingspy.modles.SearchResult;

public class DATrie<V> implements IDATrie<V> {

	/**
	 * for serialize.
	 */
	private static final long serialVersionUID = -4772138178626776718L;
	
	private DoubleArray<V> da = new DoubleArray<V>();
	private final int alphabetLength = 0x952f - 0x4e00 + 256;
	
	private int getCode(char c){
		if(c < 256){
			return c;
		}else if(c >= 0x4e00){
			return 256 + c - 0x4e00;
		}
		return -1;
	}
	private int [] getInnerCodes(String key){
		int [] ret = new int[key.length()];
		for(int i = 0; i < key.length(); i++){
			ret[i] = getCode(key.charAt(i));
		}
		return ret;
	}
	@Override
	public boolean add(String key, V value) {
		boolean changed = false;
		// Start from the root
		int state = 0;		// The current DFA state ordinal
		int transition = 0;	// The candidate for the transition end state
		int i = 0;			// The input string index
		int c = 0;			// The current input string character
		// For every input character
		int [] codes = getInnerCodes(key);
		while (i < codes.length) {
			assert state >= 0;
			int nextstate = da.getBase(state);
			assert nextstate >= 0;
			c = codes[i];
			// Calculate next hop. It is the base contents of the current state
			// plus the input character.
			transition = da.getBase(state) + c;
			assert transition > 0;
			da.ensureReachableIndex(transition);
			/* 
			 * If the next hop index is empty
			 * (-1), then simply add a new state of the DFA in that spot, with
			 * owner state the current state and next hop address the next available
			 * space.
			 */
			if (da.getCheck(transition) == Constants.EMPTY_VALUE ) {
				da.setCheck(transition, state);
				if (i == codes.length - 1) { 				// The string is done
					da.setBase(transition, Constants.LEAF_BASE_VALUE); 	// So this is a leaf
					da.setData(transition, value);
					changed = true;
				}
				else {
					da.setBase(transition, da.nextAvailableHop(codes[i + 1])); // Add a state
					changed = true;
				}
			}
			else if (da.getCheck(transition) != state) { // We have been through here before
				/*
				 * 
				 * The place we must add a new children state is already
				 * occupied. Move this state's base to a new location.
				 */
				resolveConflict(state, c);
				changed = true;
				// We must redo this character
				continue;
			}
			/*
			 * There is another case that is the default and always executed
			 * by the if above. That is simply transition through the DFA
			 * and advance the string index. This is done after we notify
			 * for the transition event.
			 */ 
			//updateInsert(state, i-1, key);
			state = transition;
			i++;
		}
		return changed;
	}

	/**
	 * This method is the most complex part of the algorithm.
	 * First of all, keep in mind that the children of a state
	 * are stored in ordered locations. That means that there is the possibility
	 * that although a new child for state s must be added, the position
	 * has already been taken. This is the conflict that is resolved here.
	 * There are two ways. One is to move the obstructing state to a new
	 * location and the other is to move the obstructed state. Here the
	 * latter is chosen. This also ensures that the root node is never moved.
	 * @param s The state to move
	 * @param newValue The value that causes the conflict.
	 */
	protected void resolveConflict(int s, int newValue) {

		// The set of children values
		TreeSet<Integer> values = new TreeSet<Integer>();

		// Add the value-to-add 
		values.add(new Integer(newValue));

		// Find all existing children and add them too.
		for (int c = 0; c < alphabetLength; c++) {
			int tempNext = da.getBase(s) + c;
			if (tempNext < da.getSize()&& tempNext >= 0 && da.getCheck(tempNext) == s)
				values.add(new Integer(c));
		}

		// Find a place to move them.
		int newLocation = da.nextAvailableMove(values);

		// newValue is not yet a child of s, so we should not check for it.
		values.remove(new Integer(newValue));
		
		/*
		 * This is where the job is done. For each child of s, 
		 */
		for (Integer value : values) {
			int c = value.intValue();		// The child state to move
			int tempNext = da.getBase(s) + c;	// 
			assert tempNext < da.getSize();
			assert da.getCheck(tempNext) == s;
			/*
			 * base(s)+c state is child of s.
			 * Mark new position as owned by s.
			 */
			assert da.getCheck(newLocation + c) == Constants.EMPTY_VALUE;
			da.setCheck(newLocation + c, s);

			/*
			 * Copy pointers to children for this child of s.
			 * Note that even if this child is a leaf, this is needed.
			 */
			assert da.getBase(newLocation + c) == Constants.EMPTY_VALUE;
			da.setBase(newLocation + c, da.getBase(tempNext));
			da.setData(newLocation + c, da.getData(tempNext));
			
			//updateChildMove(s, c, newLocation);
			/*
			 * Here the child c is moved, but not *its* children. They must be
			 * updated so that their check values point to the new position of their
			 * parent (i.e. c)
			 */
			if (da.getBase(da.getBase(s) + c) != Constants.LEAF_BASE_VALUE) {
				 // First, iterate over all possible children of c
				 for (int d = 0; d < alphabetLength; d++) {
					/*
					 *  Get the child. This could well be beyond the store size
					 *  since we don't know how many children c has.
					 */
					int tempNextChild = da.getBase(da.getBase(s) + c) + d;
					/* 
					 * Here we could also check if tempNext > 0, since
					 * negative values end the universe. However, since the
					 * implementation of nextAvailableHop never returns
					 * negative values, this should never happen. Presto, a
					 * nice way of catching bugs.
					 */
					if (tempNextChild < da.getSize() && da.getCheck(tempNextChild) == da.getBase(s) + c) {
						// Update its check value, so that it shows to the new position of this child of s.
						da.setCheck(da.getBase(da.getBase(s) + c) + d, newLocation + c);
					}
					else if (tempNextChild >= da.getSize()) {
						/*
						 *  Minor optimization here. If the above if fails then tempNextChild > check.size()
						 *  or the tempNextChild position is already owned by some other state. Remember
						 *  that children states are stored in increasing order (though not necessarily
						 *  right next to each other, since other states can be between the gaps they leave).
						 *  That means that failure of the second part of the conjuction of the if above
						 *  does not mean failure, since the next child can exist. Failure of the first conjuct
						 *  however means we are done, since all the rest of the children will only be further
						 *  down the store and therefore beyond its end also. Nothing left to do but break  
						 */
						break;
					}
				}
				// Finally, free the position held by this child of s
				da.setBase(da.getBase(s) + c, Constants.EMPTY_VALUE);
				da.setCheck(da.getBase(s) + c, Constants.EMPTY_VALUE);
				da.setData(da.getBase(s)+c , null);

			}
		}
		// Here, all children and grandchildren (if existent) of s have been
		// moved or updated. That which remains is for the state s to show
		// to its new children
		da.setBase(s, newLocation);
		//updateStateMove(s, newLocation);
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
			return da.getData(state.finishedAtState);
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
		int state		= 0; // The current DFA state ordinal
		int transition	= 0; // The candidate for the transition end state
		int i			= 0; // The input string index
		int current		= 0; // The current input character
		SearchState result = new SearchState();  // The search result
		result.prefix = prefix;
		result.result = SearchResult.PURE_PREFIX; // The default value
		// For every input character
		int [] codes = getInnerCodes(prefix);
		while (i < codes.length) {
			current = codes[i];
			assert current >= 0;
			assert current < alphabetLength;
			transition = da.getBase(state) + current;	// Get next candidate state
			if (transition < da.getSize() && da.getCheck(transition) == state) {	// If it is valid...
				if (da.getBase(transition) == Constants.LEAF_BASE_VALUE || da.getData(transition) != null) {
				// We reached a leaf. There are two possibilities:
					if (i == codes.length - 1) {
						// The string has been exhausted. Return perfect match 
						result.result = SearchResult.PERFECT_MATCH;
						break;
					} else if(da.getBase(transition) == Constants.LEAF_BASE_VALUE) {
						// The string still has more to go. Return not found.
						result.result = SearchResult.NOT_FOUND;
						break;
					}
				}
				state = transition; //  ...switch and continue
			}
			else {
				// The candidate does not belong to the current state. Not found.
				result.result = SearchResult.NOT_FOUND;
				break;
			}
			//updateSearch(state, i, prefix);
			i++;
		}
		//updateSearch(state, i, prefix);
		result.finishedAtState = transition;
		result.index = i;
		return result;
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
