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
import java.io.OutputStream;

/**
 * An output stream that modifies the contents written to it before it writes
 * them back to another output stream. Concretely, the proxy output stream
 * stores all contents written by calls to {@link #write(int) write()} into a
 * byte buffer. Once a call to {@link #close()} is made, this buffer is
 * modified by the operation of {@link #transform(byte[]) transform()}, and the
 * resulting array of bytes is then written to another output stream. 
 * <p>
 * By default, the proxy output stream acts as the identity function: method
 * {@link #transform(byte[])} returns the same byte array as it receives. A
 * descendant of this class can override this method to perform a different
 * operation on the input byte array.
 * 
 * @author Sylvain Hallé
 */
public class ProxyOutputStream extends OutputStream
{
	/**
	 * The buffer for the original file contents.
	 */
	/*@ non_null @*/ protected final ByteArrayOutputStream m_buffer;
	
	/**
	 * The output stream to write the contents to once closed.
	 */
	/*@ non_null @*/ protected final OutputStream m_output;
	
	/**
	 * Creates a new proxy output stream.
	 * @param os The output stream to write the contents to once closed
	 */
	public ProxyOutputStream(/*@ non_null @*/ OutputStream os)
	{
		super();
		m_output = os;
		m_buffer = new ByteArrayOutputStream();
	}
	
	@Override
	public void write(int b) throws IOException
	{
		m_buffer.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		m_buffer.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		m_buffer.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException
	{
		byte[] altered_contents = transform(m_buffer.toByteArray());
		m_output.write(altered_contents);
		m_output.close();
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
}