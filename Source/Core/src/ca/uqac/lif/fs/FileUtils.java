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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils
{
	/**
	 * Private constructor, used to prevent instantiating this class.
	 */
	private FileUtils()
	{
		super();
	}
	
	public static List<String> ls(FileSystem fs, String path, String pattern) throws FileSystemException
	{
		List<String> listing = fs.ls(path);
		
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
}
