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
package ca.uqac.lif.fs.memento;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.uqac.lif.fs.FileProxy;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;

/**
 * Memento that interacts with an object stored in a file in an
 * abstract file system.
 * @author Sylvain Hallé
 */
public abstract class FileMemento<T> implements Memento<T>
{
	/**
	 * The file from which to extract the metadata.
	 */
	/*@ non_null @*/ protected final FileProxy m_file;
	
	/**
	 * A byte array preserving the contents of the file the last time
	 * it was read.
	 */
	/*@ non_null @*/ protected byte[] m_fileContents;
	
	public FileMemento(FileProxy file)
	{
		super();
		m_file = file;
		m_fileContents = new byte[0];
	}
	
	@Override
	public T read() throws FileSystemException
	{
		fetchFileContents();
		try 
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(m_fileContents);
			T s = read(bais);
			bais.close();
			return s;
		} 
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void write(T b) throws FileSystemException
	{
		fetchFileContents();
		try 
		{
			OutputStream os = m_file.writeTo();
			write(b, os);
			os.close();
		} 
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	protected void fetchFileContents() throws FileSystemException
	{
		InputStream is;
		try 
		{
			is = m_file.readFrom();
			m_fileContents = FileUtils.toBytes(is);
			is.close();
		} 
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	protected abstract T read(InputStream is) throws IOException;
	
	protected abstract void write(T b, OutputStream os) throws IOException;

}
