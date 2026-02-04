/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of EPPClient.
 *
 * EPPClient is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * EPPClient is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EPPClient. If not, see <https://www.gnu.org/licenses/>.
 */

package EPPClient.config;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class CryptoUtils
{
  private static final String KEY_FILE = "local.key";
  public static final String AES = "AES";

  public static String encrypt(String value)
          throws GeneralSecurityException, IOException
  {
    SecretKeySpec sks = getSecretKeySpec();
    Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
    cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
    byte[] encrypted = cipher.doFinal(value.getBytes());
    return byteArrayToHexString(encrypted);
  }

  public static String decrypt(String message)
          throws GeneralSecurityException, IOException
  {
    SecretKeySpec sks = getSecretKeySpec();
    Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
    cipher.init(Cipher.DECRYPT_MODE, sks);
    byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
    return new String(decrypted);
  }

  private static SecretKeySpec getSecretKeySpec()
          throws NoSuchAlgorithmException, IOException
  {
    byte[] key = readKeyFile();
    SecretKeySpec sks = new SecretKeySpec(key, CryptoUtils.AES);
    return sks;
  }

  private static byte[] readKeyFile()
  {
    String keyValue = EPPparams.getKey();
    return hexStringToByteArray(keyValue);
  }


  private static String byteArrayToHexString(byte[] b)
  {
    StringBuffer sb = new StringBuffer(b.length * 2);
    for (int i = 0; i < b.length; i++)
    {
      int v = b[i] & 0xff;
      if (v < 16)
      {
        sb.append('0');
      }
      sb.append(Integer.toHexString(v));
    }
    return sb.toString().toUpperCase();
  }

  private static byte[] hexStringToByteArray(String s)
  {
    byte[] b = new byte[s.length() / 2];
    for (int i = 0; i < b.length; i++)
    {
      int index = i * 2;
      int v = Integer.parseInt(s.substring(index, index + 2), 16);
      b[i] = (byte) v;
    }
    return b;
  }
}