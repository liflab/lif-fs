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
package ca.uqac.lif.fs.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;

/**
 * A file system giving access to resources over an FTP connection.
 * @author Sylvain Hallé
 */
public class FtpConnection implements FileSystem
{
	/**
	 * The FTPClient object used to handle the connection.
	 */
	/*@ null @*/ protected FTPClient m_client;

	/**
	 * The name of the server to connect to.
	 */
	/*@ non_null @*/ protected String m_server;

	/**
	 * The TCP port on the server to connect to.
	 */
	protected int m_port;

	/**
	 * The username used to log in.
	 */
	/*@ non_null @*/ protected String m_username;

	/**
	 * The password used to log in.
	 */
	/*@ non_null @*/ protected String m_password;

	/**
	 * The path representing the current directory.
	 */
	/*@ non_null @*/ protected FilePath m_currentDir;

	/**
	 * A stack containing the history of current directories.
	 */
	/*@ non_null @*/ protected Stack<FilePath> m_dirStack;
	
	/**
	 * An optional command listener used to watch the commands sent over the FTP
	 * connection.
	 */
	/*@ null @*/ protected ProtocolCommandListener m_commandListener;

	/**
	 * Creates a new FTP connection file system.
	 * @param server The name of the server to connect to
	 * @param port The TCP port on the server to connect to
	 * @param username The username used to log in
	 * @param password The password used to log in
	 */
	public FtpConnection(/*@ non_null @*/ String server, int port, /*@ non_null @*/ String username, /*@ non_null @*/ String password)
	{
		super();
		m_client = null;
		m_server = server;
		m_port = port;
		m_username = username;
		m_password = password;
		m_currentDir = new FilePath("");
		m_dirStack = new Stack<FilePath>();
		m_commandListener = null;
	}

