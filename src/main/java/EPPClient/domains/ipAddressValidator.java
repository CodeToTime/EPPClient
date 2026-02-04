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

package EPPClient.domains;

import it.nic.epp.client.commands.converters.AbstractHostsConverter.HostIpType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ipAddressValidator
{

  private static Pattern VALID_IPV4_PATTERN = null;
  private static Pattern VALID_IPV6_PATTERN = null;
  private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
  private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

  static
  {
    try
    {
      VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
      VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
    }
    catch (PatternSyntaxException e)
    {
      //logger.severe("Unable to compile pattern", e);
    }
  }

  public static HostIpType validateIpAddress(String ipAddress) throws invalidIpAddressException
  {
    Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
    if (m1.matches())
    {
      return HostIpType.V_4;
    }
    Matcher m2 = VALID_IPV6_PATTERN.matcher(ipAddress);
    if (m2.matches())
    {
      return HostIpType.V_6;
    }
    throw new invalidIpAddressException();
  }
}
