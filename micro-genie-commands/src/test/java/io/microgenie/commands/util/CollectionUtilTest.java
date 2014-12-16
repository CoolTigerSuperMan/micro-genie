package io.microgenie.commands.util;



import io.microgenie.commands.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;



public class CollectionUtilTest {

	
	
	/**
	 * isNullOrEmpty returns true if list is null
	 */
	@Test
	public void shouldReturnTrueIfCollectionIsNull(){
		List<String> nullList = null;
		Assert.assertTrue(CollectionUtil.isNullOrEmpty(nullList));
	}
	
	
	/**
	 * isNullOrEmpty returns true if list is empty
	 */
	@Test
	public void shouldReturnTrueIfCollectionIsEmpty(){
		List<String> emptyList = new ArrayList<String>();
		Assert.assertTrue(CollectionUtil.isNullOrEmpty(emptyList));
	}
	
	
	/**
	 * isNullOrEmpty returns false if list is initialized but empty
	 */
	@Test
	public void shouldReturnFalseIfCollectionHasAnElement(){
		List<String> list = new ArrayList<String>();
		list.add("one");
		Assert.assertFalse(CollectionUtil.isNullOrEmpty(list));
	}
	
	

	/**
	 * hasElements returns true if list has an element
	 */
	@Test
	public void shouldReturnTrueIfCollectionHasElements(){
		List<String> listWithElemements = new ArrayList<String>();
		listWithElemements.add("1");
		Assert.assertTrue(CollectionUtil.hasElements(listWithElemements));
	}
	
	
	/**
	 * hasElements returns false if initialized list has no elements
	 */
	@Test
	public void shouldReturnFalseIfCollectionIsEmpty(){
		List<String> emptyList = new ArrayList<String>();
		Assert.assertFalse(CollectionUtil.hasElements(emptyList));
	}
	
	
}
