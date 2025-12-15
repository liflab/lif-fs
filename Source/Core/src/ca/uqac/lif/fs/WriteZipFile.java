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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A file system with operations to write to a zip file.
 * @author Sylvain Hallé
 */
public class WriteZipFile implements FileSystem
{
	/**
	 * A stream to write to a zip file.
	 */
	/*@ null @*/ protected ZipOutputStream m_zipOutput;
	
	/**
	 * The underlying output stream to which the zip file 
	 */
	/*@ non_null @*/ protected OutputStream m_output;
	
	/**
	 * The current working directory within the zip archive
	 */
	/*@ non_null @*/ protected FilePath m_currentDir;
	
	/**
	 * A stack containing the history of current directories.
	 */
	/*@ non_null @*/ protected Stack<FilePath> m_dirStack;
	
	/**
	 * Creates a new zip file system in write mode.
	 * @param os The output stream where the zip file is to be written upon a
	 * call to {@link #close()}
	 */
	public WriteZipFile(/*@ non_null @*/ OutputStream os)
	{
		super();
		m_output = os;
		m_currentDir = new FilePath(FilePath.SLASH);
		m_dirStack = new Stack<FilePath>();
	}
	
	@Override
	public WriteZipFile open() throws FileSystemException
	{
		m_zipOutput = new ZipOutputStream(m_output);
		return this;
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		if (m_zipOutput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		Path file_path = Paths.get(FileUtils.trimSlash(m_currentDir.toString()), filename);
		return new ZipEntryOutputStream(file_path.toString());
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		throw new FileSystemException("File system is open in write mode");
	}
	
	@Override
	public long getSize(String filename) throws FileSystemException
	{
		throw new FileSystemException("File system is open in write mode");
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		if (m_zipOutput == null)
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
		if (m_zipOutput == null)
		{
			throw new FileSystemException("File system is not open");
		}
		// Nothing to do, paths are created on first file that mentions them
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public String pwd()
	{
		return m_currentDir.toString();
	}
	
	@Override
	public List<String> ls() throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public void close() throws FileSystemException
	{
		try
		{
			m_zipOutput.close();
			m_zipOutput = null;
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * An output stream associated to an entry in a zip file. The output stream
	 * accumulates the written bytes internally, and creates a new file within
	 * the archive with this data at the moment it is closed. As such, this means
	 * that multiple files can be written to at the same time, provided that
	 * their respective output streams are not closed concurrently.
	 */
	protected class ZipEntryOutputStream extends OutputStream
	{
		/**
		 * The zip entry to write to.
		 */
		protected ZipEntry m_entry;
		
		/**
		 * The output stream collecting the contents of the zip entry before it
		 * is committed to the archive.
		 */
		protected ByteArrayOutputStream m_outputStream;
		
		/**
		 * Creates a new zip entry output stream.
		 * @param name The name of the file within the archive this stream will
		 * write to
		 */
		public ZipEntryOutputStream(String name)
		{
			super();
			m_entry = new ZipEntry(name);
			m_outputStream = new ByteArrayOutputStream();
		}
		
		@Override
		public void write(int b) throws IOException
		{
			m_outputStream.write(b);
		}
		
		@Override
		public void close() throws IOException
		{
			m_zipOutput.putNextEntry(m_entry);
			m_zipOutput.write(m_outputStream.toByteArray());
			m_zipOutput.closeEntry();
		}
	}
}
