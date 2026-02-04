/*
  Abstract file system manipulations
  Copyright (C) 2022-2025 Sylvain Hallé

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * A file system that interacts with concrete files of a local machine's hard
 * drive. Optionally, the class can be given a folder name, which will be taken
 * as the root of the file system exposed to the user. In such a way,
 * {@link HardDisk} can perform an operation similar to
 * <a href="https://en.wikipedia.org/wiki/Chroot"><tt>chroot</tt></a>
 * 
 * @author Sylvain Hallé
 */
public class HardDisk extends AbstractReifiableFileSystem
{
	/**
	 * The system-dependent carriage return symbol
	 */
	public static final transient String CRLF = System.getProperty("line.separator");

	/**
	 * The system-dependent symbol for separating paths
	 */
	public static final transient String SLASH = System.getProperty("file.separator");

	/**
	 * The path of the directory on the local file system that is exposed as the
	 * "root" directory by this file system object.
	 */
	/* @ non_null @ */ protected FilePath m_root;

	/**
	 * The path representing the current directory.
	 */
	protected FilePath m_currentDir;

	/**
	 * A stack containing the history of current directories.
	 */
	protected Stack<FilePath> m_dirStack;

	/**
	 * The current state of the file system.
	 */
	protected OpenState m_state;

	/**
	 * Creates a new local file system, using the root of the underlying file system
	 * as its root, and setting its current directory to the current working
	 * directory in that file system.
	 */
	public HardDisk()
	{
		super();
		m_root = new FilePath("");
		m_state = OpenState.UNINITIALIZED;
		m_currentDir = new FilePath(System.getProperty("user.dir"));
		m_dirStack = new Stack<FilePath>();
	}

	/**
	 * Creates a new local file system, using an underlying file system folder as
	 * its root.
	 * 
	 * @param root
	 *          The folder in the underlying file system that will act as the root
	 *          of the created file system
	 */
	public HardDisk(String root)
	{
		super();
		m_root = new FilePath(root);
		m_state = OpenState.UNINITIALIZED;
		m_currentDir = new FilePath("");
		m_dirStack = new Stack<FilePath>();
	}

