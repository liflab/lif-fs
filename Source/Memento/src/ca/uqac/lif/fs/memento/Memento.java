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

import ca.uqac.lif.fs.FileSystemException;

/**
 * Interface defining methods for reading and writing an object to an
 * abstract location.
 * @param <T> The type of the objects to retrieve
 * @author Sylvain Hallé
 */
public interface Memento<T>
{
	/**
	 * Reads the memento.
	 * @return The memento
	 * @throws IOException Thrown if the operation could not succeed
	 */
	public T read() throws FileSystemException;
	
	/**
	 * Writes the memento.
	 * @param b The memento
	 * @throws FileSystemException Thrown if the operation could not succeed
	 */
	public void write(T b) throws FileSystemException;
}
