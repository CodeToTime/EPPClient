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

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.codetotime.eppclient.CustomLogin;
import com.codetotime.eppclient.ErrorHandler;
import com.codetotime.eppclient.Main;
import com.codetotime.eppclient.config.EppParams;
import com.codetotime.eppclient.messages.Message;
import it.nic.epp.client.commands.interfaces.IEppRequest;
import it.nic.epp.client.commands.query.Poll;
import it.nic.epp.client.commands.session.Logout;
import it.nic.epp.client.exceptions.EppSchemaException;
import it.nic.epp.client.httpClient.Client;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.ext.LoginResponseExt;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EppThread extends Thread {
  private static final Logger log = LoggerFactory.getLogger(EppThread.class);

  public EppThread(Main mainFrame) {
    this.mainFrame = mainFrame;
  }

  @Override
  public void run() {
    isRunning = true;
    mainFrame.setActiveEpp(2);

    HttpBaseResponse response = null;

    try {
      URI serverUri = new URI(EppParams.getParameter("EppClient.serverUri"));

      if (EppParams.getParameter("EppClient.proxyHost").length() > 0
          && EppParams.getParameter("EppClient.proxyPort").length() > 0) {
        client =
            new Client(
                serverUri.toString(),
                EppParams.getParameter("EppClient.proxyHost"),
                Integer.parseInt(EppParams.getParameter("EppClient.proxyPort")));
      } else {
        client = new Client(serverUri.toString());
      }

      CustomLogin login =
          new CustomLogin(
              EppParams.getParameter("EppClient.defaultUser"),
              EppParams.getParameter("EppClient.defaultPassword"));

      //            logger.logmessage("HELLO: " + client.sendHello().toString());

      log.info("Sending EPP login (request omitted for security)");
      response = client.sendCommand(login);
      log.trace("EPP < login", kv("raw_xml", response.toString()));
      if (response.isSuccessfully()) {
        eppClosable = false;
        mainFrame.setActiveEpp(1);

        LoginResponseExt responseExtension = (LoginResponseExt) response.getResponseExtension();
        if (responseExtension != null) {
          if (responseExtension.getCredit() != null) {
            mainFrame.setResCredit(responseExtension.getCredit().toString());
          }
        }

        while (!isClosing) {

          doPoll();
          try {
            Thread.sleep(
                Integer.parseInt(EppParams.getParameter("EppClient.refreshInterval")) * 1000);
          } catch (InterruptedException v) {
            log.debug("EPP thread sleep interrupted: {}", v.getMessage());
          }
        }
      } else {
        mainFrame.setActiveEpp(0);
        if (response.getResultCode() == 2200) {
          if (response.getReasonCode() == 6004) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "La password è scaduta!\n\n"
                    + "Effettuare il cambio dal menu \"Configurazione->Cambio Password\"",
                "Password Scaduta",
                JOptionPane.WARNING_MESSAGE);
          }
          if (response.getReasonCode() == 6005) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "La password è errata!\n\n"
                    + "Inserire la password corretta dal menu \"Configurazione\"",
                "Password Errata",
                JOptionPane.WARNING_MESSAGE);
          }
        } else if (response.getResultCode() == 2306) {
          JOptionPane.showMessageDialog(
              mainFrame,
              "Il Registrar non è accreditato per DNSSEC",
              "DNSSEC NON Abilitato",
              JOptionPane.WARNING_MESSAGE);
        } else if (response.getResultCode() == 2400) {
          if (response.getReasonCode() == 5052) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "L'indirizzo IP non risulta tra quelli abilitati presso il Registro!",
                "Indirizzo IP NON Abilitato",
                JOptionPane.WARNING_MESSAGE);
          }
        } else if (response.getResultCode() == 2502) {
          if (response.getReasonCode() == 5051) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "E' stato superato il numero massimo di sessioni\n"
                    + "consentite dal server EPP del Registro!",
                "Numero massimo di sessioni raggiunto",
                JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    } catch (IOException v) {
      // Check for connection errors that might indicate whitelist issues
      String errorMessage = v.getMessage();
      if (errorMessage != null
          && (errorMessage.contains("Connection refused")
              || errorMessage.contains("Connect to")
              || errorMessage.contains("Connection timed out")
              || errorMessage.contains("ConnectException"))) {
        log.error("IOException connecting to EPP server - Connection issue: {}", v.getMessage());
        JOptionPane.showMessageDialog(
            mainFrame,
            "Impossibile connettersi al server EPP.\n\n"
                + "Possibili cause:\n"
                + "- L'indirizzo IP non è nella whitelist del Registro\n"
                + "- Il firewall blocca la connessione\n"
                + "- Il server EPP non è raggiungibile\n\n"
                + "Verificare che l'indirizzo IP sia abilitato presso il Registro.\n\n"
                + "Per ricevere assistenza, attivare il livello di logging DEBUG "
                + "utilizzando l’opzione -Deppclient.logLevel=DEBUG",
            "Errore di Connessione",
            JOptionPane.ERROR_MESSAGE);
      } else {
        ErrorHandler.error(log, v, "Errore di Connessione", mainFrame);
      }
    } catch (URISyntaxException v) {
      ErrorHandler.error(log, v, "Errore di Configurazione", mainFrame);
    } catch (XmlException v) {
      ErrorHandler.error(log, v, "Errore XML", mainFrame);
    } finally {
      if (!eppClosable) {
        mainFrame.setActiveEpp(2);
        try {
          if (!eppClosable) {
            Logout logout = new Logout();
            log.info("Sending EPP logout");
            log.trace("EPP > logout", kv("raw_xml", logout.xmlText()));
            response = client.sendCommand(logout);
            log.trace("EPP < logout", kv("raw_xml", response.toString()));
            if (response.isSuccessfully()) {
              eppClosable = true;
              mainFrame.setActiveEpp(0);
            } else {
              if (response.getResultCode() == 2002) {
                eppClosable = true;
                mainFrame.setActiveEpp(0);
              }
            }
          }
        } catch (IOException v) {
          ErrorHandler.logError(log, v);
        } catch (XmlException v) {
          ErrorHandler.logError(log, v);
        }
      }
    }
    isRunning = false;
  }

  public void restart() {
    if (isClosing) {
      isClosing = false;
    }
    if (!isRunning) {
      this.start();
    }
  }

  public boolean gracefulStop() {
    if (isRunning) {
      if (!isClosing) {
        isClosing = true;
      }
      this.interrupt();
      try {
        for (int i = 0; i < 10 && !eppClosable; i++) {
          Thread.sleep(500);
        }
      } catch (InterruptedException e) {
        log.debug("EPP thread interrupted while graceful stopping: {}", e.getMessage());
      }
    }
    return eppClosable;
  }

  public Boolean doPoll() throws XmlException, IOException {
    return doPoll(false);
  }

  public Boolean doPoll(Boolean doAck) throws XmlException, IOException {
    Boolean gotMessage = false;
    try {
      Poll pollCmd = new Poll();
      try {
        pollCmd.setReq();
      } catch (EppSchemaException ex) {
        ErrorHandler.logError(log, ex);
      }

      log.trace("EPP > poll", kv("raw_xml", pollCmd.xmlText()));
      HttpBaseResponse response = this.sendCommand(pollCmd);
      if (response == null) {
        log.warn("Poll command returned null response - connection may have been interrupted");
        return false;
      }
      log.trace("EPP < poll", kv("raw_xml", response.toString()));
      if (response.isSuccessfully()) {
        mainFrame.setMsgQ(Integer.toString(response.getMsgQCount()));
        if (response.getMsgQCount() > 0) {

          if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null) {
            java.text.SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            java.text.SimpleDateFormat dbDateFormatter =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mainFrame.setNextMsg(
                dateFormatter.format(response.getMsgQDate().getTime())
                    + " - "
                    + response.getMsgQText());
            if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null) {
              Message message =
                  new Message(
                      response.getMsgQId(),
                      response.getMsgQDate().getTime(),
                      response.getMsgQText(),
                      response.toString(),
                      false,
                      doAck,
                      false);
              mainFrame.messagesDao.saveRecord(message);
              mainFrame.addMsgtoList(message);
            }
          }

          gotMessage = true;
          if (doAck) {
            try {
              pollCmd.setAck(response.getMsgQId());

              log.trace("EPP > poll (ack)", kv("raw_xml", pollCmd.xmlText()));
              HttpBaseResponse ackResponse = this.sendCommand(pollCmd);
              log.trace("EPP < poll (ack)", kv("raw_xml", ackResponse.toString()));
              if (ackResponse.isSuccessfully()) {
                Message message = mainFrame.messagesDao.getMessage(response.getMsgQId());
                message.setAck(true);
                mainFrame.messagesDao.editRecord(message);
              }
            } catch (EppSchemaException ex) {
              ErrorHandler.logError(log, ex);
            }
          } else {
            java.text.SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            java.text.SimpleDateFormat dbDateFormatter =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mainFrame.setNextMsg(
                dateFormatter.format(response.getMsgQDate().getTime())
                    + " - "
                    + response.getMsgQText());
            if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null) {
              Message message =
                  new Message(
                      response.getMsgQId(),
                      response.getMsgQDate().getTime(),
                      response.getMsgQText(),
                      response.toString(),
                      false,
                      doAck,
                      false);
              mainFrame.messagesDao.saveRecord(message);
              mainFrame.addMsgtoList(message);
            }
          }
          // mainFrame.setEnableMsgRecv(true);
        } else {
          mainFrame.setNextMsg("No messages");
          // mainFrame.setEnableMsgRecv(false);
        }
      } else {
        if (response.getResultCode() == 2002) {
          JOptionPane.showMessageDialog(
              mainFrame,
              "Rilevato errore in fase di polling messaggi.\nPossibile perdita sessione!",
              "Errore Polling",
              JOptionPane.WARNING_MESSAGE);
          this.gracefulStop();
          mainFrame.setActiveEpp(0);
        }
      }

    } catch (NullPointerException v) {
      log.error("NullPointerException in doPoll", v);
    }

    return gotMessage;
  }

  public synchronized HttpBaseResponse sendCommand(IEppRequest command)
      throws org.apache.xmlbeans.XmlException, IOException {
    HttpBaseResponse response = null;
    try {
      while (isWaitingEppResponse) {
        Thread.sleep(100);
      }

      isWaitingEppResponse = true;
      response = client.sendCommand(command);
      isWaitingEppResponse = false;

    } catch (InterruptedException ex) {
      log.debug("Interrupted while waiting for EPP uplink availability.");
    }

    return response;
  }

  protected boolean isActive() {
    return isRunning;
  }

  private Client client = null;
  private boolean eppClosable = true;
  private boolean isClosing = false;
  private boolean isRunning = false;
  private boolean isWaitingEppResponse = false;
  private Main mainFrame;
}
