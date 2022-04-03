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
public class FloppyDisk extends ThrottledFileSystem
{
	/**
	 * Enumeration of possible floppy types.
	 * <ul>
	 * <li><tt>F_360</tt>: a 360 kb 5¼" floppy</li>
	 * <li><tt>F_720</tt>: a 720 kb 3½" floppy</li>
	 * </ul>
	 */
	public enum FloppyType {F_360, F_720}

	/**
	 * Creates a new empty floppy disk of given size.
	 * @param fs The underlying file system to expose as a floppy
	 * @param s The type of floppy
	 */
	public FloppyDisk(FileSystem fs, FloppyType s)
	{
		super(fs);
		setSizeLimit(getMaxSize(s));
		setSpeedLimit(getMaxSpeed(s));
	}

	/**
	 * Gets the maximum transfer speed of the drive.
	 * @param floppy_type The floppy type
	 * @return The speed, in bytes per second
	 */
	protected long getMaxSpeed(FloppyType floppy_type)
	{
		switch (floppy_type)
		{
		case F_360:
			return 21000;
		case F_720:
			return 33000;
		default:
			return 33000;
		}
	}
	
	/**
	 * Gets the maximum size of the drive.
	 * @param floppy_type The floppy type
	 * @return The size, in bytes
	 */
	protected long getMaxSize(FloppyType floppy_type)
	{
		switch (floppy_type)
		{
		case F_360:
			return 360000;
		case F_720:
			return 720000;
		default:
			return 0;
		}
	}
}
