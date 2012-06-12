/*     JBoss, Home of Professional Open Source Copyright 2008, Red Hat
 *  Middleware LLC, and individual contributors as indicated by the
 *  @author tags.
 *     See the copyright.txt in the distribution for a full listing of
 *  individual contributors. This copyrighted material is made available
 *  to anyone wishing to use, modify, copy, or redistribute it subject to
 *  the terms and conditions of the GNU Lesser General Public License, v. 2.1.
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT A WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU Lesser General Public License for more details. You should
 *  have received a copy of the GNU Lesser General Public License, v.2.1
 *  along with this distribution; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301, USA.
 *
 *  (C) 2008,
 *  @author Red Hat Middleware LLC.
 */
package org.jboss.jbossts.fileio;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.io.OutputStream;

/**
 * User: Ioannis Ganotis
 * Date: Aug 5, 2008
 */
public class DataOutputStream extends java.io.DataOutputStream
{
  /**
   * bytearr is initialized on demand by writeUTF
   */
  private byte[] bytearr = null;

  /**
   * Creates a new data output stream to write data to the specified
   * underlying output stream. The counter <code>written</code> is
   * set to zero.
   *
   * @param   out   the underlying output stream, to be saved for later
   *                use.
   * @see     java.io.FilterOutputStream#out
   */
  public DataOutputStream(OutputStream out) {
    super(out);
  }


  /**
   * Writes a string to the specified DataOutput using
   * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
   * encoding in a machine-independent manner.
   * <p/>
   * First, two bytes are written to out as if by the <code>writeShort</code>
   * method giving the number of bytes to follow. This value is the number of
   * bytes actually written out, not the length of the string. Following the
   * length, each character of the string is output, in sequence, using the
   * modified UTF-8 encoding for the character. If no exception is thrown, the
   * counter <code>written</code> is incremented by the total number of
   * bytes written to the output stream. This will be at least two
   * plus the length of <code>str</code>, and at most two plus
   * thrice the length of <code>str</code>.
   *
   * @param str a string to be written.
   * @param out destination to write to
   * @return The number of bytes written out.
   * @throws java.io.IOException if an I/O error occurs.
   */
    public static int writeUTF(String str, DataOutput out) throws IOException {
    int strlen = str.length();
    int utflen = 0;
    int c, count = 0;

    /* use charAt instead of copying String to char array */
    for (int i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }

    if (utflen > 65535) {
      throw new UTFDataFormatException(
          "encoded string too long: " + utflen + " bytes");
    }

    byte[] bytearr;
    if (out instanceof DataOutputStream) {
      DataOutputStream dos = (DataOutputStream) out;
      if (dos.bytearr == null || (dos.bytearr.length < (utflen + 2))) {
        dos.bytearr = new byte[(utflen * 2) + 2];
      }
      bytearr = dos.bytearr;
    } else {
      bytearr = new byte[utflen + 2];
    }

    bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
    bytearr[count++] = (byte) ((utflen) & 0xFF);

    int i;
    for (i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F))) {
        break;
      }
      bytearr[count++] = (byte) c;
    }

    for (; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        bytearr[count++] = (byte) c;

      } else if (c > 0x07FF) {
        bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
      } else {
        bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
      }
    }
    out.write(bytearr, 0, utflen + 2);
    return utflen + 2;
  }
}
