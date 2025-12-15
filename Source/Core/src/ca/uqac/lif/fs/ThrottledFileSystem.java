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
 * A file system that imposes size and/or speed limits on the access to another
 * file system.
 * <p>
 * A particular example of throttled file system is the {@link FloppyDisk},
 * which caps both capacity and transfer speed to that of vintage disk drives.
 * 
 * @author Sylvain Hallé
 */
public class ThrottledFileSystem extends FilterFileSystem
{
	/**
	 * The transfer speed limit, in bytes per second. Set to -1 to impose no
	 * limit.
	 */
	protected long m_speedLimit;
	
	/**
	 * The maximum capcity, in bytes. Set to -1 to impose no limit.
	 */
	protected long m_sizeLimit;
	
	/**
	 * The current size of the file system, in bytes.
	 */
	protected long m_currentSize;

	/**
	 * Creates a new throttled file system.
	 * @param fs The underlying file system to give access to
	 */
	public ThrottledFileSystem(FileSystem fs)
	{
		super(fs);
		try
		{
			m_currentSize = FileUtils.getSize(fs, "");
		}
		catch (FileSystemException e)
		{
			m_currentSize = 0;
		}
		m_sizeLimit = -1;
		m_speedLimit = -1;
	}
	
	/**
	 * Sets the transfer speed limit of this file system.
	 * @param speed The transfer speed limit, in bytes per second; set to -1 to
	 * impose no limit
	 */
	public void setSpeedLimit(long speed)
	{
		m_speedLimit = speed;
	}
	
	/**
	 * Sets the maximum capacity of this file system.
	 * @param size The maximum capcity, in bytes; set to -1 to impose no limit
	 */
	public void setSizeLimit(long size)
	{
		m_sizeLimit = size;
	}
	
	/**
	 * Gets the maximum size of the drive.
	 * @return The size, in bytes. If no limit is specified, it returns
	 * Long.MAX_VALUE.
	 */
	protected long getMaxSize()
	{
		return m_sizeLimit < 0 ? Long.MAX_VALUE : m_sizeLimit;
	}
	
	/**
	 * Gets the maximum transfer speed of the drive.
	 * @return The speed, in bytes per second.  If no limit is specified, it
	 * returns Long.MAX_VALUE.
	 */
	protected long getMaxSpeed()
	{
		return m_speedLimit < 0 ? Long.MAX_VALUE : m_speedLimit;
	}
	
	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		long current_file_size = 0;
		if (m_fs.isFile(filename))
		{
			current_file_size = m_fs.getSize(filename);
		}
		OutputStream os = m_fs.writeTo(filename);
		// Wrap input stream in another one that throttles write speed
		CappedOutputStream tos = new CappedOutputStream(os, getMaxSpeed(), current_file_size);
		long max_size = getMaxSize() - m_currentSize + current_file_size;
		tos.setMaxBytesToWrite(max_size);
		return tos;
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		InputStream is = m_fs.readFrom(filename);
		// Wrap input stream in another one that throttles read speed
		return new ThrottledInputStream(is, getMaxSpeed());
	}
	
	@Override
	public void delete(String filename) throws FileSystemException
	{
		m_currentSize -= getSize(filename);
		m_fs.delete(filename);
	}

	/**
	 * An output stream that updates the available size of the floppy drive when
	 * closed. 
	 */
	protected class CappedOutputStream extends ThrottledOutputStream
	{
		protected long m_originalSize;
		
		public CappedOutputStream(OutputStream rawStream, long maxBytesPerSec, long original_size)
		{
			super(rawStream, maxBytesPerSec);
			m_originalSize = original_size;
		}
		
		@Override
		public void close() throws IOException
		{
			super.close();
			// Update the size of the floppy
			m_currentSize += (getTotalBytesWritten() - m_originalSize);
		}
	}
}
