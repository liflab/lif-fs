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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a path to a file in a file system. This class works in a
 * similar way as Java's built-in {@link Path} class. The main difference is
 * that a FilePath supports the ".." relative path element (representing a move
 * up in the directory hierarchy). A file path is immutable; all instances that
 * modify a path return a new path instance.
 * 
 * @author Sylvain Hallé
 */
public class FilePath
{
	/**
	 * The character used to separate path parts.
	 */
	public static final String SLASH = "/";
	
	/**
	 * The character sequence used to designate the "up" direction.
	 */
	public static final String UP = "..";
	
	/**
	 * The character sequence used to designate the current directory.
	 */
	public static final String DOT = ".";
	
	/**
	 * The elements of the path.
	 */
	protected final List<String> m_parts;
	
	/**
	 * A flag that determines if the path is absolute.
	 */
	protected final boolean m_isAbsolute;
	
	/**
	 * Creates a file path from a list of path elements.
	 * @param parts The list of path elements
	 * @param is_absolute A flag indicating if the path is an absolute one
	 */
	public FilePath(List<String> parts, boolean is_absolute)
	{
		super();
		m_parts = simplify(parts);
		m_isAbsolute = is_absolute;
	}
	
	/**
	 * Creates a file path by parsing a string.
	 * @param path The string containing the path
	 */
	public FilePath(String path)
	{
		super();
		if (path.compareTo(DOT) == 0)
		{
			m_parts = new ArrayList<String>();
			m_parts.add(".");
		}
		else
		{
			m_parts = simplify(fragment(path));
		}
		m_isAbsolute = path.isEmpty() || path.startsWith(SLASH);
	}
	
	/**
	 * Determines if this path is an absolute path.
	 * @return <tt>true</tt> if the path is absolute, <tt>false</tt> otherwise
	 */
	/*@ pure @*/ public boolean isAbsolute()
	{
		return m_isAbsolute;
	}
	
	/**
	 * Obtains a path instance relative to the current path.
	 * @param path A string corresponding to the path relative to the current
	 * path
	 * @return The new file path instance
	 */
	/*@ pure non_null @*/ public FilePath chdir(/*@ non_null @*/ FilePath path)
	{
		if (path.isAbsolute())
		{
			return path;
		}
		List<String> new_parts = new ArrayList<String>();
		new_parts.addAll(m_parts);
		new_parts.addAll(path.m_parts);
		return new FilePath(new_parts, m_isAbsolute);
	}
	
	/**
	 * Obtains a path instance relative to the current path.
	 * @param path A string corresponding to the path relative to the current
	 * path
	 * @return The new file path instance
	 */
	/*@ pure non_null @*/ public FilePath chdir(String path)
	{
		path = path.trim();
		if (path.startsWith(SLASH))
		{
			return new FilePath(path);
		}
		List<String> new_parts = new ArrayList<String>();
		new_parts.addAll(m_parts);
		new_parts.addAll(fragment(path));
		return new FilePath(new_parts, m_isAbsolute);
	}
	
	/**
	 * Produces a Java {@link Path} object corresponding to the current file
	 * path.
	 * @return The path object
	 */
	/*@ pure non_null @*/ public Path asPath()
	{
		return Paths.get(toString());
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		if (m_isAbsolute)
		{
			out.append(SLASH);
		}
		for (int i = 0; i < m_parts.size(); i++)
		{
			if (i > 0)
			{
				out.append(SLASH);
			}
			out.append(m_parts.get(i));
		}
		return out.toString();
	}
	
	/**
	 * Splits a string into path fragments according to the path separator. Any
	 * path fragment that is empty, or is only made of whitespace, is ignored.
	 * @param path The string
	 * @return The path fragments
	 */
	public static List<String> fragment(String path)
	{
		List<String> parts = new ArrayList<String>();
		String[] fragments = path.split(SLASH);
		for (String fragment : fragments)
		{
			fragment = fragment.trim();
			if (!fragment.isEmpty() && fragment.compareTo(DOT) != 0)
			{
				parts.add(fragment);	
			}
		}
		return parts;
	}
	
	/**
	 * Simplifies a list of path parts.
	 * @param parts The list of parts
	 * @return The simplified list
	 */
	public static List<String> simplify(List<String> parts)
	{
		List<String> new_parts = new ArrayList<String>();
		int eat = 0;
		for (int i = parts.size() - 1; i >= 0; i--)
		{
			String p = parts.get(i);
			if (p.compareTo(UP) == 0)
			{
				eat++;
			}
			else
			{
				if (eat == 0)
				{
					new_parts.add(0, p);
				}
				eat = Math.max(0, eat - 1);
			}
		}
		return new_parts;
	}
	
}
