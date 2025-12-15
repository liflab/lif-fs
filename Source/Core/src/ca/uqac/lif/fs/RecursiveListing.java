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

import java.util.ArrayList;
import java.util.List;

/**
 * A file system visitor that recursively enumerates all files within a file
 * system.
 * @author Sylvain Hallé
 */
public abstract class RecursiveListing extends FileSystemVisitor
{
	/**
	 * Creates a new file system visitor.
	 * @param fs The file system to visit
	 */
	public RecursiveListing(FileSystem fs)
	{
		super(fs);
	}

	@Override
	public void crawl() throws FileSystemException
	{
		crawl(new FilePath(""));
	}
	
	protected void crawl(FilePath path) throws FileSystemException
	{
		List<FilePath> folders = new ArrayList<FilePath>();
		for (String e : m_fs.ls(path.toString()))
		{
			FilePath new_path = path.chdir(e);
			if (m_fs.isDirectory(new_path.toString()))
			{
				folders.add(new_path);
			}
			else
			{
				visit(path.chdir(e));
			}
		}
		for (FilePath subdir : folders)
		{
			crawl(subdir);
		}
	}
}
