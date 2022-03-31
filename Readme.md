A `FileSystem` object provides a handful of methods for manipulating files:

- `open` and `close` start and end the interaction with the file system. Any other operation is invalid if invoked before `open` or after `close`.
- `writeTo` is given a filename and returns an `OutputStream` to which data can be written. Closing the stream causes the file to be written.
- `readFrom` is given a filename and returns an `InputStream` from which data can be read.
- `rm` deletes a file
- `mkdir` and `rmdir` respectively create and delete a folder.
- `ls` lists the contents of a folder.
- `chdir` changes the current directory. All operations are performed relatively to this current directory.

This interface abstracts away the actual *physical* location of the files being manipulated, as well as the specific *means* by which these files are stored. A consumer of a `FileSystem` object needs not be aware that the underlying file system is concretely stored as...

- a local drive
- an FTP connection
- a zip file
- a temporary folder
- a tar archive
- a database table
- a ramdisk
- a Java `properties` file
- a set of WebDAV resources

Multiple uses. One can pass to an object a `FileSystem` instance where...

- the root folder is a folder of some other file system, performing a function similar to the `chroot` command
- all write operations are forbidden or do nothing; this ensures that the object has read-only access to the resources
- only a subset of the underlying files that are actually available is made visible, thereby enforcing a form of access control

Write files to a zip that is uploaded by FTP
--------------------------------------------

This example shows that a file system can be given a stream from another file system instance.

```java
// Open an FTP connection and navigate to a remote folder
FileSystem ftp = new FtpConnection("10.1.2.3", "user", "pass");
ftp.chdir("/home/sylvain/myfolder");

// Get an output stream to write to a file
OutputStream os = ftp.writeTo("myarchive.zip");

// Open a zip file system, and instruct it to write itself in this output stream
FileSystem z = new WriteZipFile(os);

// Create folders within the archive
z.mkdir("foo");
z.mkdir("bar");

// Write a first file
{
  PrintStream ps = new PrintStream(z.writeTo("/foo/foobar.txt"));
  ps.print("Hello world");
  ps.close(); // Closing ps adds the file to the archive
}

// Write a second file
{
  z.chdir("bar");
  PrintStream ps = new PrintStream(z.writeTo("foobaz.txt"));
  ps.print("The quick brown fox");
  ps.close(); // Closing ps adds the file to the archive
}

// Closing z writes the zip file in the output stream
z.close();

// Closing os sends the zip file by FTP
os.close();

// Closing ftp ends the connection
ftp.close();
```

Note how the `WriteZipFile` object has no idea it is writing its contents into a stream that comes from an FTP file system.

Read and write files as local database entries
----------------------------------------------

This example shows that the `JdbcFileSystem` object stores files as entries in a table. A column called "name" contains the filename, and a column called "content" contains a BLOB with the contents of the file. Users of the `JdbcFileSystem` are given access to these files as if they were stored in a hierarchical file system.

```java
FileSystem db = new JdbcFileSystem("localhost", "user", "pass", "dbname", "table");
db.chdir("/my/folder");

// Write a file
PrintStream ps = new PrintStream(z.writeTo("foo/foobar.txt"));
ps.print("Hello world");
ps.close(); // Closing ps adds the file to the table in the database

// Read another file
db.chdir("../bar");
Scanner s = new Scanner(db.readFrom("foobaz.txt"));
while (s.hasNextLine()) {
  // Do stuff with the file's content
}
s.close();

// Closing db ends the connection with the database
db.close();
```

Recursively dump an FTP folder to the local file system
-------------------------------------------------------

```java
// Open a file system to some local folder
FileSystem local = new HardDisk("/home/sylvain/somefolder");
local.open();

// Open a file system to some remote folder
FileSystem remote = new FtpConnection("10.1.2.3", "user", "pass");
remote.open();
remote.chdir("some/other/folder");

// Recursively copy all files from remote into local
FileUtils.copy(remote, local);

// Close the connections
local.close();
remote.close();
```