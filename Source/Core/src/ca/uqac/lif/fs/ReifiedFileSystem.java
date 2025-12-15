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

public interface ReifiedFileSystem extends FileSystem, AutoCloseable
{
	/**
	 * Returns a concrete OS path corresponding to a path in this file system. The
	 * path is guaranteed to exist.
	 */
	java.nio.file.Path toLocalPath(String path) throws FileSystemException;

	/**
	 * Commits all changes back to the backing file system.
	 */
	void commit() throws FileSystemException;

	/**
	 * Releases the lease and cleans up resources. Default semantics: rollback if
	 * commit() was not called.
	 */
	void release() throws FileSystemException;
}
