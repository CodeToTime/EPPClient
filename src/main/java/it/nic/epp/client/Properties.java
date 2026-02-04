/*
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

package it.nic.epp.client;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Properties {
    private static final String BUNDLE_NAME = "eppClient";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    
    public static String getString(String key) {
        // Check system properties first
        String sysProp = System.getProperty(key);
        if (sysProp != null) {
            return sysProp;
        }
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    public static Boolean getBoolean(String key) {
        try {
            return Boolean.parseBoolean(getString(key));
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }
}