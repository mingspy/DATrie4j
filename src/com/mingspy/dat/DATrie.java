package com.mingspy.dat;

import java.util.Arrays;

import com.mingspy.alpha.AlphabetFactory;
import com.mingspy.array.ArrayListIntFactory;
import com.mingspy.array.ListInt;
import com.mingspy.dat.modles.Constants;
import com.mingspy.dat.modles.DoubleArray;
import com.mingspy.dat.modles.IntState;
import com.mingspy.dat.modles.Tail;

public class DATrie<V> implements IDATrie<V> {

	/**
	 * for serialize.
	 */
	private static final long serialVersionUID = -4772138178626776718L;

	private DoubleArray da = new DoubleArray();
	private Tail tail = new Tail();
	private boolean is_dirty = false;

	@Override
	public boolean add(String key, V value) {
		if (key == null)
			return false;
		int[] codes = AlphabetFactory.getAlphabet().getInnerCodes(key);
		return store(codes, value);
	}

	@Override
	public boolean remove(String key) {
		if(key == null) return false;
		int[] codes = AlphabetFactory.getAlphabet().getInnerCodes(key);
		return delete(codes);
	}

	@Override
	public V find(String key) {
		if(key == null) return null;
		int[] codes = AlphabetFactory.getAlphabet().getInnerCodes(key);
		return retrieve(codes);
	}

	@Override
	public boolean containsPrefix(String prefix) {
		if(prefix == null) return false;
		int[] codes = AlphabetFactory.getAlphabet().getInnerCodes(prefix);
		return walkthrough(codes);
	}

	private boolean isSeparate(int s) {
		return da.getBase(s) < 0;
	}

	private int getTailIndex(int s) {
		return -da.getBase(s);
	}

	private boolean walkthrough(int [] key){
		int p;
		/* walk through branches */
		IntState s = new IntState(da.getRoot());
		int i = 0;
		for (; i < key.length && !isSeparate(s.getState()); i++) {
			p = key[i];
			if (0 == p)
				return true;
			if (!da.walk(s, p)) {
				return false;
			}
			
		}

		/* walk through tail */
		s.setState( getTailIndex(s.getState()));
		IntState suffix_idx = new IntState((short) 0);
		for (; i < key.length; i++) {
			p = key[i];
			if (0 == p)
				return true;
			
			if (!tail.walkChar(s.getState(), suffix_idx, p)) {
				return false;
			}
			
		}
		
		return true;
	}
	/**
	 * @brief Retrieve an entry from trie
	 * 
	 * @param trie
	 *            : the trie
	 * @param key
	 *            : the key for the entry to retrieve
	 * @param o_data
	 *            : the storage for storing the entry data on return
	 * 
	 * @return boolean value indicating the existence of the entry.
	 *         Retrieve an entry for the given key from trie. On return, if key
	 *         is found and o_data is not NULL, *o_data is set to the data
	 *         associated to key.
	 */
	private V retrieve(int[] key) {
		return retrieve(key,0,key.length);
	}
	
	public V retrieve(int [] keys, int start, int end){
		int p;
		/* walk through branches */
		IntState s = new IntState(da.getRoot());
		int i = start;
		for (; i < end && !isSeparate(s.getState()); i++) {
			p = keys[i];
			if (!da.walk(s, p)) {
				return null;
			}
			if (0 == p)
				break;
		}

		/* walk through tail */
		s.setState(getTailIndex(s.getState()));
		IntState suffix_idx = new IntState(0);
		for (; i < end; i++) {
			p = keys[i];
			if (!tail.walkChar(s.getState(), suffix_idx, p)) {
				return null;
			}
			if (0 == p)
				break;
		}

		/* found, set the val and return */
		return (V) tail.getData(s.getState());
	}

	/**
	 * @brief Store a value for an entry to trie
	 * 
	 * @param trie
	 *            : the trie
	 * @param key
	 *            : the key for the entry to retrieve
	 * @param data
	 *            : the data associated to the entry
	 * 
	 * @return boolean value indicating the success of the process Store a data
	 *         for the given key in trie. If key does not exist in trie, it will
	 *         be appended. If it does, its current data will be overwritten.
	 */
	private boolean store(int[] key, V data) {
		return storeConditionally(key, data, true);
	}

	/**
	 * @brief Store a value for an entry to trie only if the key is not present
	 * 
	 * @param trie
	 *            : the trie
	 * @param key
	 *            : the key for the entry to retrieve
	 * @param data
	 *            : the data associated to the entry
	 * 
	 * @return boolean value indicating the success of the process Store a data
	 *         for the given key in trie. If key does not exist in trie, it will
	 *         be appended. If it does, the function will return failure and the
	 *         existing value will not be touched. This can be useful for
	 *         multi-thread applications, as race condition can be avoided.
	 *         Available since: 0.2.4
	 */
	private boolean storeIfAbsent(int[] key, V data) {
		return storeConditionally(key, data, false);
	}

