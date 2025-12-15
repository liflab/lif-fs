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

public abstract class AbstractReifiableFileSystem implements ReifiableFileSystem
{
	private Object m_leaseToken = null;
	private int m_bypass = 0;

	protected final Object acquireLease() throws FileSystemException
	{
		if (m_leaseToken != null)
		{
			throw new FileSystemException("File system is already reified");
		}
		m_leaseToken = new Object();
		return m_leaseToken;
	}

	protected final void releaseLease(Object token) throws FileSystemException
	{
		if (m_leaseToken != token)
		{
			throw new FileSystemException("Invalid lease token");
		}
		m_leaseToken = null;
	}

	protected final void checkNotReified() throws FileSystemException
	{
		if (m_leaseToken != null && m_bypass == 0)
		{
			throw new FileSystemException("File system is reified");
		}
	}

	protected final void beginBypass()
	{
		m_bypass++;
	}

	protected final void endBypass()
	{
		m_bypass--;
	}

	@Override
	public ReifiedFileSystem reify() throws FileSystemException
	{
		final Object token = acquireLease();
		final java.nio.file.Path tmpRoot;
		try
		{
			tmpRoot = java.nio.file.Files.createTempDirectory("lif-fs-reified-");
		}
		catch (java.io.IOException e)
		{
			releaseLease(token);
			throw new FileSystemException(e);
		}

		// Default: generic tempdir-backed reification
		return new TmpReifiedFileSystem(/* backing */ this, /* tmpRoot */ tmpRoot,
				/* beginBypass */ this::beginBypass, /* endBypass */ this::endBypass,
				/* releaseLease */ () -> {
					try
					{
						releaseLease(token);
					}
					catch (FileSystemException e)
					{
						throw new RuntimeException(e);
					}
				});
	}
}
