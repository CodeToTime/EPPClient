/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of EPPClient.
 *
 * EPPClient is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EPPClient is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EPPClient.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.codetotime.eppclient.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and writes application configuration parameters, storing them encrypted in the Java
 * Preferences store and falling back to the resource bundle when no stored value exists.
 */
public class EppParams {
  private static final Logger log = LoggerFactory.getLogger(EppParams.class);
  private static final String BUNDLE_NAME = "com.codetotime.eppclient.config.EppParams";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  private static Class thisClass = null;

  private static String paramPrefix = "";

  static {
    try {
      thisClass = Class.forName("com.codetotime.eppclient.config.EppParams");
    } catch (ClassNotFoundException e) {
      log.error("Class not found", e);
    }
  }

  private static final Preferences prefs = Preferences.userNodeForPackage(thisClass);

  public static void setPrefix(String inParamPrefix) {
    paramPrefix = inParamPrefix;
  }

  /**
   * Returns the value for the given parameter key, decrypting from preferences or falling back to
   * the resource bundle.
   *
   * @param key the parameter key
   * @return the parameter value, or an empty string if not found
   */
  public static String getParameter(String key) {
    String parameterValue = "";

    try {
      try {
        prefs.sync();
      } catch (java.util.prefs.BackingStoreException ex) {
        log.error("BackingStoreException in getParameter", ex);
      }

      String storedValue = new String(prefs.getByteArray(paramPrefix + key, new byte[0]));

      if (storedValue.length() > 0) {
        try {
          String decryptedValue = CryptoUtils.decrypt(storedValue);
          if (decryptedValue != null && !decryptedValue.isEmpty()) {
            parameterValue = decryptedValue;
            log.debug("Successfully decrypted parameter: {}", key);
          }
        } catch (Exception decryptEx) {
          log.debug(
              "Decryption failed for key {}, falling back to resource bundle: {}",
              key,
              decryptEx.getMessage());
          // Fall through to load from resource bundle
        }
      }

      // Final check: if parameterValue is still empty, use resource bundle
      if (parameterValue.isEmpty()) {
        try {
          parameterValue = RESOURCE_BUNDLE.getString(key);
          log.warn(
              "Decrypted value is empty for key {}, falling back to resource bundle: \"{}\"",
              key,
              parameterValue);
          // Save the fallback value to preferences
          setParameter(key, parameterValue);
        } catch (MissingResourceException e) {
          log.error("Missing parameter in resource bundle: {}", key);
          parameterValue = "";
        }
      }
    } catch (Exception e) {
      log.error("Error in getParameter for key {}: {}", key, e.getMessage(), e);
      // Final fallback attempt
      try {
        parameterValue = RESOURCE_BUNDLE.getString(key);
      } catch (MissingResourceException ex) {
        parameterValue = "";
      }
    }

    return parameterValue;
  }

  /**
   * Encrypts and stores the given value in the preferences store under the given key.
   *
   * @param key the parameter key
   * @param value the value to store
   */
  public static void setParameter(String key, String value) {
    key = paramPrefix + key;

    try {
      prefs.putByteArray(key, CryptoUtils.encrypt(value).getBytes());
      try {
        prefs.sync();
      } catch (java.util.prefs.BackingStoreException ex) {
        log.error("BackingStoreException in setParameter", ex);
      }
    } catch (MissingResourceException e) {
      log.error("MissingResourceException in setParameter", e);
    } catch (Exception e) {
      log.error("Error in setParameter", e);
    }
  }

  public static String getKey() {
    return "D6B80943CCE0F1C049D9A39094FB4FA1";
  }
}
