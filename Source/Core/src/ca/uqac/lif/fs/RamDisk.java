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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A file system where files are stored in memory.
 * @author Sylvain Hallé
 */
public class RamDisk implements FileSystem
{
	/**
	 * The path representing the current directory within the file system.
	 */
	/*@ non_null @*/ protected FilePath m_currentDir;

	/**
	 * A stack containing the history of current directories.
	 */
	/*@ non_null @*/ protected Stack<FilePath> m_dirStack;

	/**
	 * The root folder of the ramdisk.
	 */
	/*@ non_null @*/ protected RamdiskFolderNode m_root;

	/**
	 * The current state of the ramdisk.
	 */
	protected OpenState m_state;

	/**
	 * Creates a new empty ramdisk.
	 */
	public RamDisk()
	{
		super();
		m_currentDir = new FilePath("");
		m_root = new RamdiskFolderNode("");
		m_state = OpenState.UNINITIALIZED;
		m_dirStack = new Stack<FilePath>();
	}

	@Override
	public void open() throws FileSystemException
	{
		if (m_state == OpenState.CLOSED)
		{
			throw new FileSystemException("File system has already been closed");
		}
		m_state = OpenState.OPEN;
	}

	@Override
	public void close() throws FileSystemException
	{
		m_state = OpenState.CLOSED;
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		return ls(m_currentDir);
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		return ls(m_currentDir.chdir(path));
	}

	protected List<String> ls(FilePath path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		if (rn == null || !(rn instanceof RamdiskFolderNode))
		{
			throw new FileSystemException("No such directory");
		}
		List<String> listing = new ArrayList<String>();
		for (RamdiskNode child : rn.getChildren())
		{
			listing.add(child.getName());
		}
		return listing;
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		return rn instanceof RamdiskFolderNode;
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		return rn instanceof RamdiskFileNode;
	}
	
	@Override
	public long getSize(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		if (!(rn instanceof RamdiskFileNode))
		{
			throw new FileSystemException("Path is a directory");
		}
		return ((RamdiskFileNode) rn).m_contents.length;
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		FilePath path = m_currentDir.chdir(filename);
		RamdiskFileNode rfn = createFileNode(path);
		return new RamdiskFileOutputStream(rfn);
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		FilePath path = m_currentDir.chdir(filename);
		RamdiskNode rn = locate(path);
		if (!(rn instanceof RamdiskFileNode))
		{
			throw new FileSystemException(path + " is not a file"); 
		}
		RamdiskFileNode rfn = (RamdiskFileNode) rn;
		ByteArrayInputStream bais = new ByteArrayInputStream(rfn.m_contents);
		return bais;
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		m_dirStack.push(m_currentDir);
		m_currentDir = m_currentDir.chdir(path);
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		chdir(path);
	}

	@Override
	public void popd() throws FileSystemException
	{
		if (m_state != OpenState.OPEN)
		{
			throw new FileSystemException("File system is not open");
		}
		if (m_dirStack.isEmpty())
		{
			m_currentDir = new FilePath("");
		}
		else
		{
			m_currentDir = m_dirStack.pop();
		}
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		createFolderNode(fp);
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		if (rn == null || !(rn instanceof RamdiskFolderNode))
		{
			throw new FileSystemException("No such directory");
		}
		RamdiskNode rn_parent = rn.getParent();
		if (rn_parent != null)
		{
			rn_parent.deleteChild(rn);
		}
		else
		{
			rn.deleteChildren();
		}
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		RamdiskNode rn = locate(fp);
		if (rn == null || !(rn instanceof RamdiskFileNode))
		{
			throw new FileSystemException("No such file");
		}
		RamdiskNode rn_parent = rn.getParent();
		if (rn_parent != null)
		{
			rn_parent.deleteChild(rn);
		}
	}

	/**
	 * Locates a ramdisk node corresponding to a given path.
	 * @param path The path
	 * @return The ramdisk node, or <tt>null</tt> if no node corresponds to this
	 * path
	 */
	protected RamdiskNode locate(FilePath path)
	{
		List<String> parts = new ArrayList<String>(path.m_parts.size());
		parts.addAll(path.m_parts);
		return locate(m_root, parts);
	}

	/**
	 * Locates a ramdisk node corresponding to a given path, from a given
	 * starting point.
	 * @param current The starting point
	 * @param parts The parts of the path
	 * @return The ramdisk node, or <tt>null</tt> if no node corresponds to this
	 * path
	 */
	protected RamdiskNode locate(RamdiskNode current, List<String> parts)
	{
		if (parts.isEmpty())
		{
			return current;
		}
		String wanted = parts.get(0);
		for (RamdiskNode child : current.getChildren())
		{
			if (wanted.compareTo(child.getName()) == 0)
			{
				parts.remove(0);
				return locate(child, parts);
			}
		}
		return null;
	}

