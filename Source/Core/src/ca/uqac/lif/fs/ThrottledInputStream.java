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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * The ThrottledInputStream provides bandwidth throttling on a specified
 * InputStream. It is implemented as a wrapper on top of another InputStream
 * instance.
 * The throttling works by examining the number of bytes read from the underlying
 * InputStream from the beginning, and sleep()ing for a time interval if
 * the byte-transfer is found exceed the specified tolerable maximum.
 * (Thus, while the read-rate might exceed the maximum for a given short interval,
 * the average tends towards the specified maximum, overall.)
 */
class ThrottledInputStream extends InputStream
{

  private final InputStream rawStream;
  private final long maxBytesPerSec;
  private final long startTime = System.currentTimeMillis();

  private long bytesRead = 0;
  private long totalSleepTime = 0;

  public ThrottledInputStream(InputStream rawStream) 
  {
    this(rawStream, Long.MAX_VALUE);
  }

  public ThrottledInputStream(InputStream rawStream, long maxBytesPerSec) 
  {
    assert maxBytesPerSec > 0 : "Bandwidth " + maxBytesPerSec + " is invalid";
    this.rawStream = rawStream;
    this.maxBytesPerSec = maxBytesPerSec;
  }

  @Override
  public void close() throws IOException
  {
    rawStream.close();
  }

  /** {@inheritDoc} */
  @Override
  public int read() throws IOException {
    throttle();
    int data = rawStream.read();
    if (data != -1) {
      bytesRead++;
    }
    return data;
  }

  /** {@inheritDoc} */
  @Override
  public int read(byte[] b) throws IOException {
    throttle();
    int readLen = rawStream.read(b);
    if (readLen != -1) {
      bytesRead += readLen;
    }
    return readLen;
  }

  /** {@inheritDoc} */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    throttle();
    int readLen = rawStream.read(b, off, len);
    if (readLen != -1) {
      bytesRead += readLen;
    }
    return readLen;
  }

  private long calSleepTimeMs()
  {
    return calSleepTimeMs(bytesRead, maxBytesPerSec, System.currentTimeMillis() - startTime);
  }

  static long calSleepTimeMs(long bytesRead, long maxBytesPerSec, long elapsed)
  {
    assert elapsed > 0 : "The elapsed time should be greater than zero";
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
   * Getter for the number of bytes read from this stream, since creation.
   * @return The number of bytes.
   */
  public long getTotalBytesRead() {
    return bytesRead;
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
      return bytesRead;
    }
    else 
    {
      return bytesRead / elapsed;
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
    return "ThrottledInputStream{" +
        "bytesRead=" + bytesRead +
        ", maxBytesPerSec=" + maxBytesPerSec +
        ", bytesPerSec=" + getBytesPerSec() +
        ", totalSleepTime=" + totalSleepTime +
        '}';
  }
}
