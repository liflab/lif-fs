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
 * File system whose root is a sub-directory of another file system. As a
 * result, all absolute paths within the file system are translated as paths
 * relative to this sub-directory within the underlying file system. This
 * creates an environment that resembles the
 * <a href="https://en.wikipedia.org/wiki/Chroot"><tt>chroot</tt></a> Unix
 * command, hence the name of this class.
 * 
 * @author Sylvain Hallé
 */
public class Chroot extends FilterFileSystem
{
	/**
	 * The path in the underlying file system acting as the root of the current
	 * file system.
	 */
	protected FilePath m_root;
	
	/**
	 * Creates a new chroot file system.
	 * @param fs The underlying file system
	 * @param root The directory acting as the root of the current file system
	 */
	public Chroot(FileSystem fs, String root)
	{
		super(fs);
		m_root = new FilePath(root);
	}
	
	@Override
	public List<String> ls() throws FileSystemException
	{
		return m_fs.ls(getPath(""));
	}
	
	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		return m_fs.ls(getPath(path));
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		return m_fs.isDirectory(getPath(path));
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		return m_fs.isFile(getPath(path));
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return m_fs.writeTo(getPath(filename));
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		return m_fs.readFrom(getPath(filename));
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		m_fs.chdir(getPath(path));
	}
	
	@Override
	public void pushd(String path) throws FileSystemException
	{
		m_fs.pushd(getPath(path));
	}
	
	@Override
	public void mkdir(String path) throws FileSystemException
	{
		m_fs.mkdir(getPath(path));
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		m_fs.rmdir(getPath(path));
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		m_fs.delete(getPath(path));
	}
	
	/**
	 * Gets the absolute path corresponding to a path within the file system
	 * @param path The path
	 * @return The absolute path on the local machine
	 * @throws FileSystemException Thrown if the current directory could not be
	 * obtained
	 */
	protected String getPath(String path) throws FileSystemException
	{
		String wd = m_fs.pwd();
		if (wd.startsWith(FilePath.SLASH))
		{
			return m_root.toString() + wd;
		}
		return m_root.toString() + FilePath.SLASH + wd;
	}
}
