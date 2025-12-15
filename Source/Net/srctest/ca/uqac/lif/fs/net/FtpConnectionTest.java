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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.net.PrintCommandListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import ca.uqac.lif.fs.FileSystemException;

/**
 * Unit tests for {@link FtpConnection}.
 */
public class FtpConnectionTest
{
	private FakeFtpServer fakeFtpServer;

	@Before
	public void setup() throws IOException
	{
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));
		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/data"));
		fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
		fileSystem.add(new DirectoryEntry("/data/foo"));
		fileSystem.add(new FileEntry("/data/foo/somefile.txt", "The quick brown fox"));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(0);
		fakeFtpServer.start();
	}

	@Test
	public void test1() throws FileSystemException
	{
		FtpConnection conn = new FtpConnection("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
		conn.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		conn.open();
		assertEquals("/data", conn.pwd());
		conn.close();
	}
	
	@Test
	public void test2() throws FileSystemException
	{
		FtpConnection conn = new FtpConnection("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
		conn.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		conn.open();
		List<String> listing = conn.ls();
		assertEquals(2, listing.size());
		assertTrue(listing.contains("foobar.txt"));
		assertTrue(listing.contains("foo"));
		conn.chdir("/data/foo");
		listing = conn.ls();
		assertEquals(1, listing.size());
		assertTrue(listing.contains("somefile.txt"));
		conn.close();
	}
	
	@Test
	public void test3() throws FileSystemException
	{
		FtpConnection conn = new FtpConnection("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
		conn.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		conn.open();
		Scanner s = new Scanner(conn.readFrom("/data/foobar.txt"));
		assertTrue(s.hasNextLine());
		String line = s.nextLine();
		assertEquals("abcdef 1234567890", line);
		assertFalse(s.hasNextLine());
		s.close();
		conn.close();
	}
	
	@Test
	public void test4() throws FileSystemException
	{
		FtpConnection conn = new FtpConnection("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
		conn.open();
		PrintStream ps = new PrintStream(conn.writeTo("/data/newfile.txt"));
		ps.print("The quick brown fox");
		ps.close();
		assertTrue(fakeFtpServer.getFileSystem().exists("/data/newfile.txt"));
		assertEquals(19, fakeFtpServer.getFileSystem().getEntry("/data/newfile.txt").getSize());
		conn.close();
	}
	
	@Test
	public void test5() throws FileSystemException
	{
		FtpConnection conn = new FtpConnection("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
		conn.open();
		conn.mkdir("/data/mynewdir1");
		assertTrue(fakeFtpServer.getFileSystem().exists("/data/mynewdir1"));
		conn.rmdir("/data/mynewdir1");
		assertFalse(fakeFtpServer.getFileSystem().exists("/data/mynewdir1"));
		conn.close();
	}

	@After
	public void teardown() throws IOException
	{
		fakeFtpServer.stop();
	}
}
