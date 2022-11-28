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
import java.nio.file.Files;

/**
 * A local file system whose root is a one-time folder located in the local
 * system's temporary directory. By default, this folder and all its contents
 * are deleted when the file system is closed.
 * 
 * @author Sylvain Hallé
 */
public class TempFolder extends HardDisk
{
	/**
	 * A flag indicating whether the temporary folder should be deleted when the
	 * file system is closed.
	 */
	protected boolean m_deleteOnClose;
	
	/**
	 * The prefix to give to the temporary folder name.
	 */
	protected String m_prefix;
	
	/**
	 * Creates a new temporary file system.
	 * @throws IOException Thrown if the folder could not be created
	 */
	public TempFolder() throws IOException
	{
		this("");
	}
	
	/**
	 * Creates a new temporary file system, giving a prefix to the created
	 * folder.
	 * @param prefix The prefix
	 * @throws IOException Thrown if the folder could not be created
	 */
	public TempFolder(String prefix) throws IOException
	{
		super(Files.createTempDirectory(prefix).toString());
		m_deleteOnClose = true;		
	}
	
	/**
	 * Sets whether the temporary folder should be deleted when the file system
	 * is closed.
	 * @param b Set to <tt>true</tt> to delete folder, <tt>false</tt> to leave it
	 */
	public void deleteOnClose(boolean b)
	{
		m_deleteOnClose = b;
	}
	
	@Override
	public TempFolder open() throws FileSystemException
	{
		super.open();
		return this;
	}
	
	@Override
	public void close() throws FileSystemException
	{
		super.close();
		if (m_deleteOnClose)
		{
			try
			{
				deleteDirectoryRecursion(m_root.asPath());
			}
			catch (IOException e)
			{
				throw new FileSystemException(e);
			}
		}
	}
}
