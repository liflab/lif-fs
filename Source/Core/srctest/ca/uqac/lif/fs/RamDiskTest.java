/*
  Abstract file system manipulations
  Copyright (C) 2022 Sylvain Hallé

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

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link RamDisk}.
 */
public class RamDiskTest
{
	@Test
	public void testNavigation1() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		fs.chdir("foo");
		assertEquals("/foo", fs.pwd());
		fs.chdir("bar");
		assertEquals("/foo/bar", fs.pwd());
		fs.chdir("../baz");
		assertEquals("/foo/baz", fs.pwd());
		fs.close();
	}
	
	@Test
	public void testWrite1() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("/foo/bar/a.txt"));
		ps.println("Foobar");
		ps.close();
		assertTrue(fs.isFile("/foo/bar/a.txt"));
		assertFalse(fs.isDirectory("/foo/bar/a.txt"));
		fs.close();
	}
	
	@Test
	public void testDelete1() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("/foo/bar/a.txt"));
		ps.println("Foobar");
		ps.close();
		assertTrue(fs.isFile("/foo/bar/a.txt"));
		assertFalse(fs.isDirectory("/foo/bar/a.txt"));
		fs.delete("/foo/bar/a.txt");
		assertFalse(fs.isFile("/foo/bar/a.txt"));
		assertFalse(fs.isDirectory("/foo/bar/a.txt"));
		assertTrue(fs.isDirectory("/foo/bar"));
		assertTrue(fs.isDirectory("/foo"));
		fs.close();
	}
	
	@Test(expected = FileSystemException.class)
	public void testDelete2() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("/foo/bar/a.txt"));
		ps.println("Foobar");
		ps.close();
		fs.rmdir("/foo/bar/a.txt"); // Not a dir
	}
	
	@Test(expected = FileSystemException.class)
	public void testDelete3() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("/foo/bar/a.txt"));
		ps.println("Foobar");
		ps.close();
		fs.delete("/foo/bar"); // Not a file
	}
	
	@Test
	public void test1() throws FileSystemException
	{
		RamDisk fs = new RamDisk();
		fs.open();
		TempFolderTest.populate(fs);
		List<String> listing = fs.ls();
		assertEquals(4, listing.size());
		assertTrue(listing.contains("abc"));
		assertTrue(listing.contains("def"));
		assertTrue(listing.contains("jkl"));
		assertTrue(listing.contains("e.txt"));
		fs.chdir("abc");
		listing = fs.ls();
		assertEquals(2, listing.size());
		assertTrue(listing.contains("a.txt"));
		assertTrue(listing.contains("b.txt"));
		listing = fs.ls("../def/ghi/");
		assertEquals(2, listing.size());
		assertTrue(listing.contains("c.txt"));
		assertTrue(listing.contains("d.txt"));
		fs.close();
	}
}
