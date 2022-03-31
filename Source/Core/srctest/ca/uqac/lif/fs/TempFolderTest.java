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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * Unit tests for {@link TempFolder}.
 */
public class TempFolderTest
{
	/**
	 * The set of temporary folders created by the test suite.
	 */
	protected static Set<String> s_folders = new HashSet<String>();
	
	/**
	 * Gets an instance of TempFolder.
	 * @return The instance
	 * @throws IOException
	 */
	public TempFolder getTempFolder() throws IOException
	{
		TempFolder fs = new TempFolder("fstest");
		s_folders.add(fs.getRoot().toString());
		return fs;
	}
	
	@Test
	public void testNavigation1() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
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
	public void test1() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.open();
		FilePath temp_root = fs.getRoot();
		PrintStream ps = new PrintStream(fs.writeTo("/foo.txt"));
		ps.print("Hello");
		ps.close();
		File f = new File(temp_root.toString() + "/foo.txt");
		assertTrue(f.exists());
		assertEquals(5, f.length());
		fs.close();
		assertFalse(f.exists());
		File folder = new File(temp_root.toString());
		assertFalse(folder.exists());
	}
	
	@Test
	public void test2() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("/foo.txt"));
		ps.print("Hello");
		ps.close();
		Scanner s = new Scanner(fs.readFrom("/foo.txt"));
		assertTrue(s.hasNextLine());
		String line = s.nextLine();
		assertEquals("Hello", line);
		assertFalse(s.hasNextLine());
		s.close();
	}
	
	@Test
	public void testLs1() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.open();
		populate(fs);
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
	
	@Test(expected = FileSystemException.class)
	public void testState1() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.writeTo("/foo.txt");
	}
	
	@Test(expected = FileSystemException.class)
	public void testState2() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.readFrom("/foo.txt");
	}
	
	@Test(expected = FileSystemException.class)
	public void testState3() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.ls();
	}
	
	@Test(expected = FileSystemException.class)
	public void testState4() throws IOException, FileSystemException
	{
		TempFolder fs = getTempFolder();
		fs.pwd();
	}
	
	/**
	 * Manually writes file to a file system to test it. The structure is as
	 * follows:
	 * <pre>
	 * /
	 * +- abc/
	 * |  +- a.txt
	 * |  +- b.txt
	 * +- def/
	 * |  +- ghi/
	 * |  |  +- c.txt
	 * |  |  +- d.txt
	 * |  +- e.txt
	 * +  jkl/
	 * +- e.txt
	 * </pre>
	 * @param fs The file system to write to
	 */
	public static void populate(FileSystem fs) throws FileSystemException
	{
		fs.mkdir("abc");
		fs.chdir("abc");
		{
			PrintStream ps = new PrintStream(fs.writeTo("a.txt"));
			ps.println("Hello world");
			ps.close();
		}
		{
			PrintStream ps = new PrintStream(fs.writeTo("b.txt"));
			ps.println("The quick brown fox jumps over the lazy dog.");
			ps.close();
		}
		fs.popd();
		fs.mkdir("def");
		fs.chdir("def");
		fs.mkdir("ghi");
		fs.chdir("ghi");
		{
			PrintStream ps = new PrintStream(fs.writeTo("c.txt"));
			ps.println("Four score and seven years ago");
			ps.close();
		}
		{
			PrintStream ps = new PrintStream(fs.writeTo("d.txt"));
			ps.println("Lorem ipsum dolor sit amet");
			ps.close();
		}
		fs.popd();
		{
			PrintStream ps = new PrintStream(fs.writeTo("e.txt"));
			ps.println("1234567890");
			ps.close();
		}
		fs.popd();
		fs.mkdir("jkl");
		{
			PrintStream ps = new PrintStream(fs.writeTo("e.txt"));
			ps.println("abcdefg");
			ps.close();
		}
	}
	
	/**
	 * Cleans the temporary folders that may still be present on the machine.
	 */
	@AfterClass
	public static void cleanup()
	{
		for (String folder : s_folders)
		{
			try
			{
				HardDisk.deleteDirectoryRecursion(Path.of(folder));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
