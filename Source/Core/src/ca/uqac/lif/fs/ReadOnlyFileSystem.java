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

import java.io.OutputStream;

/**
 * A file system that disables all write operations of another file system,
 * rendering it read-only.
 * @author Sylvain Hallé
 */
public class ReadOnlyFileSystem extends FilterFileSystem
{
	/**
	 * Creates a new read-only file system.
	 * @param fs The underlying file system to regulate access to
	 */
	public ReadOnlyFileSystem(FileSystem fs)
	{
		super(fs);
	}
	
	@Override
	public void mkdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Unauthorized access");
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Unauthorized access");
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		throw new FileSystemException("Unauthorized access");
	}
	
	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		throw new FileSystemException("Unauthorized access");
	}
}
