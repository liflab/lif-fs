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

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * A memento that writes byte arrays into
 * <a href="http://www.ietf.org/rfc/rfc4648.txt">base-64</a> encoded strings
 * for storage into another memento object.
 * @author Sylvain Hallé
 */
public class Base64Memento extends FilterMemento<byte[],String>
{
	/**
	 * The encoder used to read base-64 strings.
	 */
	protected static final Decoder s_decoder = Base64.getDecoder();

	/**
	 * The encoder used to produce base-64 strings.
	 */
	protected static final Encoder s_encoder = Base64.getEncoder();

	/**
	 * Creates a new base-64 memento.
	 * @param m The memento handling the base-64 encoded version of the byte
	 * array
	 */
	public Base64Memento(Memento<String> m)
	{
		super(m);
	}	

	@Override
	protected String transformFrom(byte[] m)
	{
		return s_encoder.encodeToString(m);
	}

	@Override
	protected byte[] transformTo(String m)
	{
		return s_decoder.decode(m);
	}
}
