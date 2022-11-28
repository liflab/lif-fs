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
import java.math.BigInteger;
import java.util.Stack;

/**
 * A file system that exposes files in a hierarchy of directory, but internally
 * stores them in a file system that has a flat structure.
 * <p>
 * More precisely, the file system associates an absolute path for a file or a
 * folder to a "flat" filename made of the hex string of that path. For
 * instance:
 * <ul>
 * <li><tt>foo</tt> becomes <tt>666f6f</tt></li>
 * <li><tt>/foo/bar.txt</tt> becomes <tt>2f666f6f2f6261722e747874</tt></li>
 * </ul>
 * The class makes the filename conversion back and forth with the underlying
 * file system, thus simulating the presence of a folder structure on a file
 * system that does not necessarily support it directly. For example, one can
 * store a nested directory structure into a flat key-value store, such as a
 * distributed hash table or a relational database.
 * 
 * @author Sylvain Hallé
 *
 */
public class FlatFileSystem extends RamDisk
{
	/**
	 * The current state of the file system.
	 */
	protected OpenState m_state;

	/**
	 * The underlying file system in which files are stored.
	 */
	protected FileSystem m_fs;

	/**
	 * Creates a new flat file system.
	 * @param fs The underlying file system in which files are stored using
	 * flat filenames
	 */
	public FlatFileSystem(FileSystem fs)
	{
		super();
		m_fs = fs;
		m_state = OpenState.UNINITIALIZED;
		m_currentDir = new FilePath("");
		m_dirStack = new Stack<FilePath>();
		try
		{
			m_fs.open();
			new Populator(m_fs).crawl();
		}
		catch (FileSystemException e)
		{
			m_state = OpenState.CLOSED;
		}
	}

	@Override
	public FlatFileSystem open() throws FileSystemException
	{
		super.open();
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.OPEN;
		return this;
	}

	@Override
	public void close() throws FileSystemException
	{
		super.close();
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.CLOSED;
		m_fs.close();
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(filename);
		String flat_filename = toFlatFilename(fp);
		return m_fs.readFrom(flat_filename);
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(filename);
		createFileNode(fp);
		String flat_filename = toFlatFilename(fp);
		return m_fs.writeTo(flat_filename);
	}

	@Override
	public void delete(String filename) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(filename);
		super.delete(filename);
		String flat_filename = toFlatFilename(fp);
		m_fs.delete(flat_filename);
	}

	/**
	 * Converts an absolute path into a flat filename. The method does so by
	 * printing the hex value of each character in the string.
	 * @param path The file path
	 * @return The corresponding flat filename
	 */
	public static String toFlatFilename(FilePath path)
	{
		byte[] string_bytes = path.toString().getBytes();
		BigInteger bi = new BigInteger(1, string_bytes);
		return String.format("%x", bi);
	}

	/**
	 * Converts a flat filename into a hierarchical file path.
	 * @param filename The filename
	 * @return The hierarchical file path
	 * @throws FileSystemException If the filename is invalid
	 */
	public static FilePath fromFlatFilename(String filename) throws FileSystemException
	{
		boolean absolute = false;
		if (filename.startsWith(FilePath.SLASH))
		{
			absolute = true;
			filename = filename.substring(1);
		}
		if (filename.length() % 2 != 0)
		{
			throw new FileSystemException("Invalid flat filename");
		}
		byte[] array = new byte[filename.length() / 2];
		for (int i = 0, arrayIndex = 0; i < filename.length(); i += 2, arrayIndex++)
		{
			array[arrayIndex] = Integer.valueOf(filename.substring(i, i + 2), 16).byteValue();
		}
		String path = new String(array);
		if (absolute)
		{
			path = "/" + path;
		}
		return new FilePath(path);
	}
	
	/**
	 * Creates the internal directory structure from the listing of flat files
	 * retrieved from the underlying file system's listing.
	 */
	protected class Populator extends RecursiveListing
	{
		/**
		 * Creates a new populator.
		 * @param fs The underlying file system
		 */
		public Populator(FileSystem fs)
		{
			super(fs);
		}

		@Override
		protected void visit(FilePath filename) throws FileSystemException
		{
			FilePath fh_path = fromFlatFilename(filename.toString());
			createFileNode(fh_path);
		}
	}
}
