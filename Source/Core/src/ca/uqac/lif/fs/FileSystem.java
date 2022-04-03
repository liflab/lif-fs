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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Abstraction of elementary read and write operations over a file system.
 * @author Sylvain Hallé
 */
public interface FileSystem
{
	/**
	 * Enumeration representing the three possible states of a file system.
	 */
	public enum OpenState {
		/**
		 * State of the file system before a call to {@link #open()}.
		 */
		UNINITIALIZED,
		
		/**
		 * State of the file system after a call to {@link #open()} and before a
		 * call to {@link #close()}.
		 */
		OPEN,
		
		/**
		 * State of the file system after a call to {@link #close()}.
		 */
		CLOSED}
	
	/**
	 * Starts the interaction with the file system. All operations invoked on
	 * a file system will throw a {@link FileSystemException} if called before
	 * this method.
	 * @throws FileSystemException Thrown if the interaction with the file system
	 * cannot be started
	 */
	public void open() throws FileSystemException;
	
	/**
	 * Lists all the files and folders in the current directory.
	 * @return The list of files and folders
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	/*@ non_null @*/ public List<String> ls() throws FileSystemException;
	
	/**
	 * Lists all the files and folders in a given directory.
	 * @param path The path from which to list files
	 * @return The list of files and folders
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	/*@ non_null @*/ public List<String> ls(String path) throws FileSystemException;
	
	/**
	 * Determines if the resource pointed to by a given path corresponds to a
	 * directory.
	 * @param path The path
	 * @return <tt>true</tt> if the resource exists and is a directory,
   * <tt>false</tt> otherwise
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public boolean isDirectory(String path) throws FileSystemException;
	
	/**
	 * Determines if the resource pointed to by a given path corresponds to a
	 * file.
	 * @param path The path
	 * @return <tt>true</tt> if the resource exists and is a file, <tt>false</tt>
	 * otherwise
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public boolean isFile(String path) throws FileSystemException;
	
	/**
	 * Gets the size of a file.
	 * @param path The path corresponding to the file
	 * @return The size in bytes
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public long getSize(String path) throws FileSystemException;
	
	/**
	 * Writes data to a file. The method provides an {@link OutputStream} to
	 * which data can be printed. The data is guaranteed to be committed to the
	 * file only when the print stream is closed. Whether data is written before
	 * that moment depends on the actual file system implementation. 
	 * @param filename The name of the file to which data is to be written
	 * @return An output stream open on the file. If the method successfully
	 * returns, it guarantees that this stream is not null
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	/*@ non_null @*/ public OutputStream writeTo(String filename) throws FileSystemException;
	
	/**
	 * Reads data from a file. The method provides an {@link InputStream} from
	 * which data can be read. Consumers of this input stream have the
	 * responsibility to close the stream after usage. 
	 * @param filename The name of the file to which data is to be written
	 * @return A print stream open on the file. If the method successfully
	 * returns, it guarantees that this stream is not null
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	/*@ non_null @*/public InputStream readFrom(String filename) throws FileSystemException;
	
	/**
	 * Changes the current directory.
	 * @param path The directory to change to. The string can contain a path
	 * relative to the current directory.
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void chdir(String path) throws FileSystemException;
	
	/**
	 * @param path The directory to change to. The string can contain a path
	 * relative to the current directory.
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void pushd(String path) throws FileSystemException;
	
	/**
	 * Changes the current directory to the one before the last call to
	 * {@link #chdir(String)} or {@link #pushd(String)}. 
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void popd() throws FileSystemException;
	
	/**
	 * Creates a directory if it does not exist.
	 * @param path The directory to create. The string can contain a path
	 * relative to the current directory.
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void mkdir(String path) throws FileSystemException;
	
	/**
	 * Deletes a directory and all its contents.
	 * @param path The directory to delete
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void rmdir(String path) throws FileSystemException;
	
	/**
	 * Deletes a directory and all its contents.
	 * @param path The directory to delete
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public void delete(String path) throws FileSystemException;
	
	/**
	 * Retrieves the current working directory, relative to the root of the
	 * file system.
	 * @return A string representing the current working directory
	 * @throws FileSystemException Thrown if the operation could not be performed
	 * for some reason
	 */
	public String pwd() throws FileSystemException;
	
	/**
	 * Stops the interaction with the file system. All operations invoked on
	 * a file system will throw a {@link FileSystemException} if called after
	 * this method.
	 * @throws FileSystemException Thrown if an error occurred while stopping the
	 * interaction with the file system
	 */
	public void close() throws FileSystemException;
}
