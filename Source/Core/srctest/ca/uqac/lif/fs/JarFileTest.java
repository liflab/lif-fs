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

import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link JarFile}.
 */
public class JarFileTest
{
	@Test
	public void test1() throws FileSystemException
	{
		JarFile jf = new JarFile(JarFileTest.class);
		jf.open();
		jf.chdir("resources");
		List<String> listing = jf.ls();
		assertEquals(2, listing.size());
		assertTrue(listing.contains("foo.txt"));
		assertTrue(listing.contains("foo"));
		assertTrue(jf.isDirectory("foo"));
		assertFalse(jf.isFile("foo"));
		assertFalse(jf.isDirectory("foo.txt"));
		assertTrue(jf.isFile("foo.txt"));
		jf.close();
	}
	
	@Test
	public void test2() throws FileSystemException
	{
		JarFile jf = new JarFile(JarFileTest.class);
		jf.open();
		jf.chdir("resources");
		String s = new String(FileUtils.toBytes(jf.readFrom("foo.txt")));
		assertEquals("Hello world", s);
		jf.close();
	}
}
