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
import java.util.ArrayList;
import java.util.List;

/**
 * A file system that replicates all operations made on it on multiple other
 * file systems.
 * <p>
 * An interesting side effect of Mirror is what it does on read operations:
 * when a folder or a file is accessed, it queries each of its underlying file
 * systems until one of them finds it. As a result, when used in read mode,
 * Mirror "merges" multiple directory structures into one.
 * 
 * @author Sylvain Hallé
 */
public class Mirror implements FileSystem
{
	/**
	 * The file systems on which operations are done.
	 */
	/*@ non_null @*/ protected final FileSystem[] m_mirrors;
	
	/**
	 * Creates a new mirror file system.
	 * @param systems The file systems on which operations are done
	 */
	public Mirror(FileSystem ... systems)
	{
		super();
		m_mirrors = systems;
	}
	
	@Override
	public void open() throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.open();
		}
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		return ls(m_mirrors[0].pwd());
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		List<String> contents = new ArrayList<String>();
		for (FileSystem fs : m_mirrors)
		{
			List<String> listing = fs.ls(path);
			for (String f : listing)
			{
				if (!contents.contains(f))
				{
					contents.add(f);
				}
			}
		}
		return contents;
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			if (fs.isDirectory(path))
			{
				return true;		
			}
		}
		return false;
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			if (fs.isFile(path))
			{
				return true;		
			}
		}
		return false;
	}

	@Override
	public long getSize(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			if (fs.isFile(path))
			{
				return fs.getSize(path);		
			}
		}
		throw new FileSystemException("File not found");
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		return new MirrorOutputStream(filename);
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			if (fs.isFile(filename))
			{
				return fs.readFrom(filename);		
			}
		}
		throw new FileSystemException("File not found");
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.chdir(path);
		}
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.pushd(path);
		}
	}

	@Override
	public void popd() throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.popd();
		}
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.mkdir(path);
		}
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.rmdir(path);
		}
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.delete(path);
		}
	}

	@Override
	public String pwd() throws FileSystemException
	{
		return m_mirrors[0].pwd();
	}

	@Override
	public void close() throws FileSystemException
	{
		for (FileSystem fs : m_mirrors)
		{
			fs.close();
		}
	}
	
	protected class MirrorOutputStream extends OutputStream
	{
		protected OutputStream[] m_streams;
		
		public MirrorOutputStream(String filename) throws FileSystemException
		{
			super();
			m_streams = new OutputStream[m_mirrors.length];
			for (int i = 0; i < m_streams.length; i++)
			{
				m_streams[i] = m_mirrors[i].writeTo(filename);
			}
		}
		
		@Override
		public void write(int b) throws IOException
		{
			for (OutputStream os : m_streams)
			{
				os.write(b);
			}
		}
		
		@Override
		public void write(byte[] b) throws IOException
		{
			for (OutputStream os : m_streams)
			{
				os.write(b);
			}
		}
		
		@Override
		public void write(byte[] b, int arg0, int arg1) throws IOException
		{
			for (OutputStream os : m_streams)
			{
				os.write(b, arg0, arg1);
			}
		}
		
		@Override
		public void close() throws IOException
		{
			super.close();
			for (OutputStream os : m_streams)
			{
				os.close();
			}
		}
	}

}