	private boolean storeConditionally(int[] key, V data, boolean is_overwrite) {
		int t;
		int p, sep;

		/* walk through branches */
		IntState s = new IntState(da.getRoot());
		int i = 0;
		for (; i < key.length && !isSeparate(s.getState()); i++) {
			p = key[i];
			if (!da.walk(s, p)) {
				int[] key_str = Arrays.copyOfRange(key, i, key.length);
				return branchInBranch(s.getState(), key_str, data);
			}
			if (0 == p)
				break;
		}

		/* walk through tail */
		sep = i;
		t = getTailIndex(s.getState());
		IntState suffix_idx = new IntState( 0);
		for (; i < key.length; i++) {
			p = key[i];
			if (!tail.walkChar(t, suffix_idx, p)) {
				int[] tail_str = Arrays.copyOfRange(key, sep, key.length);
				return branchInTail(s.getState(), tail_str, data);

			}
			if (0 == p)
				break;
		}

		/* duplicated key, overwrite val if flagged */
		if (!is_overwrite) {
			return false;
		}
		tail.setData(t, data);
		is_dirty = true;
		return true;
	}

	private void setTailIndex(int s, int v) {
		da.setBase(s, -v);
	}

	private boolean branchInBranch(int sep_node, int[] suffix, Object data) {
		int new_da, new_tail;

		int i = 0;
		new_da = da.insertBranch(sep_node, suffix[i]);
		if (Constants.TRIE_INDEX_ERROR == new_da)
			return false;

		if (0 != suffix[i])
			++i;

		ListInt new_suffix = ArrayListIntFactory.newListInt(suffix, i);
		new_tail = tail.addSuffix(new_suffix);
		tail.setData(new_tail, data);
		setTailIndex(new_da, new_tail);

		is_dirty = true;
		return true;
	}

	private boolean branchInTail(int sep_node, int[] suffix, Object data) {

		/* adjust separate point in old path */
		int old_tail = getTailIndex(sep_node);
		ListInt old_suffix = tail.getSuffix(old_tail);
		if (old_suffix == null)
			return false;
		int i = 0;
		int s = sep_node;
		for (; old_suffix.get(i) == suffix[i]; i++) {
			int t = da.insertBranch(s, suffix[i]);
			if (Constants.TRIE_INDEX_ERROR == t) {
				da.pruneUpto(sep_node, s);
				setTailIndex(sep_node, old_tail);
				return false;
			}
			s = t;
		}

		int p = old_suffix.get(i);
		int old_da = da.insertBranch(s, p);
		if (Constants.TRIE_INDEX_ERROR == old_da) {
			da.pruneUpto(sep_node, s);
			setTailIndex(sep_node, old_tail);
			return false;
		}

		// when run here, all equals prefix are added.
		// save old left suffix.
		if (0 != p)
			++i;
		tail.setSuffix(old_tail, old_suffix.subList(i));
		setTailIndex(old_da, old_tail);

		// insert new added suffix to state s.
		if(i == suffix.length){
			i --;
		}
		
		int [] new_suffix = Arrays.copyOfRange(suffix, i, suffix.length);
		/* insert the new branch at the new separate point */
		return branchInBranch(s, new_suffix, data);

	}

	/**
	 * @brief Delete an entry from trie
	 * 
	 * @param trie
	 *            : the trie
	 * @param key
	 *            : the key for the entry to delete
	 * 
	 * @return boolean value indicating whether the key exists and is removed
	 *         Delete an entry for the given key from trie.
	 */
	private boolean delete(int[] key) {
		int t;

		/* walk through branches */
		IntState s = new IntState(da.getRoot());
		int i = 0;
		int p;
		for (; i < key.length && !isSeparate(s.getState()); i++) {
			p = key[i];
			if (!da.walk(s, p)) {
				return false;
			}
			if (0 == p)
				break;
		}

		/* walk through tail */
		t = getTailIndex(s.getState());
		IntState suffix_idx = new IntState( 0);
		for (; i < key.length; i++) {
			p = key[i];
			if (!tail.walkChar(t, suffix_idx, p)) {
				return false;
			}
			if (0 == p)
				break;
		}

		tail.delete(t);
		da.setBase(s.getState(), Constants.TRIE_INDEX_ERROR);
		da.prune(s.getState());

		is_dirty = true;
		return true;
	}

}
