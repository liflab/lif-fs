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
  
  This file is taken and adapted from the ThrottledInputStream class of the
  Apache hBase project: https://github.com/apache/hbase
  The original file was distributed under the Apache license. You may obtain
  a copy of the License at:
  
    http://www.apache.org/licenses/LICENSE-2.0
*/
package ca.uqac.lif.fs;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * The ThrottledOutputStream provides bandwidth throttling on a specified
 * OutputStream. It is implemented as a wrapper on top of another OutputStream
 * instance.
 * The throttling works by examining the number of bytes written to the underlying
 * OutputStream from the beginning, and sleep()ing for a time interval if
 * the byte-transfer is found exceed the specified tolerable maximum.
 * (Thus, while the read-rate might exceed the maximum for a given short interval,
 * the average tends towards the specified maximum, overall.)
 * <p>
 * In addition, the class can be given a maximum number of bytes it is allowed
 * to write to the underlying output stream. It throws an {@link IOException}
 * if a call to {@link #write(int) write()} exceeds this limit.
 */
class ThrottledOutputStream extends OutputStream
{

  private final OutputStream rawStream;
  private final long maxBytesPerSec;
  private final long startTime = System.currentTimeMillis();

  private long bytesWritten = 0;
  private long totalSleepTime = 0;
  private long m_maxBytesToWrite = -1;

  public ThrottledOutputStream(OutputStream rawStream) 
  {
    this(rawStream, Long.MAX_VALUE);
  }

  public ThrottledOutputStream(OutputStream rawStream, long maxBytesPerSec) 
  {
    assert maxBytesPerSec > 0 : "Bandwidth " + maxBytesPerSec + " is invalid";
    this.rawStream = rawStream;
    this.maxBytesPerSec = maxBytesPerSec;
  }
  
  /**
   * Sets a maximum number of bytes that this output stream is allowed to
   * write.
   * @param max_bytes The maximum number of bytes
   */
  public void setMaxBytesToWrite(long max_bytes)
  {
  	m_maxBytesToWrite = max_bytes;
  }

  @Override
  public void close() throws IOException
  {
    rawStream.close();
  }

  private long calSleepTimeMs()
  {
    return calSleepTimeMs(bytesWritten, maxBytesPerSec, System.currentTimeMillis() - startTime);
  }

  static long calSleepTimeMs(long bytesRead, long maxBytesPerSec, long elapsed)
  {
  	if (elapsed == 0)
  	{
  		return 0;
  	}
    //assert elapsed > 0 : "The elapsed time should be greater than zero";
    if (bytesRead <= 0 || maxBytesPerSec <= 0)
    {
      return 0;
    }
    // We use this class to load the single source file, so the bytesRead
    // and maxBytesPerSec aren't greater than Double.MAX_VALUE.
    // We can get the precise sleep time by using the double value.
    long rval = (long) ((((double) bytesRead) / ((double) maxBytesPerSec)) * 1000 - elapsed);
    if (rval <= 0)
    {
      return 0;
    }
    else 
    {
      return rval;
    }
  }

  private void throttle() throws InterruptedIOException 
  {
    long sleepTime = calSleepTimeMs();
    totalSleepTime += sleepTime;
    try 
    {
      TimeUnit.MILLISECONDS.sleep(sleepTime);
    }
    catch (InterruptedException e) 
    {
      throw new InterruptedIOException("Thread aborted");
    }
  }
  
  /**
   * Getter for the number of bytes written to this stream, since creation.
   * @return The number of bytes
   */
  public long getTotalBytesWritten() 
  {
    return bytesWritten;
  }

  /**
   * Getter for the read-rate from this stream, since creation.
   * Calculated as bytesRead/elapsedTimeSinceStart.
   * @return Read rate, in bytes/sec.
   */
  public long getBytesPerSec()
  {
    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
    if (elapsed == 0) 
    {
      return bytesWritten;
    }
    else 
    {
      return bytesWritten / elapsed;
    }
  }

  /**
   * Getter the total time spent in sleep.
   * @return Number of milliseconds spent in sleep.
   */
  public long getTotalSleepTime()
  {
    return totalSleepTime;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() 
  {
    return "ThrottledOutputStream{" +
        "bytesWritten=" + bytesWritten +
        ", maxBytesPerSec=" + maxBytesPerSec +
        ", bytesPerSec=" + getBytesPerSec() +
        ", totalSleepTime=" + totalSleepTime +
        '}';
  }

	@Override
	public void write(int b) throws IOException
	{
		throttle();
		rawStream.write(b);
		bytesWritten++;
		if (m_maxBytesToWrite >= 0 && bytesWritten > m_maxBytesToWrite)
		{
			throw new IOException("Exceeded maximum number of bytes to write");
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		throttle();
		rawStream.write(b);
		bytesWritten += b.length;
		if (m_maxBytesToWrite >= 0 && bytesWritten > m_maxBytesToWrite)
		{
			throw new IOException("Exceeded maximum number of bytes to write");
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		throttle();
		rawStream.write(b, off, len);
		bytesWritten += len;
		if (m_maxBytesToWrite >= 0 && bytesWritten > m_maxBytesToWrite)
		{
			throw new IOException("Exceeded maximum number of bytes to write");
		}
	}
}
