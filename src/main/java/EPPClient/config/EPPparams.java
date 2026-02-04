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

public class EPPparams
{
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
      e.printStackTrace();
    }
  }

  private static final Preferences prefs = Preferences.userNodeForPackage(thisClass);

  public static void setPrefix(String INparamPrefix)
  {
    paramPrefix = INparamPrefix;
  }

  public static String getParameter(String key)
  {

    try
    {
      try
      {
        prefs.sync();
      }
      catch (java.util.prefs.BackingStoreException ex)
      {
        ex.printStackTrace();
      }

      String parameterValue = new String(prefs.getByteArray(paramPrefix + key, new byte[0]));
      try
      {
        if (parameterValue.length() > 0)
        {
          parameterValue = CryptoUtils.decrypt(parameterValue);
        }
        else
        {
          try
          {
            parameterValue = RESOURCE_BUNDLE.getString(key);
          }
          catch (MissingResourceException e)
          {
            parameterValue = "";
          }
          setParameter(key, parameterValue);
          try
          {
            prefs.sync();
          }
          catch (java.util.prefs.BackingStoreException ex)
          {
            ex.printStackTrace();
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      return parameterValue;
    }
    catch (MissingResourceException e)
    {
      e.printStackTrace();
      setParameter(paramPrefix + key, RESOURCE_BUNDLE.getString(key));
      //prefs.put (key, RESOURCE_BUNDLE.getString(key));
      try
      {
        prefs.sync();
      }
      catch (java.util.prefs.BackingStoreException ex)
      {
        ex.printStackTrace();
      }
      return RESOURCE_BUNDLE.getString(key);
    }
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
        ex.printStackTrace();
      }
    }
    catch (MissingResourceException e)
    {
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static String getKey()
  {
    return "D6B80943CCE0F1C049D9A39094FB4FA1";
  }
}
