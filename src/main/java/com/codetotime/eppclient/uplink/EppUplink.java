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

package com.codetotime.eppclient.uplink;

import com.codetotime.eppclient.config.EPPparams;
import com.codetotime.eppclient.main;
import it.nic.epp.client.commands.interfaces.IEppRequest;
import it.nic.epp.client.responses.HttpBaseResponse;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;

/**
 * Manages the EPP connection lifecycle: starts, restarts, and stops the underlying {@link
 * EppThread}, configures proxy settings from application parameters, and delegates command dispatch
 * and poll operations to the active thread.
 */
public class EppUplink {
  /**
   * Constructs a new EppUplink bound to the given application main frame.
   *
   * @param mainFrame the application main frame, passed through to the EPP thread
   */
  public EppUplink(main mainFrame) {
    this.mainFrame = mainFrame;
  }

  /** Applies proxy settings from application parameters and starts the EPP thread. */
  public void start() {
    if (EPPparams.getParameter("EppClient.proxyHost").length() > 0
        && EPPparams.getParameter("EppClient.proxyPort").length() > 0) {
      System.setProperty("https.proxyHost", EPPparams.getParameter("EppClient.proxyHost"));
      System.setProperty("https.proxyPort", EPPparams.getParameter("EppClient.proxyPort"));
    } else {
      System.setProperty("https.proxyHost", "");
      System.setProperty("https.proxyPort", "");
    }
    eppThread = new EppThread(mainFrame);
    eppThread.start();
  }

  /** Restarts the EPP thread if one exists, or starts a new one. */
  public void restart() {
    if (eppThread != null) {
      eppThread.restart();
    } else {
      this.start();
    }
  }

  /** Interrupts the EPP thread to trigger an immediate poll cycle. */
  public void doPoll() {
    eppThread.interrupt();
  }

  /**
   * Polls the EPP server for a pending message.
   *
   * @return {@code true} if a message was retrieved, {@code false} otherwise
   * @throws XmlException if the server response cannot be parsed
   * @throws IOException if a network error occurs
   */
  public Boolean pollMsg() throws XmlException, IOException {
    return eppThread.doPoll(true);
  }

  /** Reserved for future listener registration; currently a no-op. */
  public void registerListener() {
    // EppThread
  }

  /**
   * Attempts a graceful shutdown of the EPP thread.
   *
   * @return {@code true} if the thread was stopped successfully or was already null
   */
  public boolean gracefulStop() {
    boolean isStopped = false;
    if (eppThread == null) {
      isStopped = true;
    } else {
      if (eppThread.gracefulStop()) {
        eppThread = null;
        isStopped = true;
      }
    }
    return isStopped;
  }

  /** Immediately discards the EPP thread reference without waiting for it to finish. */
  public void stop() {
    eppThread = null;
  }

  /**
   * Checks whether the EPP connection is currently active.
   *
   * @return {@code true} if the EPP thread exists and reports itself as active
   */
  public boolean isActive() {
    boolean isActive = false;
    if (eppThread instanceof EppThread) {
      isActive = eppThread.isActive();
    }
    return isActive;
  }

  /**
   * Sends an EPP command through the active thread and returns the server response.
   *
   * @param command the EPP request to send
   * @return the server's response
   * @throws XmlException if the response cannot be parsed
   * @throws IOException if a network error occurs
   */
  public HttpBaseResponse sendCommand(IEppRequest command)
      throws org.apache.xmlbeans.XmlException, IOException {
    return eppThread.sendCommand(command);
  }

  private EppThread eppThread = null;
  private main mainFrame;
}
