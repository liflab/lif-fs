A library for abstract file system operations
=============================================

Java provides various ways to access and manipulate files; however, depending on where these files are located, they are exposed through different classes and methods. If the files are on a local folder, they are accessed using the `File` class and its associated methods; if they are inside a zip file, the `ZipInputStream` and `ZipOutputStream` objects must be used according to a completely different workflow; if the same files are accessible over an FTP connection, yet other objects and methods are required.

The *lif-fs* library defines an interface called `FileSystem` that exposes files through a simple and **uniform** set of methods:

- `open` and `close` start and end the interaction with the file system
- `writeTo` is given a filename and returns an `OutputStream` to which data can be written
- `readFrom` is given a filename and returns an `InputStream` from which data can be read
- `rm` deletes a file
- `mkdir` and `rmdir` respectively create and delete a folder
- `ls` lists the contents of a folder
- `chdir` changes the current directory; `pushd` and `popd` allow a stack of current directories to be pushed/popped

Usage
-----

### Abstract access to resources

It is possible to pass a `FileSystem` instance to a method or an object. Consider the following method from some arbitrary class:

```java
public void doSomething(FileSystem fs) {
  FileUtils.copy(FileUtils.asBytes("Hello"), fs.writeTo("/foo.txt"));
}
```

We can see that this method writes the string "Hello" to some file called "foo.txt" in the root folder of `fs`. However, the method does not care where this file system resides, or how the file is actually stored. Depending on what `FileSystem` object is being passed, the file could be concretely stored as...

- a file on some folder of a local drive (`HardDisk`)
- a file sent over an FTP connection (`FtpConnection`)
- a file written into the contents of a zip file being created (`ReadZipFile`, `WriteZipFile`)
- a file written to a temporary folder (`TempFolder`), or an in-memory file system (`RamDisk`)
- nothing at all (`NoopFileSystem`)
- etc.

### Testing

This can be useful for **testing** purposes: for instance, a test case can pass a `RamDisk` instance to a system under test and examine what the system writes to it, instead of having it write files for real on the local machine. Similarly, testing a program that accesses files over a network no longer needs a setup to simulate the connection: if it accesses these files through a `FileSystem` object, any other file system can be passed instead of an `FtpConnection` at testing time (the file system can even be artificially throttled using the `ThrottledFileSystem` to simulate transfer speed). If the contents of the files being written is not necessary for the test, one can even pass the `NoopFileSystem` that writes nothing.

### Access control

As another interesting side effect, many file system objects of the library take as input another file system instance, or a stream from another file system instance. In this way, they can alter the way in which the resources of the underlying file system are accessed: modifying file contents or filenames, preventing access to some files, etc. For example, the `ReadOnlyFileSystem` disables all write access to the underlying file system. Passing this file system to an object ensures it will not succeed at writing into the target file system. One can also choose to expose only a folder of a file system as the root of some other file system, using the `Chroot` file system. The consumer of this object operates on that file system without being aware it is contained within another one.

### Customization

If the available file systems do not suit your needs, users are free to write their own. The `FilterFileSystem` class is useful for that purpose, as it delegates all its operations to another file system instance; descendants of this class may elect to override some of these methods to perform different operations. See the examples below for some cases of custom file systems.

Examples
--------

Here are some examples of the tasks that you can do by mixing together various instances of the `FileSystem` interface.

### Give read-only access to a folder of the local machine

In this example, a folder of the local machine is exposed as a root of some file system object. This file system is then passed to the `ReadOnlyFileSystem` object that disables all write access to the underlying file system. This example also shows a first use of the `FileUtils` object, whose method `toBytes` can turn an `InputStream` into an array of bytes.

```java
// Open file system
FileSystem fs = new ReadOnlyFileSystem(new HardDisk("/path/to/folder"));

// Change to folder; internally resolves to /path/to/folder/foo
fs.chdir("/foo");

// Read contents of a file: OK
byte[] contents = FileUtils.toBytes(fs.readFrom("bar.txt"));

// Try to write contents: throws an exception
OutputStream os = fs.writeTo("baz/somefile.txt");
```

### Give size-constrained access to a folder of the local machine

It is also possible to restrict the total space that a file system is allowed to use. In the example below, the `fs` instance will throw an exception whenever an operation makes the directory `/path/to/folder` occupy more than 1 MB.

```java
// Open file system
FileSystem fs = new ThrottledFileSystem(new HardDisk("/path/to/folder"));
fs.setSizeLimit(1000000);

// Get and output stream and try to write a big file
OutputStream os = fs.writeTo("foobar.txt");
os.write(new byte[2000000]); // Too big: throws an exception
```

