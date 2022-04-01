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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link ReadZipFile}.
 */
public class ReadZipFileTest
{
	@Test
	public void testLs1() throws FileSystemException, IOException
	{
		InputStream z_is = ReadZipFileTest.class.getResourceAsStream("archive.zip");
		ReadZipFile fs = new ReadZipFile(z_is);
		fs.open();
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
		z_is.close();
	}
	
	@Test
	public void testRead1() throws FileSystemException, IOException
	{
		InputStream z_is = ReadZipFileTest.class.getResourceAsStream("archive.zip");
		ReadZipFile fs = new ReadZipFile(z_is);
		fs.open();
		String s = new String(FileUtils.toBytes(fs.readFrom("def/e.txt")));
		// For some reason, the file in the zip has as 0A at the end
		assertEquals("1234567890" + (char) 10, s);
		fs.close();
		z_is.close();
	}
}
