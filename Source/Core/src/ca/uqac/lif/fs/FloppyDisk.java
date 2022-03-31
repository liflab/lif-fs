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
 * A file system simulating the operation of an old floppy disk. A floppy is
 * limited in size to either 360 kb or 720 kb. In addition, read and write
 * operations are artificially slowed down to reproduce the transfer speed
 * of these physical drives.
 * 
 * @author Sylvain Hallé
 */
public class FloppyDisk extends HardDisk
{
	public enum Size {F_360, F_720}
	
	/**
	 * The maximum size of the file system, in bytes.
	 */
	protected int m_maxSize;
	
	/**
	 * Creates a new empty floppy disk of given size.
	 * @param root The root folder 
	 * @param s
	 */
	public FloppyDisk(String root, Size s)
	{
		super(root);
		switch (s)
		{
		case F_360:
			m_maxSize = 360;
		case F_720:
			m_maxSize = 720;
		}
	}
}
