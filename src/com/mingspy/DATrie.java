package com.mingspy;

public abstract interface DATrie<K,V> {
	public boolean add(K key, V value);
	public boolean remove(K key);
	public V find(K key);
	public boolean containsPrefix(K key);
	public boolean unserialize(String path);
	public boolean serialize(String path);
}
