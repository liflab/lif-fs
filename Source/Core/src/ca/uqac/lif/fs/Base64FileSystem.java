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
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * A file system that stores its files into another file system as
 * <a href="http://www.ietf.org/rfc/rfc4648.txt">base-64</a> encoded strings.
 * 
 * @author Sylvain Hallé
 */
public class Base64FileSystem extends FilterFileSystem
{
	/**
	 * Creates a new base-64 file system.
	 * @param fs The file system where files are stored
	 */
	public Base64FileSystem(FileSystem fs)
	{
		super(fs);
	}
	
	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		return new Base64InputStream(m_fs.readFrom(filename));
	}
	
	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return new Base64OutputStream(m_fs.writeTo(filename));
	}
	
	/**
	 * A proxy input stream that reads the contents of another input stream,
	 * interprets it as a base-64 encoded string, and makes available the
	 * decoded result.
	 */
	protected static class Base64InputStream extends ProxyInputStream
	{
		/**
		 * The encoder used to read base-64 strings.
		 */
		protected static Decoder s_decoder = Base64.getDecoder();
		
		/**
		 * Creates a new base-64 input stream.
		 * @param is The input stream to read from
		 */
		public Base64InputStream(InputStream is)
		{
			super(is);
		}
		
		@Override
		public byte[] transform(byte[] contents)
		{
			return s_decoder.decode(contents);
		}
	}
	
	/**
	 * A proxy output stream that converts the bytes written to it into a base-64
	 * encoded string.
	 */
	protected static class Base64OutputStream extends ProxyOutputStream
	{
		/**
		 * The encoder used to produce base-64 strings.
		 */
		protected static Encoder s_encoder = Base64.getEncoder();
		
		/**
		 * Creates a new base-64 output stream.
		 * @param is The output stream to write to
		 */
		public Base64OutputStream(OutputStream is)
		{
			super(is);
		}
		
		@Override
		public byte[] transform(byte[] contents)
		{
			return s_encoder.encode(contents);
		}
	}
	
}
