package ca.uqac.lif.fs;

/**
 * Exception thrown by methods of the {@link FileSystem} class.
 * @author Sylvain Hallé
 */
public class FileSystemException extends Exception
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new instance of the exception.
	 * @param message The message associated to the exception
	 */
	public FileSystemException(String message)
	{
		super(message);
	}
	
	/**
	 * Creates a new instance of the exception.
	 * @param t The throwable causing the exception to be thrown
	 */
	public FileSystemException(Throwable t)
	{
		super(t);
	}
}
