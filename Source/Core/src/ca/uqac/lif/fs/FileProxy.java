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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Object providing read and write access to a single file from an underlying
 * file system.
 * @author Sylvain Hallé
 */
public class FileProxy
{
	/**
	 * The path to the file.
	 */
	/*@ non_null @*/ protected final String m_pathToFile;
	
	/**
	 * The file system from which the file is to be accessed.
	 */
	/*@ non_null @*/ protected final FileSystem m_fileSystem;
	
	/**
	 * Creates a new file proxy.
	 * @param fs The file system from which the file is to be accessed
	 * @param path The path to the file
	 */
	public FileProxy(FileSystem fs, String path)
	{
		super();
		m_fileSystem = fs;
		m_pathToFile = path;
	}
	
	/**
	 * Gets an input stream open on the file from the underlying file system. 
	 * @return The input stream
	 * @throws FileSystemException
	 */
	public InputStream readFrom() throws FileSystemException
	{
		if (!m_fileSystem.isFile(m_pathToFile))
		{
			throw new FileSystemException("File " + m_pathToFile + " not found");
		}
		return m_fileSystem.readFrom(m_pathToFile);
	}
	
	/**
	 * Gets an output stream open on the file from the underlying file system. 
	 * @return The input stream
	 * @throws FileSystemException
	 */
	public OutputStream writeTo() throws FileSystemException
	{
		if (!m_fileSystem.isFile(m_pathToFile))
		{
			throw new FileSystemException("File " + m_pathToFile + " not found");
		}
		return m_fileSystem.writeTo(m_pathToFile);
	}
	
	/**
	 * A filter input stream that closes the underlying file system once closed.
	 */
	protected class DependentInputStream extends FilterInputStream
	{
		/**
		 * Creates a new dependent input stream from another input stream.
		 * @param is The input stream whose operations are delegated to
		 */
		public DependentInputStream(InputStream is)
		{
			super(is);
		}
		
		@Override
		public void close()
		{
			try
			{
				m_fileSystem.close();
			}
			catch (FileSystemException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A filter output stream that closes the underlying file system once
	 * closed.
	 */
	protected class DependentOutputStream extends FilterOutputStream
	{
		/**
		 * Creates a new dependent output stream from another output stream.
		 * @param is The output stream whose operations are delegated to
		 */
		public DependentOutputStream(OutputStream is)
		{
			super(is);
		}
		
		@Override
		public void close()
		{
			try
			{
				m_fileSystem.close();
			}
			catch (FileSystemException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
