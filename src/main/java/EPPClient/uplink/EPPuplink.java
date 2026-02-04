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

package EPPClient.uplink;


import EPPClient.config.EPPparams;
import EPPClient.main;
import it.nic.epp.client.commands.interfaces.IEppRequest;
import it.nic.epp.client.responses.HttpBaseResponse;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;

public class EPPuplink
{
  public EPPuplink(main mainFrame)
  {
    this.mainFrame = mainFrame;
  }

  public void start()
  {
    if (EPPparams.getParameter("EppClient.proxyHost").length() > 0 && EPPparams.getParameter("EppClient.proxyPort").length() > 0)
    {
      System.setProperty("https.proxyHost", EPPparams.getParameter("EppClient.proxyHost"));
      System.setProperty("https.proxyPort", EPPparams.getParameter("EppClient.proxyPort"));
    }
    else
    {
      System.setProperty("https.proxyHost", "");
      System.setProperty("https.proxyPort", "");
    }
      EPPthread = new EPPthread(mainFrame);
      EPPthread.start();
  }

  public void restart()
  {
    if (EPPthread != null)
    {
      EPPthread.restart();
    }
    else
    {
      this.start();
    }
  }

  public void doPoll()
  {
    EPPthread.interrupt();
  }

  public Boolean pollMsg() throws XmlException, IOException
  {
    return EPPthread.doPoll(true);
  }

  public void registerListener()
  {
    //EPPthread
  }

  public boolean gracefulStop()
  {
    boolean isStopped = false;
    if (EPPthread == null)
    {
      isStopped = true;
    }
    else
    {
      if (EPPthread.gracefulStop())
      {
        EPPthread = null;
        isStopped = true;
      }
    }
    return isStopped;
  }

  public void stop()
  {
    EPPthread = null;
  }

  public boolean isActive()
  {
    boolean isActive = false;
    if (EPPthread instanceof EPPthread)
    {
      isActive = EPPthread.isActive();
    }
    return isActive;
  }

  public HttpBaseResponse sendCommand(IEppRequest command) throws org.apache.xmlbeans.XmlException, IOException
  {
    return EPPthread.sendCommand(command);
  }

  private EPPthread EPPthread = null;
  private main mainFrame;
}
