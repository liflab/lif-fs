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
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Exposes the contents of a zip archive as a read-only file system.
 * Internally, the class descends from {@link RamDisk}; however, it does
 * <em>not</em> extract the archive into memory: it only uses its parent's
 * tree structure to store the listing of folders without having to re-scan
 * the file entries in the zip archive every time an operation such as
 * {@link #ls()} is invoked. Only when {@link #readFrom(String)} is called
 * does the file system move the location of the pointer within the zip
 * file stream to the appropriate location; the corresponding file is then
 * extracted on the fly. This way, it is possible to interact with a large
 * archive, without the need to completely uncompress it before use.
 * 
 * @author Sylvain Hallé
 */
public class ReadZipFile extends RamDisk
{
	/**
	 * A stream to read from a zip file.
	 */
	/*@ null @*/ protected ZipInputStream m_zipInput;
	
	/**
	 * The input stream from which the contents of the zip file is to be read.
	 */
	/*@ non_null @*/ protected ProxyInputStream m_input;
	
	/**
	 * Creates a new zip file system in read mode.
	 * @param input The input stream from which the contents of the zip file is
	 * to be read
	 */
	public ReadZipFile(InputStream input)
	{
		super();
		m_input = new ProxyInputStream(input);
	}
	
	@Override
	public void open() throws FileSystemException
	{
		super.open();
		m_input.mark(0);
		m_zipInput = new ZipInputStream(m_input);
		ZipEntry entry;
		try
		{
			entry = m_zipInput.getNextEntry();
			while (entry != null)
			{
				if (entry.isDirectory())
				{
					createFolderNode(new FilePath(entry.getName()));
				}
				else
				{
					createFileNode(new FilePath(entry.getName()));
				}
				entry = m_zipInput.getNextEntry();
			}
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		throw new FileSystemException("Operation not supported by this file system");
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		try
		{
			Path file_path = Paths.get(FileUtils.trimSlash(m_currentDir.toString()), filename);
			getZipEntry(file_path.toString());
			return m_zipInput;
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
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
	public void close() throws FileSystemException
	{
		super.close();
		try
		{
			m_zipInput.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * Gets the zip entry corresponding to a given path. This method has the
	 * side effect of placing the pointer of the zip input stream at the location
	 * of the desired file.
	 * @param path The path to get the entry for
	 * @return The zip entry
	 * @throws IOException Thrown if resetting the input stream fails
	 */
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
			ze = m_zipInput.getNextEntry();
		}
		return null;
	}
}
