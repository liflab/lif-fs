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

import org.junit.Test;

/**
 * Unit tests for {@link FlatFileSystem}.
 */
public class FlatFileSystemTest
{
	@Test
	public void testToFlatFilename1()
	{
		String flat = FlatFileSystem.toFlatFilename(new FilePath("foo"));
		assertEquals("666f6f", flat);
	}
	
	@Test
	public void testToFlatFilename2()
	{
		String flat = FlatFileSystem.toFlatFilename(new FilePath("/foo/bar.txt"));
		assertEquals("2f666f6f2f6261722e747874", flat);
	}
	
	@Test
	public void testFromFlatFilename1() throws FileSystemException
	{
		FilePath fp = FlatFileSystem.fromFlatFilename("2f666f6f2f6261722e747874");
		assertEquals("/foo/bar.txt", fp.toString());
	}
	
	@Test
	public void test1() throws FileSystemException
	{
		RamDisk rd = new RamDisk();
		FlatFileSystem fs = new FlatFileSystem(rd);
		fs.open();
		TempFolderTest.populate(fs);
		assertTrue(rd.isFile(FlatFileSystem.toFlatFilename(new FilePath("/def/ghi/c.txt"))));
		String contents = new String(FileUtils.toBytes(rd.readFrom(FlatFileSystem.toFlatFilename(new FilePath("/def/ghi/c.txt")))));
		assertEquals("Four score and seven years ago", contents.trim());
		fs.close();
	}
	
	@Test
	public void test2() throws FileSystemException
	{
		RamDisk rd = new RamDisk();
		FlatFileSystem fs = new FlatFileSystem(rd);
		fs.open();
		TempFolderTest.populate(fs);
		fs.delete("/def/ghi/c.txt");
		assertFalse(rd.isFile(FlatFileSystem.toFlatFilename(new FilePath("/def/ghi/c.txt"))));
		fs.close();
	}
	
	@Test
	public void test3() throws FileSystemException
	{
		RamDisk rd = new RamDisk();
		FlatFileSystem fs = new FlatFileSystem(rd);
		fs.open();
		TempFolderTest.populate(fs);
		RamDisk rd2 = new RamDisk();
		rd2.open();
		FileUtils.copy(rd, rd2);
		fs.close();
		FlatFileSystem fs2 = new FlatFileSystem(rd2);
		fs2.open();
		assertTrue(fs2.isFile("/def/ghi/c.txt"));
		String contents = new String(FileUtils.toBytes(fs2.readFrom("/def/ghi/c.txt")));
		assertEquals("Four score and seven years ago", contents.trim());
		fs2.close();
	}
}