	/**
	 * Creates a new folder node corresponding to a path. 
	 * @param fp The path
	 * @throws FileSystemException Thrown if the path cannot be created
	 */
	protected void createFolderNode(FilePath fp) throws FileSystemException
	{
		RamdiskFolderNode rfn = m_root;
		for (int i = 0; i < fp.m_parts.size(); i++)
		{
			RamdiskNode found = null;
			String part_name = fp.m_parts.get(i);
			for (RamdiskNode child : rfn.getChildren())
			{
				if (part_name.compareTo(child.getName()) == 0)
				{
					found = child;
					break;
				}
			}
			if (found == null)
			{
				RamdiskFolderNode new_node = new RamdiskFolderNode(part_name);
				rfn.addChild(new_node);
				rfn = new_node;
			}
			else
			{
				if (!(found instanceof RamdiskFolderNode))
				{
					throw new FileSystemException("Invalid path");
				}
				rfn = (RamdiskFolderNode) found;
			}
		}
	}

	/**
	 * Creates a new file node corresponding to a path. 
	 * @param fp The path
	 * @throws FileSystemException Thrown if the path cannot be created
	 */
	protected RamdiskFileNode createFileNode(FilePath fp) throws FileSystemException
	{
		RamdiskFolderNode rfn = m_root;
		for (int i = 0; i < fp.m_parts.size(); i++)
		{
			RamdiskNode found = null;
			String part_name = fp.m_parts.get(i);
			for (RamdiskNode child : rfn.getChildren())
			{
				if (part_name.compareTo(child.getName()) == 0)
				{
					found = child;
					break;
				}
			}
			if (found == null)
			{
				if (i < fp.m_parts.size() - 1)
				{
					RamdiskFolderNode new_node = new RamdiskFolderNode(part_name);
					rfn.addChild(new_node);
					rfn = new_node;
				}
				else // Last node
				{
					RamdiskFileNode new_node = new RamdiskFileNode(part_name, new byte[0]);
					rfn.addChild(new_node);
					return new_node;
				}
			}
			else
			{
				if (i < fp.m_parts.size() - 1)
				{
					if (!(found instanceof RamdiskFolderNode))
					{
						throw new FileSystemException("Invalid path");
					}
					rfn = (RamdiskFolderNode) found;
				}
				else
				{
					if (!(found instanceof RamdiskFileNode))
					{
						throw new FileSystemException("A folder with this name already exists");
					}
					return (RamdiskFileNode) found;
				}
			}
		}
		return null;
	}

	@Override
	public String pwd()
	{
		return m_currentDir.toString();
	}

	protected static abstract class RamdiskNode
	{
		protected RamdiskNode m_parent;

		protected List<RamdiskNode> m_children;

		protected String m_name;

		public RamdiskNode(String name)
		{
			super();
			m_parent = null;
			m_children = new ArrayList<RamdiskNode>();
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public RamdiskNode getParent()
		{
			return m_parent;
		}

		public List<RamdiskNode> getChildren()
		{
			return m_children;
		}

		public void setParent(RamdiskNode node)
		{
			m_parent = node;
		}

		public void addChild(RamdiskNode node)
		{
			m_children.add(node);
			node.m_parent = this;
		}

		public void deleteChildren()
		{
			m_children.clear();
		}

		public void deleteChild(RamdiskNode node)
		{
			m_children.remove(node);
			node.m_parent = null;
		}

		@Override
		public String toString()
		{
			return m_name;
		}
	}

	protected static class RamdiskFolderNode extends RamdiskNode
	{
		public RamdiskFolderNode(String name)
		{
			super(name);
		}

		@Override
		public String toString()
		{
			return m_name + "/";
		}
	}

	protected static class RamdiskFileNode extends RamdiskNode
	{
		/**
		 * The contents of the file.
		 */
		protected byte[] m_contents;

		public RamdiskFileNode(String name, byte[] contents)
		{
			super(name);
			m_contents = contents;
		}

		public byte[] getContents()
		{
			return m_contents;
		}
	}

	protected class RamdiskFileOutputStream extends OutputStream
	{
		protected ByteArrayOutputStream m_contents;

		protected RamdiskFileNode m_node;

		public RamdiskFileOutputStream(RamdiskFileNode node)
		{
			super();
			m_contents = new ByteArrayOutputStream();
			m_node = node;
		}

		@Override
		public void write(int b) throws IOException
		{
			m_contents.write(b);
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			m_contents.close();
			m_node.m_contents = m_contents.toByteArray();
		}
	}
}
