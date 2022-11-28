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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A set of static methods for common operations on files and file systems.
 * @author Sylvain Hallé
 */
public class FileUtils
{
	/**
	 * Private constructor, used to prevent instantiating this class.
	 */
	private FileUtils()
	{
		super();
	}
	
	/**
	 * Writes an array of bytes into a file on a file system.
	 * @param fs The file system to interact with
	 * @param content The byte array to write
	 * @param path The path of the file to write to
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static void writeBytesTo(FileSystem fs, byte[] content, String path) throws FileSystemException
	{
		OutputStream os = fs.writeTo(path);
		copy(toStream(content), os);
		try
		{
			os.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * Writes a string into a file on a file system.
	 * @param fs The file system to interact with
	 * @param content The byte array to write
	 * @param path The path of the file to write to
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static void writeStringTo(FileSystem fs, String content, String path) throws FileSystemException
	{
		writeBytesTo(fs, content.getBytes(), path);
	}
	
	/**
	 * Reads an array of bytes from a file on a file system.
	 * @param fs The file system to interact with
	 * @param path The path of the file to read from
	 * @return The array of bytes
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static byte[] readBytesFrom(FileSystem fs, String path) throws FileSystemException
	{
		InputStream is = fs.readFrom(path);
		byte[] out = toBytes(is);
		try
		{
			is.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
		return out;
	}
	
	/**
	 * Reads a character string from a file on a file system.
	 * @param fs The file system to interact with
	 * @param path The path of the file to read from
	 * @return The character string
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static String readStringFrom(FileSystem fs, String path) throws FileSystemException
	{
		return new String(readBytesFrom(fs, path));
	}
	
	/**
	 * Filters the files of a directory listing according to a filename pattern.
	 * @param fs The file system to list
	 * @param path The path in the file system to list
	 * @param pattern The filename pattern, expressed as a regular expression
	 * @return The list of files matching the pattern
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static List<String> ls(FileSystem fs, String path, String pattern) throws FileSystemException
	{
		List<String> listing = fs.ls(path);
		Iterator<String> it = listing.iterator();
		while (it.hasNext())
		{
			String file = it.next();
			if (!file.matches(pattern))
			{
				it.remove();
			}
		}
		return listing;
	}
	
	/**
	 * Reads data from an input stream and puts it into an array of bytes.
	 * @param is The input stream
	 * @return The array of bytes
	 * @throws FileSystemException Thrown if the operation could not
	 * proceed
	 */
	public static byte[] toBytes(InputStream is) throws FileSystemException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		return baos.toByteArray();
	}

	/**
	 * Copies the content of an input stream to an output stream. The method
	 * assumes that the input streams are open, and lets the user close them
	 * manually.
	 * @param from The input stream to read from
	 * @param to The output stream to write to
	 * @throws FileSystemException Thrown if the copying process could not
	 * proceed
	 */
	public static void copy(InputStream from, OutputStream to) throws FileSystemException
	{
		int length;
		byte[] bytes = new byte[1024];
		try
		{
			while ((length = from.read(bytes)) != -1) 
			{
				to.write(bytes, 0, length);
			}
		}
		catch (IOException ex) 
		{
			throw new FileSystemException(ex); 
		}
	}

	/**
	 * Copies all the files and folders of a file system into another file
	 * system.
	 * @param from The file system to copy from
	 * @param to The file system to copy to
	 * @throws FileSystemException Thrown if the copying process could not
	 * proceed for some reason
	 */
	public static void copy(FileSystem from, FileSystem to) throws FileSystemException
	{
		List<String> folders = new ArrayList<String>();
		for (String e : from.ls())
		{
			if (from.isDirectory(e))
			{
				folders.add(e);
			}
			else
			{
				InputStream in = from.readFrom(e);
				OutputStream out = to.writeTo(e);
				copy(in, out);
				try
				{
					in.close();
					out.close();
				}
				catch (IOException ioe)
				{
					throw new FileSystemException(ioe);
				}
			}
		}
		for (String dir : folders)
		{
			to.mkdir(dir);
			from.pushd(dir);
			to.pushd(dir);
			copy(from, to);
			from.popd();
			to.popd();
		}
	}
	
	/**
	 * Gets the size of a folder, including that of all its files and
	 * sub-folders.
	 * @param fs The file system
	 * @param path The folder
	 * @return The size of the folder, in bytes
	 * @throws FileSystemException Thrown if the copying process could not
	 * proceed for some reason
	 */
	public static long getSize(FileSystem fs, String path) throws FileSystemException
	{
		fs.pushd(path);
		TotalSize ts = new TotalSize(fs);
		ts.crawl();
		fs.popd();
		return ts.getSize();
	}
	
	/**
	 * Makes an array of bytes accessible though an input stream.
	 * @param bytes The array of bytes
	 * @return An input stream open on this array of bytes
	 */
	public static InputStream toStream(byte[] bytes)
	{
		return new ByteArrayInputStream(bytes);
	}
	
	/**
	 * Makes a string accessible though an input stream.
	 * @param bytes The string
	 * @return An input stream open on the array of bytes corresponding to the
	 * string
	 */
	public static InputStream toStream(String s)
	{
		return new ByteArrayInputStream(s.getBytes());
	}
	
	/**
	 * Trims a string of its forward slash if it has one.
	 * @param s The string
	 * @return The trimmed string
	 */
	public static String trimSlash(String s)
	{
		if (!s.isEmpty() && s.startsWith("/"))
		{
			return s.substring(1);
		}
		return s;
	}
	
	/**
	 * Visitor calculating the total size of all files inside a folder and its
	 * sub-folders.
	 */
	protected static class TotalSize extends RecursiveListing
	{
		/**
		 * The total size of all visited files.
		 */
		protected long m_totalSize;

		/**
		 * Creates a new visitor.
		 * @param fs The file system to visit
		 */
		public TotalSize(FileSystem fs)
		{
			super(fs);
			m_totalSize = 0;
		}

		@Override
		protected void visit(FilePath filename) throws FileSystemException
		{
			m_totalSize += m_fs.getSize(filename.toString());
		}
		
		/**
		 * Gets the total size of all visited files.
		 * @return The size, in bytes
		 */
		public long getSize()
		{
			return m_totalSize;
		}
		
	}
}
