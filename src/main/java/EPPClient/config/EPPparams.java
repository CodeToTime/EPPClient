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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPPparams
{
  private static final Logger log = LoggerFactory.getLogger(EPPparams.class);
  private static final String BUNDLE_NAME = "EPPClient.config.EPPparams";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  private static Class thisClass = null;

  private static String paramPrefix = "";

  static
  {
    try
    {
      thisClass = Class.forName("EPPClient.config.EPPparams");
    }
    catch (ClassNotFoundException e)
    {
      log.error("Class not found", e);
    }
  }

  private static final Preferences prefs = Preferences.userNodeForPackage(thisClass);

  public static void setPrefix(String INparamPrefix)
  {
    paramPrefix = INparamPrefix;
  }

  public static String getParameter(String key)
  {
    String parameterValue = "";
    
    try
    {
      try
      {
        prefs.sync();
      }
      catch (java.util.prefs.BackingStoreException ex)
      {
        log.error("BackingStoreException in getParameter", ex);
      }

      String storedValue = new String(prefs.getByteArray(paramPrefix + key, new byte[0]));
      
      if (storedValue.length() > 0)
      {
        try
        {
          String decryptedValue = CryptoUtils.decrypt(storedValue);
          if (decryptedValue != null && !decryptedValue.isEmpty())
          {
            parameterValue = decryptedValue;
            log.debug("Successfully decrypted parameter: {}", key);
          }
          else
          {
            log.warn("Decrypted value is empty for key {}, falling back to resource bundle", key);
            throw new Exception("Decrypted value is empty");
          }
        }
        catch (Exception decryptEx)
        {
          log.warn("Decryption failed for key {}, falling back to resource bundle: {}", key, decryptEx.getMessage());
          // Fall through to load from resource bundle
        }
      }
      
      // Final check: if parameterValue is still empty, use resource bundle
      if (parameterValue.isEmpty())
      {
        log.debug("Parameter value is empty for key {}, loading from resource bundle", key);
        try
        {
          parameterValue = RESOURCE_BUNDLE.getString(key);
          log.debug("Loaded parameter from resource bundle: {}", key);
          // Save the fallback value to preferences
          setParameter(key, parameterValue);
        }
        catch (MissingResourceException e)
        {
          log.error("Missing parameter in resource bundle: {}", key);
          parameterValue = "";
        }
      }
    }
    catch (Exception e)
    {
      log.error("Error in getParameter for key {}: {}", key, e.getMessage(), e);
      // Final fallback attempt
      try
      {
        parameterValue = RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException ex)
      {
        parameterValue = "";
      }
    }

    return parameterValue;
  }

  public static void setParameter(String key, String value)
  {
    key = paramPrefix + key;

    try
    {
      prefs.putByteArray(key, CryptoUtils.encrypt(value).getBytes());
      try
      {
        prefs.sync();
      }
      catch (java.util.prefs.BackingStoreException ex)
      {
        log.error("BackingStoreException in setParameter", ex);
      }
    }
    catch (MissingResourceException e)
    {
      log.error("MissingResourceException in setParameter", e);
    }
    catch (Exception e)
    {
      log.error("Error in setParameter", e);
    }
  }

  public static String getKey()
  {
    return "D6B80943CCE0F1C049D9A39094FB4FA1";
  }
}
