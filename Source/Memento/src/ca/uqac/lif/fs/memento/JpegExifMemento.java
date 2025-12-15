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
package ca.uqac.lif.fs.memento;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoGpsText;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import ca.uqac.lif.fs.FileProxy;

public class JpegExifMemento extends FileMemento<String>
{
	/**
	 * The name of the metadata field in which the string is stored.
	 */
	protected static final TagInfoGpsText s_field = ExifTagConstants.EXIF_TAG_USER_COMMENT;
	
	public JpegExifMemento(FileProxy file)
	{
		super(file);
	}

	@Override
	protected String read(InputStream is) throws IOException
	{
		try
		{
			ImageMetadata metadata = Imaging.getMetadata(is, "");
			if (!(metadata instanceof JpegImageMetadata))
			{
				return "";
			}
			JpegImageMetadata j_metadata = (JpegImageMetadata) metadata;
			TiffImageMetadata exif = j_metadata.getExif();
			String value = exif.getFieldValue(s_field);
			if (value == null)
			{
				return "";
			}
			return value;
		}
		catch (ImageReadException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void write(String s, OutputStream os) throws IOException
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(m_fileContents);
			ImageMetadata metadata = Imaging.getMetadata(bais, "");
			bais.close();
			if (!(metadata instanceof JpegImageMetadata))
			{
				return ;
			}
			TiffOutputSet output_set = null;
			JpegImageMetadata j_metadata = (JpegImageMetadata) metadata;
			TiffImageMetadata exif = j_metadata.getExif();
			if (exif != null)
			{
				output_set = exif.getOutputSet();
			}
			else
			{
				output_set = new TiffOutputSet();
			}
			TiffOutputDirectory exif_directory = output_set.getOrCreateExifDirectory();
			exif_directory.removeField(s_field);
			exif_directory.add(s_field, s);
			ExifRewriter rewriter = new ExifRewriter();
			rewriter.updateExifMetadataLossless(m_fileContents, os, output_set);
			
		}
		catch (ImageReadException e)
		{
			e.printStackTrace();
		}
		catch (ImageWriteException e)
		{
			e.printStackTrace();
		}
	}

}
