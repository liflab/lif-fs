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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that modifies the contents it reads from another input
 * stream before it makes it available. Concretely, the proxy input stream
 * first reads and stores all contents of another input stream into a byte
 * buffer. Then, this buffer is modified by the operation of
 * {@link #transform(byte[]) transform()}, and the resulting array of bytes
 * is then made available in calls to {@link #read()}. 
 * <p>
 * By default, the proxy input stream acts as the identity function: method
 * {@link #transform(byte[])} returns the same byte array as it receives. A
 * descendant of this class can override this method to perform a different
 * operation on the input byte array.
 * <p>
 * The contents of the underlying input stream is only fetched on the first
 * call to {@link #read()}.
 * 
 * @author Sylvain Hallé
 * @see ProxyOutputStream
 */
public class ProxyInputStream extends InputStream
{
	/**
	 * The buffer for the original file contents.
	 */
	/*@ non_null @*/ protected ByteArrayInputStream m_buffer;
	
	/**
	 * The input stream to read from.
	 */
	/*@ non_null @*/ protected final InputStream m_input;
	
	/**
	 * Creates a new proxy input stream.
	 * @param is The input stream to read from
	 */
	public ProxyInputStream(/*@ non_null @*/ InputStream is)
	{
		super();
		m_input = is;
	}
	
	/**
	 * Modifies the contents of a file. By default, this method merely returns
	 * the same byte array as it receives. A descendant of this class can
	 * override this method to perform a different operation on the input byte
	 * array.
	 * @param contents The original contents of the file
	 * @return The modified contents of the file
	 */
	public byte[] transform(/*@ non_null @*/ byte[] contents)
	{
		return contents;
	}

	
	@Override
	public void close() throws IOException
	{
		super.close();
		m_buffer.close();
	}
	
	@Override
	public int read() throws IOException
	{
		checkState();
		return m_buffer.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException
	{
		checkState();
		return m_buffer.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		checkState();
		return m_buffer.read(b, off, len);
	}
	
	protected void checkState() throws IOException
	{
		if (m_buffer == null)
		{
			try
			{
				byte[] original_contents = FileUtils.toBytes(m_input);
				byte[] transformed_contents = transform(original_contents);
				m_buffer = new ByteArrayInputStream(transformed_contents);
			}
			catch (FileSystemException e)
			{
				throw new IOException("Cannot read contents of input stream");
			}
		}
	}
}