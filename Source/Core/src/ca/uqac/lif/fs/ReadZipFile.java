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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.uqac.lif.fs.FileSystem.OpenState;

public class ReadZipFile implements FileSystem
{
	/**
	 * A stream to read from a zip file.
	 */
	/*@ null @*/ protected ZipInputStream m_zipInput;
	
	/**
	 * The input stream from which the contents of the zip file is to be read.
	 */
	/*@ non_null @*/ protected InputStream m_input;
	
	/**
	 * The current working directory within the zip archive
	 */
	/*@ non_null @*/ protected FilePath m_currentDir;
	
	/**
	 * A stack containing the history of current directories.
	 */
	/*@ non_null @*/ protected Stack<FilePath> m_dirStack;
	
	/**
	 * A set containing a summary of all the files inside the zip file.
	 */
	protected Set<ZipEntrySummary> m_zipContents;
	
	/**
	 * Creates a new zip file system in read mode.
	 * @param input The input stream from which the contents of the zip file is
	 * to be read
	 */
	public ReadZipFile(InputStream input)
	{
		super();
		m_input = input;
		m_zipContents = new HashSet<ZipEntrySummary>();
		m_dirStack = new Stack<FilePath>();
	}
	
	@Override
	public void open() throws FileSystemException
	{
		m_zipInput = new ZipInputStream(m_input);
		ZipEntry entry;
		try
		{
			entry = m_zipInput.getNextEntry();
			while (entry != null)
			{
				m_zipContents.add(new ZipEntrySummary(entry));
			}
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		if (m_zipInput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		if (m_zipInput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		if (m_zipInput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		if (m_zipInput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		if (m_zipInput == null)
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
		if (m_zipInput == null)
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
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");		
	}

	@Override
	public String pwd()
	{
		return m_currentDir.toString();
	}

	@Override
	public void close() throws FileSystemException
	{
		try
		{
			m_zipInput.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	protected List<String> filterResourceListing(String path)
	{
		for (ZipEntrySummary zes : m_zipContents)
		{
			
		}
		return null;
	}
	
	protected ZipEntry getZipEntry(String path) throws IOException
	{
		m_input.reset();
		m_zipInput = new ZipInputStream(m_input);
		ZipEntry ze = m_zipInput.getNextEntry();
		while (ze != null)
		{
			String e_name = ze.getName();
			if (e_name.compareTo(path) == 0)
			{
				return ze;
			}
		}
		return null;
	}
	
	protected static class ZipEntrySummary
	{
		/**
		 * The path of the zip entry
		 */
		protected Path m_path;
		
		/**
		 * A flag indicating whether the entry represents a directory within the
		 * zip file.
		 */
		protected boolean m_isDirectory;
		
		/**
		 * Creates a new zip entry summary.
		 * @param ze The zip entry of which this object is a summary
		 */
		public ZipEntrySummary(ZipEntry ze)
		{
			super();
			m_path = Path.of(ze.getName());
			m_isDirectory = ze.isDirectory();
		}
		
		/**
		 * Determines if the zip entry is a directory.
		 * @return <tt>true</tt> if it is a directory, <tt>false</tt> otherwise
		 */
		/*@ pure @*/ public boolean isDirectory()
		{
			return m_isDirectory;
		}
		
		/**
		 * Gets the path of the zip entry.
		 * @return The path
		 */
		/*@ pure non_null @*/ public Path getPath()
		{
			return m_path;
		}
	}
}
