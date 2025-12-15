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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link FileUtils}.
 */
public class FileUtilsTest
{
	@Test
	public void testCopyStreams1() throws FileSystemException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream("foobar".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileUtils.copy(bais, baos);
		String s = new String(baos.toByteArray());
		assertEquals("foobar", s);
	}
	
	@Test
	public void testCopyFileSystem1() throws FileSystemException
	{
		RamDisk rd1 = new RamDisk();
		rd1.open();
		TempFolderTest.populate(rd1);
		RamDisk fs = new RamDisk();
		fs.open();
		FileUtils.copy(rd1, fs);
		rd1.close();
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
