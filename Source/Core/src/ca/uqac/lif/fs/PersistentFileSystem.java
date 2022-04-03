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

/**
 * A file system whose content is preserved within a single file of another
 * file system. When open, the persistent file system reads that file, and uses
 * method {@link #load(InputStream)} to populate the contents of an internal
 * ramdisk. All read/write operations are then performed directly on this
 * in-memory structure. When closed, the persistent file system opens the same
 * file as in the beginning, and uses the {@link #save(OutputStream)} method to
 * write the contents of its internal ramdisk back to this file.
 * <p>
 * Exactly how the ramdisk contents is read and written is left to specific
 * descendants of {@link PersistentFileSystem}, which must implement the
 * {@link #load(InputStream)} and {@link #save(OutputStream)} methods and
 * typically should leave all other methods as they are. A possible use case
 * could be to read an archive file, manipulate its contents in memory, and
 * write its contents back to a new version of the archive.
 *  
 * @author Sylvain Hallé
 */
public abstract class PersistentFileSystem extends RamDisk
{
	/**
	 * Populates the ramdisk by reading the contents of an input file.
	 * The method is not expected to close the stream.
	 * @param is An input stream open on the contents of the file
	 */
	protected abstract void load(InputStream is);
	
	/**
	 * Saves the contents of the ramdisk by writing it to a file.
	 * The method is not expected to close the stream.
	 * @param os An output stream open on the file to write to
	 */
	protected abstract void save(OutputStream os);
	
	/**
	 * The file system used to load and save the current file system.
	 */
	protected FileSystem m_fs;
	
	/**
	 * The name of the file in the underlying file system where the contents of
	 * the current file system should be loaded/saved.
	 */
	protected String m_filename;
	
	@Override
	public void open() throws FileSystemException
	{
		super.open();
		InputStream is = m_fs.readFrom(m_filename);
		load(is);
		try
		{
			is.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	@Override
	public void close() throws FileSystemException
	{
		OutputStream os = m_fs.writeTo(m_filename);
		save(os);
		try
		{
			os.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
		super.close();
	}
}
