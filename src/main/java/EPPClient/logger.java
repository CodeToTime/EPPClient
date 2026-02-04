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

package EPPClient;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class logger
{
  public logger(String source)
  {
    this.source = source;
    try
    {
      fstream = new FileWriter("log-" + source + ".txt", true);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void logmessage(String message)
  {
//        System.out.println("EPPClient - " + source + ": " + message);
    try
    {
      fstream.write("\nEPPClient " + sdf.format(cal.getTime()) + " log source " + source + ":\n " + message);
      fstream.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private String source;
  private FileWriter fstream;

  Calendar cal = Calendar.getInstance();
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
