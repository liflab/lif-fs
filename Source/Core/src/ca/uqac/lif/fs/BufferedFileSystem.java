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

/**
 * A filter file system where input and output streams for files are byte
 * buffers. More precisely, {@link #readFrom(String) readFrom()} completely
 * retrieves the file from the underlying file system and places its contents
 * into a byte array, which is then returned as an input stream. Similarly,
 * {@link #writeTo(String) writeTo()} accumulates contents into a byte array,
 * and dumps the contents of this array to the file system when it is closed.
 * 
 * @author Sylvain Hallé
 */
public class BufferedFileSystem extends FilterFileSystem
{
	/**
	 * Creates a new buffered file system.
	 * @param fs The underlying file system where read and write operations are
	 * redirected
	 */
	public BufferedFileSystem(FileSystem fs)
	{
		super(fs);
	}
	
	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		return new ProxyInputStream(m_fs.readFrom(filename));
	}
	
	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return new ProxyOutputStream(m_fs.writeTo(filename));
	}

}
