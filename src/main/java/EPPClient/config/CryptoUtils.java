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
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class CryptoUtils
{
  private static final String KEY_FILE = "local.key";
  public static final String AES = "AES";
  // Old hardcoded key for migration from versions <= 2.3.2
  private static final String OLD_KEY = "D6B80943CCE0F1C049D9A39094FB4FA1";

  private static String getKeyFilePath() {
    String userHome = System.getProperty("user.home");
    return userHome + "/.eppclient/" + KEY_FILE;
  }

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
    // Try with new key first
    try {
      SecretKeySpec sks = getSecretKeySpec();
      Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
      cipher.init(Cipher.DECRYPT_MODE, sks);
      byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
      return new String(decrypted);
    } catch (Exception e) {
      // If fails, try with old key for migration from <= 2.3.2
      try {
        byte[] oldKeyBytes = hexStringToByteArray(OLD_KEY);
        SecretKeySpec sks = new SecretKeySpec(oldKeyBytes, CryptoUtils.AES);
        Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
        cipher.init(Cipher.DECRYPT_MODE, sks);
        byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
        return new String(decrypted);
      } catch (Exception e2) {
        throw new GeneralSecurityException("Decryption failed with both new and old keys");
      }
    }
  }

  private static SecretKeySpec getSecretKeySpec()
          throws NoSuchAlgorithmException, IOException
  {
    byte[] key = readOrGenerateKeyFile();
    SecretKeySpec sks = new SecretKeySpec(key, CryptoUtils.AES);
    return sks;
  }

  private static byte[] readOrGenerateKeyFile()
  {
    String keyPath = getKeyFilePath();
    File keyFile = new File(keyPath);
    
    // If key file exists, read it
    if (keyFile.exists()) {
      try {
        java.util.Scanner scanner = new java.util.Scanner(keyFile);
        String keyHex = scanner.nextLine().trim();
        scanner.close();
        if (keyHex.length() == 32) { // 16 bytes = 32 hex chars
          return hexStringToByteArray(keyHex);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    // Generate new key and save it
    byte[] newKey = new byte[16];
    new java.security.SecureRandom().nextBytes(newKey);
    
    try {
      // Ensure directory exists
      File keyDir = keyFile.getParentFile();
      if (keyDir != null) {
        keyDir.mkdirs();
      }
      
      // Save key to file
      java.io.FileWriter writer = new java.io.FileWriter(keyFile);
      writer.write(byteArrayToHexString(newKey));
      writer.close();
      
      // Set file permissions to owner only (Unix-like systems)
      keyFile.setReadable(true, false);
      keyFile.setWritable(true, false);
      keyFile.setExecutable(false);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return newKey;
  }

  /**
   * Check if migration from old key is needed
   * @return true if preferences exist encrypted with old key
   */
  public static boolean needsMigration() {
    String keyPath = getKeyFilePath();
    File keyFile = new File(keyPath);
    return !keyFile.exists();
  }

  /**
   * Get old key for migration purposes
   */
  public static String getOldKey() {
    return OLD_KEY;
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