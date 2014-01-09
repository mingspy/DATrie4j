package test;

import junit.framework.TestCase;

import com.mingspy.dat.DATrie;
import com.mingspy.dat.IDATrie;

public class TestTrie extends TestCase{
	public void testAdd(){
		IDATrie<String> trie = new DATrie<String>();
		trie.add("12", "12");
		trie.add("1", "1");
		trie.add("13", "13");
		trie.add("13", "13");
		trie.add("123", "123");
		trie.add("1478", "1478");
		assertEquals("12", trie.find("12"));
		assertEquals("1", trie.find("1"));
		assertEquals("123", trie.find("123"));
		assertTrue(trie.containsPrefix("147"));
		assertTrue(trie.containsPrefix("1478"));
		assertTrue(!trie.containsPrefix("154"));
		assertTrue(trie.containsPrefix("12"));
		trie.add("1", "1111");
		assertEquals("1111", trie.find("1"));	
	}
	
	public void testDelete(){
		IDATrie<String> trie = new DATrie<String>();
		trie.add("12", "12");
		assertEquals("12", trie.find("12"));
		trie.remove("12");
		assertTrue(!trie.containsPrefix("12"));
		assertTrue(!trie.containsPrefix("1"));
		assertTrue(trie.add("12", "12"));
		assertTrue(trie.add("1245", "1245"));
		//trie.add("123", "12");
		assertTrue(trie.remove("12"));
		assertTrue(trie.containsPrefix("12"));
		assertTrue(trie.containsPrefix("1"));
		//assertTrue(trie.containsPrefix("124"));
		assertTrue(trie.containsPrefix("1245"));
	}

	public void testChinese(){
		IDATrie<String> trie = new DATrie<String>();
		trie.add("中华", "中华");
		trie.add("中华人民", "中华人民");
		trie.add("中华人民共和国", "中华人民共和国");
		trie.add("中央人民广播电台", "in china");
		trie.add("xyz中央人民广播电台", "in china");
		assertEquals("中华", trie.find("中华"));
		assertEquals("中华人民", trie.find("中华人民"));
		assertEquals("中华人民共和国", trie.find("中华人民共和国"));
		assertEquals("in china", trie.find("xyz中央人民广播电台"));
		trie.remove("12");
		assertTrue(!trie.containsPrefix("12"));
		assertTrue(!trie.containsPrefix("1"));
	}
	
	public void testSpace(){
		
	}
}