### List files contained in a zip file obtained from an FTP connection

This example shows how a stream from a file system can be passed as the input of another file system object. Here, an FTP connection is open, and a zip file accessible through that connection is exposed as another file system, which can be transparently manipulated as any other.

```java
// Open an FTP connection and navigate to a remote folder
FileSystem ftp = new FtpConnection("10.1.2.3", "user", "pass");
ftp.open();
ftp.chdir("/home/sylvain/myfolder");

// Get a hold of a zip file inside the FTP repository
ReadZipFile zip = new ReadZipFile(ftp.readFrom("myarchive.zip"));
zip.open();
List<String> listing = zip.ls();

// Close resources
zip.close();
ftp.close();
```

In this example, it is worth noting that no file transfer is explicitly done. The transfer of the zip file is implicitly executed on the call to `zip.open()`. Also worthy of mention is the fact that `zip.ls()` fetches an in-memory cache of the archive's directory, and does not require the file to be tranferred multiple times.

### Mirror file operations on multiple systems

The `Mirror` file system replicates all its actions on any number of file systems. In the following example, we create a mirror file system where all write operations cause files to be written both to a local folder, and sent over an FTP connection to some remote location. A single call to `writeTo` creates the same file in both places.

```java
FileSystem mirror = new Mirror(
  new FtpConnection("10.1.2.3", "user", "pass"),
  new HardDisk("/path/to/my/folder")
);
mirror.open();
FileUtils.copy(FileUtils.asBytes("Hello"), mirror.writeTo("/blabla.txt"));
mirror.close();
```

An interesting side effect of `Mirror` is what it does on read operations: when a folder or a file is accessed, it queries each of its underlying file systems until one of them finds it. As a result, when used in read mode, `Mirror` "merges" multiple directory structures into one:

```java
FileSystem mirror = new Mirror(
  new FtpConnection("10.1.2.3", "user", "pass"),
  new HardDisk("/path/to/my/folder")
);
mirror.open();
byte[] contents = FileUtils.getBytes(mirror.readFrom("foobar.bin"));
mirror.close();
```

In this example, the consumer of `mirror` does not know if "foobar.bin" has been read from the local drive or retrieved from the FTP connection.

### Write files to a zip that is uploaded by FTP

In this more complex example, a file system open on an FTP connection is first created. Another file system is used to write files that will be stored into a zip file, which is written into a stream obtained from the FTP connection. Note how there is no explicit notion of "transferring" files: the transfer is implicit by the way streams are instantiated and passed. In other words, the `WriteZipFile` object has no idea it is writing its contents into a stream that comes from an FTP file system.

```java
// Open an FTP connection and navigate to a remote folder
FileSystem ftp = new FtpConnection("10.1.2.3", "user", "pass");
ftp.open();
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

### Read and write files as local database entries

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

### Recursively dump an FTP folder to the local file system

This example makes use of the `FileUtils` utility class, whose `copy()` method can be used to transfer all files and folders of a file system object to another one.

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

### Simulate an old floppy drive

This example may look a bit strange, but it shows what is possible using descendants of the `ThrottledFileSystem`. Here, we allocate a portion of memory and make its capacity and transfer speed approximate that of an old 360 kb 5¼" floppy disk. Assuming that `bigfile.bin` is a file that exceeds the size of the floppy, the last instruction will take some time to execute, and eventually throw an exception once the "disk" is full.

```java
// Open floppy
FileSystem floppy = new FloppyDisk(new RamDisk(), FloppyType.F_360);
floppy.open();

// Open hard disk
FileSystem hd = new HardDisk("/home/sylvain/somefolder");
hd.open();

// Try to copy a big file into the floppy
FileUtils.copy(hd.readFrom("bigfile.bin"), floppy.writeTo("bigfile.bin"));
```

### Write a custom file system

This example shows how one can write a custom file system with a specific behavior. Here, the class `MyFileSystem` descends from `FilterFileSystem`, which delegates operations to another file system instance. We override method `readFrom` so that a count of the number of accessed files is kept, and read access is not granted once the number of accesses reaches 10.

```java
class MyFileSystem extends FilterFileSystem {

  int filesRead = 0;
  
  public MyFileSystem(FileSystem fs) {
    super(fs);
  }
  
  public InputStream readFrom(String filename) throws FileSystemException {
    if (++filesRead > 10) {
      throw new FileSystemException("Maximum number of files reached");
    }
    return super.readFrom(filename);
  }
}
```

One could imagine custom file systems performing various operations: enforcing access control rules based on the files that have been read in the past (Chinese wall policy), associate files with security levels and prevent read or write access depending on the level of the current user (Bell-LaPadula), etc.