package ca.uqac.lif.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
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