	/**
	 * Gets the path on the local machine corresponding to the root of the file
	 * system.
	 * 
	 * @return The path
	 */
	/* @ pure non_null @ */ public FilePath getRoot()
	{
		return m_root;
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		Path fp_filename = getPath(filename);
		File f = fp_filename.toFile();
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(f);
		}
		catch (FileNotFoundException e)
		{
			throw new FileSystemException(e);
		}
		return fos;
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		Path fp_filename = getPath(filename);
		File f = fp_filename.toFile();
		FileInputStream fis;
		BufferedInputStream bis;
		try
		{
			fis = new FileInputStream(f);
			bis = new BufferedInputStream(fis);
		}
		catch (FileNotFoundException e)
		{
			throw new FileSystemException(e);
		}
		return bis;
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		m_dirStack.push(m_currentDir);
		m_currentDir = m_currentDir.chdir(path);
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		chdir(path);
	}

	@Override
	public void popd() throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		if (m_dirStack.isEmpty())
		{
			m_currentDir = new FilePath("");
		}
		else
		{
			m_currentDir = m_dirStack.pop();
		}
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		try
		{
			Files.deleteIfExists(getPath(path));
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		if (!Files.exists(getPath(path)))
		{
			try
			{
				Files.createDirectory(getPath(path));
			}
			catch (IOException e)
			{
				throw new FileSystemException(e);
			}
		}
	}

	@Override
	public String pwd() throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		return m_currentDir.toString();
	}

	@Override
	public HardDisk open() throws FileSystemException
	{
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
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.CLOSED;
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		return ls(m_currentDir.toString());
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		try
		{
			Stream<Path> contents = Files.list(getPath(path));
			List<String> files = new ArrayList<String>();
			contents.forEach(x -> files.add(x.getFileName().toString()));
			contents.close();
			return files;
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		return Files.isDirectory(getPath(path));
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		Path p = getPath(path);
		return Files.exists(p) && !Files.isDirectory(p);
	}

	@Override
	public long getSize(String path) throws FileSystemException
	{
		Path p = getPath(path);
		try
		{
			return Files.size(p);
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		try
		{
			Files.deleteIfExists(getPath(path));
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	/**
	 * Gets the absolute path corresponding to a path within the file system
	 * 
	 * @param path
	 *          The path
	 * @return The absolute path on the local machine
	 */
	protected Path getPath(String path)
	{
		FilePath fp = m_currentDir.chdir(path);
		if (fp.isAbsolute())
		{
			return Paths.get(m_root.toString() + fp.toString());
		}
		return Paths.get(m_root.toString() + SLASH + fp.toString());
	}

	protected static void deleteDirectoryRecursion(Path path) throws IOException
	{
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
		{
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path))
			{
				for (Path entry : entries)
				{
					deleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}

	@Override
	protected ReifiedFileSystem createReifiedFileSystem(Object token)
	{
		return new ReifiedFileSystem()
		{
			private boolean m_closed = false;

			private void checkOpen() throws FileSystemException
			{
				if (m_closed)
				{
					throw new FileSystemException("Reified file system is closed");
				}
			}

			@Override
			public Path toLocalPath(String path) throws FileSystemException
			{
				checkOpen();
				// HardDisk.getPath already resolves against root + current dir
				return HardDisk.this.getPath(path);
			}

			@Override
			public void commit() throws FileSystemException
			{
				checkOpen();
				// Identity reification: nothing to sync
			}

			@Override
			public FileSystem open() throws FileSystemException
			{
				checkOpen();
				// Optional: either delegate or no-op. Delegating is usually fine.
				HardDisk.this.open();
				return this;
			}

			@Override
			public List<String> ls() throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.ls();
			}

			@Override
			public List<String> ls(String path) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.ls(path);
			}

			@Override
			public boolean isDirectory(String path) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.isDirectory(path);
			}

			@Override
			public boolean isFile(String path) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.isFile(path);
			}

			@Override
			public long getSize(String path) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.getSize(path);
			}

			@Override
			public OutputStream writeTo(String filename) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.writeTo(filename);
			}

			@Override
			public InputStream readFrom(String filename) throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.readFrom(filename);
			}

			@Override
			public void chdir(String path) throws FileSystemException
			{
				checkOpen();
				HardDisk.this.chdir(path);
			}

			@Override
			public void pushd(String path) throws FileSystemException
			{
				checkOpen();
				HardDisk.this.pushd(path);
			}

			@Override
			public void popd() throws FileSystemException
			{
				checkOpen();
				HardDisk.this.popd();
			}

			@Override
			public void mkdir(String path) throws FileSystemException
			{
				checkOpen();
				HardDisk.this.mkdir(path);
			}

			@Override
			public void rmdir(String path) throws FileSystemException
			{
				checkOpen();
				HardDisk.this.rmdir(path);
			}

			@Override
			public void delete(String path) throws FileSystemException
			{
				checkOpen();
				HardDisk.this.delete(path);
			}

			@Override
			public String pwd() throws FileSystemException
			{
				checkOpen();
				return HardDisk.this.pwd();
			}

			/**
			 * Releases the lease and ends the reification. For an identity reification,
			 * this should NOT close the underlying HardDisk (caller may want to keep it open).
			 */
			@Override
			public void close() throws FileSystemException
			{
				if (m_closed)
				{
					return;
				}
				m_closed = true;

				// Default rollback if not committed (no-op here anyway)
				// if (!m_committed) { ... }

				// Release exclusive lease held by this reified wrapper
				releaseLease(token);
			}

			@Override
			public void release() throws FileSystemException {
				// TODO Auto-generated method stub
				
			}
		};
	}
}
