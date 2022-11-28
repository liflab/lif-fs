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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A "dummy" file system where all operations have no effect. That is:
 * <ul>
 * <li>{@link #writeTo(String)}, and all directory operations do nothing</li>
 * <li>{@link #readFrom(String)} returns a stream pointing to an empty byte
 * array</li>
 * <li>{@link #ls()} returns an empty list of files</li>
 * </ul>
 * <p>
 * The purpose of this class is to provide a drop-in replacement for an actual
 * system when the result of write operations is irrelevant (such as during a
 * test).
 * @author Sylvain Hallé
 */
public class NoopFileSystem implements FileSystem
{

	@Override
	public NoopFileSystem open() throws FileSystemException
	{
		return this;
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		return new ArrayList<String>(0);
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		return new ArrayList<String>(0);
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		return false;
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		return false;
	}

	@Override
	public long getSize(String path) throws FileSystemException
	{
		return 0;
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return new NullOutputStream();
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public void popd() throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		// Do nothing
	}

	@Override
	public String pwd() throws FileSystemException
	{
		return FilePath.SLASH;
	}

	@Override
	public void close() throws FileSystemException
	{
		// Do nothing
	}

	/**
	 * A stream where write operations have no effect.
	 */
	public class NullOutputStream extends OutputStream 
	{
		@Override
		public void write(int b) throws IOException 
		{
			// Do nothing
		}
	}
}
