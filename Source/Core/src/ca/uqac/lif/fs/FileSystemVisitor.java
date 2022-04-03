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

/**
 * An object that explores the file and directory structure of a file system,
 * and calls a special method {@link #visit(FilePath)} on each element.
 * @author Sylvain Hallé
 *
 */
public abstract class FileSystemVisitor
{
	/**
	 * The file system to visit.
	 */
	/*@ non_null @*/ protected final FileSystem m_fs;
	
	/**
	 * Creates a new file system visitor.
	 * @param fs The file system to visit
	 */
	public FileSystemVisitor(/*@ non_null @*/ FileSystem fs)
	{
		super();
		m_fs = fs;
	}
	
	/**
	 * Starts the visit inside the file system.
	 * @throws FileSystemException Thrown if the operation could not proceed for
	 * some reason
	 */
	public abstract void crawl() throws FileSystemException;
	
	/**
	 * Visits a file in the file system.
	 * @param filename The name of the file
	 * @throws FileSystemException Thrown if the operation could not proceed for
	 * some reason
	 */
	protected abstract void visit(/*@ non_null @*/ FilePath filename) throws FileSystemException;
}
