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
 * A memento that delegates its read/write operations to another internal
 * memento class. Descendants of the {@link FilterMemento} can override the
 * {@link #read()} and {@link #write(byte[]) write()} methods to apply
 * transformations to the underlying memento.
 * 
 * @author Sylvain Hallé
 */
public abstract class FilterMemento<U,T> implements Memento<U>
{
	/**
	 * The internal memento to which operations are delegated.
	 */
	protected final Memento<T> m_memento;
	
	/**
	 * Creates a new instance of the filter memento.
	 * @param m The internal memento to which operations are delegated
	 */
	public FilterMemento(Memento<T> m)
	{
		super();
		m_memento = m;
	}

	@Override
	public U read() throws FileSystemException
	{
		T t = m_memento.read();
		return transformTo(t);
	}

	@Override
	public void write(U b) throws FileSystemException
	{
		T t = transformFrom(b);
		m_memento.write(t);
	}
	
	protected abstract T transformFrom(U m);
	
	protected abstract U transformTo(T m);
}
