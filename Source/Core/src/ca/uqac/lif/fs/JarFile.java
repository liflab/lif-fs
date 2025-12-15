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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Exposes files within a resource such as a JAR file.
 * @author Sylvain Hallé
 */
public class JarFile implements FileSystem
{
	/**
	 * The class relative to which the files are located.
	 */
	protected Class<?> m_referenceClass;

	/**
	 * The path representing the current directory.
	 */
	protected FilePath m_currentDir;

	/**
	 * A stack containing the history of current directories.
	 */
	/*@ non_null @*/ protected Stack<FilePath> m_dirStack;

	/**
	 * The current state of the file system.
	 */
	protected OpenState m_state;

	/**
	 * Creates a new instance of the file system.
	 * @param reference The class relative to which the files are located
	 */
	public JarFile(Class<?> reference)
	{
		super();
		m_referenceClass = reference;
		m_currentDir = new FilePath("");
		m_dirStack = new Stack<FilePath>();
	}

	@Override
	public JarFile open() throws FileSystemException
	{
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.OPEN;
		return this;
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		return ls(m_currentDir);
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		return ls(m_currentDir.chdir(new FilePath(path)));
	}

	protected List<String> ls(FilePath fp) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		List<String> listing = new ArrayList<String>();
		File f;
		try 
		{
			String path = fp.toString();
			if (path.startsWith(FilePath.SLASH))
			{
				path = path.substring(1);
			}
			URL url = m_referenceClass.getResource(path);
			if (url == null)
			{
				throw new FileSystemException("No such path");
			}
			URI uri = url.toURI();
			if (uri == null)
			{
				throw new FileSystemException("No such path");
			}
			f = new File(uri);
			for (String uris : f.list())
			{
				listing.add(uris);
			}
		}
		catch (URISyntaxException e)
		{
			throw new FileSystemException(e);
		}
		return listing;
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		FilePath filename = m_currentDir.chdir(path);
		String s_filename = filename.toString();
		if (s_filename.startsWith(FilePath.SLASH))
		{
			s_filename = s_filename.substring(1);
		}
		URL u = m_referenceClass.getResource(s_filename);
		if (u == null)
		{
			return false;
		}
		try
		{
			URI uri = u.toURI();
			if (uri == null)
			{
				return false;
			}
			File f = new File(uri);
			return f.isDirectory();
		}
		catch (URISyntaxException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		FilePath filename = m_currentDir.chdir(path);
		String s_filename = filename.toString();
		if (s_filename.startsWith(FilePath.SLASH))
		{
			s_filename = s_filename.substring(1);
		}
		URL u = m_referenceClass.getResource(s_filename);
		if (u == null)
		{
			return false;
		}
		try
		{
			URI uri = u.toURI();
			if (uri == null)
			{
				return false;
			}
			File f = new File(uri);
			return f.isFile();
		}
		catch (URISyntaxException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	@Override
	public long getSize(String path) throws FileSystemException
	{
		FilePath filename = m_currentDir.chdir(path);
		String s_filename = filename.toString();
		if (s_filename.startsWith(FilePath.SLASH))
		{
			s_filename = s_filename.substring(1);
		}
		URL u = m_referenceClass.getResource(s_filename);
		if (u == null)
		{
			throw new FileSystemException("File does not exist");
		}
		try
		{
			URI uri = u.toURI();
			if (uri == null)
			{
				throw new FileSystemException("File does not exist");
			}
			File f = new File(uri);
			return f.length();
		}
		catch (URISyntaxException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported on this file system");
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		FilePath current = m_currentDir.chdir(filename);
		String s_filename = current.toString();
		if (s_filename.startsWith(FilePath.SLASH))
		{
			s_filename = s_filename.substring(1);
		}
		InputStream is = m_referenceClass.getResourceAsStream(s_filename);
		if (is == null)
		{
			throw new FileSystemException("File " + current + " not found");
		}
		return is;
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		m_dirStack.push(m_currentDir);
		m_currentDir = m_currentDir.chdir(path);
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		chdir(path);
	}

	@Override
	public void popd() throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		if (m_dirStack.isEmpty())
		{
			m_currentDir = new FilePath("");
		}
		else
		{
			m_currentDir = m_dirStack.pop();
		}
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported on this file system");
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported on this file system");
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported on this file system");
	}

	@Override
	public String pwd() throws FileSystemException
	{
		return m_currentDir.toString();
	}

	@Override
	public void close() throws FileSystemException
	{
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.CLOSED;
	}
}
