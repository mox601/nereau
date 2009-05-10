package model.queryexpansion.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import model.queryexpansion.QueryExpander;

import org.junit.Before;
import org.junit.Test;


public class QueryExpanderTest {
	
	private HashMap<String, Double> a;
	private HashMap<String, Double> b;
	
	@Before
	public void setUp() {
		a = new HashMap<String, Double>();
		String key1 = "a";
		String key2 = "b";
		String key3 = "c";
		Double value1 = new Double(1.0);
		Double value2 = new Double(2.0);
		Double value3 = new Double(3.0);
		a.put(key1, value1);
		a.put(key2, value2);
		a.put(key3, value3);
		
		
		b = new HashMap<String, Double>();
		String key4 = "b";
		String key5 = "e";
		String key6 = "c";
		Double value4 = new Double(1.5);
		Double value5 = new Double(2.5);
		Double value6 = new Double(3.5);
		b.put(key4, value4);
		b.put(key5, value5);
		b.put(key6, value6);
		
		
	}
	
	@Test
	public void testMergeMaps() {
		QueryExpander qExp = new QueryExpander();
		HashMap<String, Double> result = new HashMap<String, Double>();
		//before
		System.out.println(a);
		System.out.println(b);
		
		
		result = qExp.mergeMaps(a, b);
		a = result;
		//after
		System.out.println(result);
		System.out.println(a);
	}

}
