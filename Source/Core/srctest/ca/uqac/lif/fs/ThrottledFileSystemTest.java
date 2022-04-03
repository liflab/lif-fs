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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Unit tests for {@link ThrottledFileSystem}.
 */
public class ThrottledFileSystemTest
{
	@Test
	public void testSizeLimit1() throws FileSystemException, IOException
	{
		RamDisk rd = new RamDisk();
		ThrottledFileSystem fs = new ThrottledFileSystem(rd);
		fs.setSizeLimit(128);
		fs.open();
		PrintStream ps = new PrintStream(fs.writeTo("foo"));
		ps.write(new byte[128]);
		ps.close();
		assertEquals(128, fs.getSize("foo"));
		fs.close();
	}

	@Test(expected = IOException.class)
	public void testSizeLimit2() throws FileSystemException, IOException
	{
		RamDisk rd = new RamDisk();
		ThrottledFileSystem fs = new ThrottledFileSystem(rd);
		fs.setSizeLimit(128);
		fs.open();
		OutputStream os = fs.writeTo("foo");
		os.write(new byte[256]);
		os.close();
	}

	@Test
	public void testSizeLimit3() throws FileSystemException, IOException
	{
		RamDisk rd = new RamDisk();
		rd.open();
		{
			PrintStream ps = new PrintStream(rd.writeTo("foo"));
			ps.print("012345678901234567890123456789"); // 30 bytes
			ps.close();
		}
		ThrottledFileSystem fs = new ThrottledFileSystem(rd);
		fs.setSizeLimit(50);
		fs.open();
		{
			OutputStream ps = fs.writeTo("bar");
			ps.write(new byte[10]); // 10 bytes left: OK
			ps.close();
		}
		boolean excepted = false;
		try
		{
			OutputStream ps = fs.writeTo("baz");
			ps.write(new byte[15]); // Disk full
			ps.close();
		}
		catch (IOException e)
		{
			excepted = true;
		}
		assertTrue(excepted);
		fs.close();
	}
	
	@Test
	public void testSizeLimit4() throws FileSystemException, IOException
	{
		RamDisk rd = new RamDisk();
		rd.open();
		{
			PrintStream ps = new PrintStream(rd.writeTo("foo"));
			ps.print("012345678901234567890123456789"); // 30 bytes
			ps.close();
		}
		ThrottledFileSystem fs = new ThrottledFileSystem(rd);
		fs.setSizeLimit(50);
		fs.open();
		{
			OutputStream ps = fs.writeTo("foo");
			ps.write(new byte[10]); // 40 bytes left, since we overwrite foo
			ps.close();
		}
		boolean excepted = false;
		try
		{
			OutputStream ps = fs.writeTo("baz");
			ps.write(new byte[15]); // OK
			ps.close();
		}
		catch (IOException e)
		{
			excepted = true;
		}
		assertFalse(excepted);
		fs.close();
	}
}