	@Override
	public void open() throws FileSystemException
	{
		if (m_client != null)
		{
			throw new FileSystemException("A connection is alerady open");
		}
		m_client = new FTPClient();
		if (m_commandListener != null)
		{
			m_client.addProtocolCommandListener(m_commandListener);
		}
		
		int reply = -1;
		try
		{
			m_client.connect(m_server, m_port);
			reply = m_client.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				throw new FileSystemException("FTP server refused connection");
			}
			if (!m_client.login(m_username, m_password))
			{
				disconnect();
				throw new FileSystemException("Invalid username or password");
			}
			m_client.enterLocalPassiveMode();
			m_currentDir = new FilePath(m_client.printWorkingDirectory());
		}
		catch (IOException e)
		{
			disconnect();
			throw new FileSystemException(e);
		}
	}

	/**
	 * Attempts to disconnect the FTP client.
	 * @throws FileSystemException Thrown if the disconnection did not proceed
	 * successfully
	 */
	protected void disconnect() throws FileSystemException
	{
		if (m_client != null && m_client.isConnected())
		{
			try
			{
				m_client.disconnect();
			}
			catch (IOException ioe)
			{
				throw new FileSystemException(ioe);
			}
		}
	}

	@Override
	public List<String> ls() throws FileSystemException
	{
		if (m_client == null)
		{
			throw new FileSystemException("Client not connected");
		}
		try
		{
			FTPFile[] files = m_client.listFiles();
			List<String> listing = new ArrayList<String>();
			for (FTPFile f : files)
			{
				listing.add(f.getName());
			}
			return listing;
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public List<String> ls(String path) throws FileSystemException
	{
		if (m_client == null)
		{
			throw new FileSystemException("Client not connected");
		}
		try
		{
			FTPFile[] files = m_client.listFiles(path);
			List<String> listing = new ArrayList<String>();
			for (FTPFile f : files)
			{
				listing.add(f.getName());
			}
			return listing;
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public boolean isDirectory(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			FTPFile f = m_client.mlistFile(fp.toString());
			if (f == null)
			{
				return false;
			}
			return f.isDirectory();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public boolean isFile(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			FTPFile f = m_client.mlistFile(fp.toString());
			if (f == null)
			{
				return false;
			}
			return f.isFile();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	@Override
	public long getSize(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			FTPFile f = m_client.mlistFile(fp.toString());
			if (f == null)
			{
				throw new FileSystemException("File does not exist");
			}
			return f.getSize();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public OutputStream writeTo(String filename) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(filename);
		try
		{
			return new FtpOutputStream(m_client.storeFileStream(fp.toString()));
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public InputStream readFrom(String filename) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(filename);
		try
		{
			return new FtpInputStream(m_client.retrieveFileStream(fp.toString()));
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void chdir(String path) throws FileSystemException
	{
		m_dirStack.push(m_currentDir);
		m_currentDir = m_currentDir.chdir(path);
		boolean success = false;
		try
		{
			success = m_client.changeWorkingDirectory(m_currentDir.toString());
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
		if (!success)
		{
			throw new FileSystemException("Cannot change directory");
		}
	}

	@Override
	public void pushd(String path) throws FileSystemException
	{
		chdir(path);
	}

	@Override
	public void popd() throws FileSystemException
	{
		if (m_dirStack.isEmpty())
		{
			m_currentDir = new FilePath("");
		}
		else
		{
			m_currentDir = m_dirStack.pop();
		}
		boolean success = false;
		try
		{
			success = m_client.changeWorkingDirectory(m_currentDir.toString());
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
		if (!success)
		{
			throw new FileSystemException("Cannot change directory");
		}
	}

	@Override
	public void mkdir(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			m_client.mkd(fp.toString());
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void rmdir(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			m_client.rmd(fp.toString());
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public void delete(String path) throws FileSystemException
	{
		FilePath fp = m_currentDir.chdir(path);
		try
		{
			m_client.dele(fp.toString());
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}

	@Override
	public String pwd() throws FileSystemException
	{
		return m_currentDir.toString();
	}

	@Override
	public void close() throws FileSystemException
	{
		if (m_client == null)
		{
			throw new FileSystemException("Connection is alerady closed");
		}
		disconnect();
		m_client = null;
	}
	
	/**
	 * Adds a command listener.
	 * @param listener The listener
	 */
	public void addProtocolCommandListener(/*@ null @*/ ProtocolCommandListener listener)
	{
		m_commandListener = listener;
		if (m_client != null)
		{
			m_client.addProtocolCommandListener(listener);
		}
	}

	/**
	 * An input stream reading a file from a remote connection, and taking
	 * care of completing the associated FTP command after it is closed.
	 */
	protected class FtpInputStream extends InputStream
	{
		/**
		 * The underlying input stream.
		 */
		protected InputStream m_inStream;

		/**
		 * Creates a new FTP input stream.
		 * @param out_stream The underlying output stream
		 */
		public FtpInputStream(InputStream in_stream)
		{
			super();
			m_inStream = in_stream;
		}

		@Override
		public int read() throws IOException
		{
			return m_inStream.read();
		}

		@Override
		public void close() throws IOException
		{
			m_inStream.close();
			m_client.completePendingCommand();
		}
	}

	/**
	 * An output stream writing a file on a remote connection, and taking
	 * care of completing the associated FTP command after it is closed.
	 */
	protected class FtpOutputStream extends OutputStream
	{
		/**
		 * The underlying output stream.
		 */
		protected OutputStream m_outStream;

		/**
		 * Creates a new FTP output stream.
		 * @param out_stream The underlying output stream
		 */
		public FtpOutputStream(OutputStream out_stream)
		{
			super();
			m_outStream = out_stream;
		}

		@Override
		public void write(int b) throws IOException
		{
			m_outStream.write(b);
		}

		@Override
		public void close() throws IOException
		{
			m_outStream.close();
			m_client.completePendingCommand();
		}
	}
}
