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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A file system that delegates all its operations to another file system
 * passed as a parameter. The class {@link FilterFileSystem} itself simply
 * overrides all methods of {@link FileSystem} with versions that pass all
 * requests to the contained file system. Subclasses of FilterFileSystem may
 * further override some of these methods and may also provide additional
 * methods and fields.
 * 
 * @author Sylvain Hallé
 */
public class FilterFileSystem implements FileSystem
{
	/**
	 * The file system to which operations are delegated.
	 */
	/*@ non_null @*/ protected final FileSystem m_fs;
	
	/**
	 * Creates a new filter file system.
	 * @param fs The file system to which operations are delegated
	 */
	public FilterFileSystem(/*@ non_null @*/ FileSystem fs)
	{
		super();
		m_fs = fs;
	}

	@Override
	public void open() throws FileSystemException
	{
		m_fs.open();
	}
	
	@Override
	public long getSize(String path) throws FileSystemException
	{
		return m_fs.getSize(path);
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		return m_fs.ls();
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		return m_fs.ls(path);
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		return m_fs.isDirectory(path);
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		return m_fs.isFile(path);
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return m_fs.writeTo(filename);
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		return m_fs.readFrom(filename);
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		m_fs.chdir(path);
	}
	
	@Override
	public void pushd(String path) throws FileSystemException
	{
		m_fs.pushd(path);
	}
	
	@Override
	public void popd() throws FileSystemException
	{
		m_fs.popd();
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		m_fs.mkdir(path);
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		m_fs.rmdir(path);
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		m_fs.delete(path);
	}

	@Override
	public String pwd() throws FileSystemException
	{
		return m_fs.pwd();
	}

	@Override
	public void close() throws FileSystemException
	{
		m_fs.close();
	}
}
