/*
  Abstract file system manipulations
  Copyright (C) 2022-2025 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.fs;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link FilePath}.
 */
public class FilePathTest
{
	@Test
	public void testSimplify1()
	{
		List<String> parts = getList("foo", "bar", "baz");
		List<String> simplified = FilePath.simplify(parts);
		equalLists(getList("foo", "bar", "baz"), simplified);
	}
	
	@Test
	public void testSimplify2()
	{
		List<String> parts = getList("foo", "bar", "..");
		List<String> simplified = FilePath.simplify(parts);
		equalLists(getList("foo"), simplified);
	}
	
	@Test
	public void testSimplify3()
	{
		List<String> parts = getList("foo", "..", "baz");
		List<String> simplified = FilePath.simplify(parts);
		equalLists(getList("baz"), simplified);
	}
	
	@Test
	public void testSimplify4()
	{
		List<String> parts = getList("foo", "..", "..");
		List<String> simplified = FilePath.simplify(parts);
		equalLists(getList(".."), simplified);
	}
	
	@Test
	public void testSimplify5()
	{
		List<String> parts = getList("..", "foo", "bar");
		List<String> simplified = FilePath.simplify(parts);
		equalLists(getList("..", "foo", "bar"), simplified);
	}
	
	@Test
	public void testChdir1()
	{
		FilePath fp1 = new FilePath("foo/bar");
		FilePath fp2 = fp1.chdir("baz");
		assertEquals("foo/bar/baz", fp2.toString());
	}
	
	@Test
	public void testChdir2()
	{
		FilePath fp1 = new FilePath("foo/bar");
		FilePath fp2 = fp1.chdir("../baz");
		assertEquals("foo/baz", fp2.toString());
	}
	
	@Test
	public void testChdir3()
	{
		FilePath fp1 = new FilePath("/foo/bar");
		FilePath fp2 = fp1.chdir("../baz");
		assertEquals("/foo/baz", fp2.toString());
	}
	
	@Test
	public void testChdir4()
	{
		FilePath fp1 = new FilePath("/foo/bar");
		FilePath fp2 = fp1.chdir("/baz");
		assertEquals("/baz", fp2.toString());
	}
	
	@Test
	public void testChdir5()
	{
		FilePath fp1 = new FilePath(".");
		FilePath fp2 = fp1.chdir("../baz");
		assertEquals("../baz", fp2.toString());
	}
	
	@Test
	public void testChdir6()
	{
		FilePath fp1 = new FilePath("/home/user");
		FilePath fp2 = fp1.chdir("../baz");
		assertEquals("/home/baz", fp2.toString());
	}
	
	@Test
	public void testChdir7()
	{
		FilePath fp1 = new FilePath("/home/user");
		FilePath fp2 = fp1.chdir("/tmp/baz");
		assertEquals("/tmp/baz", fp2.toString());
	}
	
	protected static List<String> getList(String ... elements)
	{
		return Arrays.asList(elements);
	}
	
	protected static void equalLists(List<String> list1, List<String> list2)
	{
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++)
		{
			assertEquals(list1.get(i), list2.get(i));
		}
	}
}
