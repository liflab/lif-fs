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

import java.util.Base64;

import org.junit.Test;

/**
 * Unit tests for {@link Base64FileSystem}.
 */
public class Base64FileSystemTest
{
	@Test
	public void test1() throws FileSystemException
	{
		RamDisk rd = new RamDisk();
		Base64FileSystem fs = new Base64FileSystem(rd);
		fs.open();
		TempFolderTest.populate(fs);
		byte[] contents = FileUtils.toBytes(rd.readFrom("/e.txt"));
		assertEquals("abcdefg", new String(Base64.getDecoder().decode(contents)).trim());
		String in_file = new String(FileUtils.toBytes(fs.readFrom("/e.txt")));
		assertEquals("abcdefg", in_file.trim());
		fs.close();
	}
}
