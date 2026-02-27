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

import EPPClient.CustomLogin;
import EPPClient.Debug;
import EPPClient.config.EPPparams;
import EPPClient.logger;
import EPPClient.main;
import EPPClient.messages.Message;
import it.nic.epp.client.commands.interfaces.IEppRequest;
import it.nic.epp.client.commands.query.Poll;
import it.nic.epp.client.commands.session.Login;
import it.nic.epp.client.commands.session.Logout;
import it.nic.epp.client.exceptions.EppSchemaException;
import it.nic.epp.client.httpClient.Client;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.ext.LoginResponseExt;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.xmlbeans.XmlException;

import javax.swing.*;
import java.io.IOException;

class EPPthread extends Thread
{
  public EPPthread(main mainFrame)
  {
    this.mainFrame = mainFrame;
    logger = new logger("SESSION");
  }

  @Override
  public void run()
  {
    isRunning = true;
    mainFrame.setActiveEPP(2);

    HttpBaseResponse response = null;

    try
    {
      URI serverURI = new URI(EPPparams.getParameter("EppClient.serverURI"));

      if (EPPparams.getParameter("EppClient.proxyHost").length() > 0 && EPPparams.getParameter("EppClient.proxyPort").length() > 0)
      {
        client = new Client(serverURI.toString(), EPPparams.getParameter("EppClient.proxyHost"), Integer.parseInt(EPPparams.getParameter("EppClient.proxyPort")));
      }
      else
      {
        client = new Client(serverURI.toString());
      }

      CustomLogin login = new CustomLogin(EPPparams.getParameter("EppClient.defaultUser"),
              EPPparams.getParameter("EppClient.defaultPassword"));

//            logger.logmessage("HELLO: " + client.sendHello().toString());


      logger.logmessage("CLIENT login request:\n" + login.toString() + "\n");
      //logger.logmessage("CLIENT: *LOGIN COMMAND OMITTED*\n");
      response = client.sendCommand(login);
      logger.logmessage("SERVER login response:\n" + response.toString());
      if (response.isSuccessfully())
      {
        EPPclosable = false;
        mainFrame.setActiveEPP(1);

        LoginResponseExt responseExtension = (LoginResponseExt)
                response.getResponseExtension();
        if (responseExtension != null)
        {
          if (responseExtension.getCredit() != null)
          {
            mainFrame.setResCredit(responseExtension.getCredit().toString());
          }
        }

        while (!isClosing)
        {

          doPoll();
          try
          {
            Thread.sleep(Integer.parseInt(EPPparams.getParameter("EppClient.refreshInterval")) * 1000);
          }
          catch (InterruptedException v)
          {
          }
        }
      }
      else
      {
        mainFrame.setActiveEPP(0);
        if (response.getResultCode() == 2200)
        {
          if (response.getReasonCode() == 6004)
          {
            JOptionPane.showMessageDialog(mainFrame, "La password è scaduta!\n\nEffettuare il cambio dal menu \"Configurazione->Cambio Password\"", "Password Scaduta", JOptionPane.WARNING_MESSAGE);
          }
          if (response.getReasonCode() == 6005)
          {
            JOptionPane.showMessageDialog(mainFrame, "La password è errata!\n\nInserire la password corretta dal menu \"Configurazione\"", "Password Errata", JOptionPane.WARNING_MESSAGE);
          }
        }
        else if (response.getResultCode() == 2306)
        {
          JOptionPane.showMessageDialog(mainFrame, "Il Registrar non è accreditato per DNSSEC", "DNSSEC NON Abilitato", JOptionPane.WARNING_MESSAGE);
        }
        else if (response.getResultCode() == 2400)
        {
          if (response.getReasonCode() == 5052)
          {
            JOptionPane.showMessageDialog(mainFrame, "L'indirizzo IP non risulta tra quelli abilitati presso il Registro!", "Indirizzo IP NON Abilitato", JOptionPane.WARNING_MESSAGE);
          }
        }
        else if (response.getResultCode() == 2502)
        {
          if (response.getReasonCode() == 5051)
          {
            JOptionPane.showMessageDialog(mainFrame, "E' stato superato il numero massimo di sessioni\nconsentite dal server EPP del Registro!", "Numero massimo di sessioni raggiunto", JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    }
    catch (IOException v)
    {
      // Check if debug mode is enabled
      Debug.printStackTrace(v);
      // Check for connection errors that might indicate whitelist issues
      String errorMessage = v.getMessage();
      if (errorMessage != null && (errorMessage.contains("Connection refused") || errorMessage.contains("Connect to") || errorMessage.contains("Connection timed out") || errorMessage.contains("ConnectException"))) {
        JOptionPane.showMessageDialog(mainFrame, 
            "Impossibile connettersi al server EPP.\n\n" +
            "Possibili cause:\n" +
            "- L'indirizzo IP non è nella whitelist del Registro\n" +
            "- Il firewall blocca la connessione\n" +
            "- Il server EPP non è raggiungibile\n\n" +
            "Verificare che l'indirizzo IP sia abilitato presso il Registro.", 
            "Errore di Connessione", JOptionPane.ERROR_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(mainFrame, 
            "Errore di comunicazione con il server EPP:\n" + v.getMessage(), 
            "Errore di Connessione", JOptionPane.ERROR_MESSAGE);
      }
    }
    catch (URISyntaxException v)
    {
      v.printStackTrace();
    }
    catch (XmlException v)
    {
      v.printStackTrace();
    }
    finally
    {
      if (!EPPclosable)
      {
        mainFrame.setActiveEPP(2);
        try
        {
          if (!EPPclosable)
          {
            Logout logout = new Logout();
            logger.logmessage("CLIENT: " + logout.toString() + "\n");
            response = client.sendCommand(logout);
            logger.logmessage("SERVER: " + response.toString());
            if (response.isSuccessfully())
            {
              EPPclosable = true;
              mainFrame.setActiveEPP(0);
            }
            else
            {
              if (response.getResultCode() == 2002)
              {
                EPPclosable = true;
                mainFrame.setActiveEPP(0);
              }
            }

          }
        }
        catch (IOException v)
        {
          v.printStackTrace();
        }
        catch (XmlException v)
        {
          v.printStackTrace();
        }
      }
    }
    isRunning = false;
  }

  public void restart()
  {
    if (isClosing) isClosing = false;
    if (!isRunning)
    {
      this.start();
    }

  }

  public boolean gracefulStop()
  {
    if (isRunning)
    {
      if (!isClosing) isClosing = true;
      this.interrupt();
      try
      {
        for (int i = 0; i < 10 && !EPPclosable; i++)
        {
          Thread.sleep(500);
        }
      }
      catch (InterruptedException e)
      {
      }
    }
    return EPPclosable;
  }

  public Boolean doPoll() throws XmlException, IOException
  {
    return doPoll(false);
  }

  public Boolean doPoll(Boolean doAck) throws XmlException, IOException
  {
    Boolean gotMessage = false;
    try
    {
      Poll pollCmd = new Poll();
      try
      {
        pollCmd.setReq();
      }
      catch (EppSchemaException ex)
      {
        ex.printStackTrace();
      }

      HttpBaseResponse response = this.sendCommand(pollCmd);
      logger.logmessage("CLIENT: " + pollCmd.toString());
      logger.logmessage("SERVER: " + response.toString());

      if (response.isSuccessfully())
      {
        mainFrame.setMSGQ(Integer.toString(response.getMsgQCount()));
        if (response.getMsgQCount() > 0)
        {


          if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null)
          {
            java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            java.text.SimpleDateFormat dbDateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mainFrame.setNextMsg(dateFormatter.format(response.getMsgQDate().getTime()) + " - " + response.getMsgQText());
            if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null)
            {
              Message message = new Message(response.getMsgQId(), response.getMsgQDate().getTime(), response.getMsgQText(), response.toString(), false, doAck, false);
              mainFrame.messagesDao.saveRecord(message);
              mainFrame.addMsgtoList(message);
            }
          }


          gotMessage = true;
          if (doAck)
          {
            try
            {
              pollCmd.setAck(response.getMsgQId());

              HttpBaseResponse ackResponse = this.sendCommand(pollCmd);
              logger.logmessage("CLIENT: " + pollCmd.toString());
              logger.logmessage("SERVER: " + ackResponse.toString());

              if (ackResponse.isSuccessfully())
              {
                Message message = mainFrame.messagesDao.getMessage(response.getMsgQId());
                message.setAck(true);
                mainFrame.messagesDao.editRecord(message);
              }
            }
            catch (EppSchemaException ex)
            {
              ex.printStackTrace();
            }
          }
          else
          {
            java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            java.text.SimpleDateFormat dbDateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mainFrame.setNextMsg(dateFormatter.format(response.getMsgQDate().getTime()) + " - " + response.getMsgQText());
            if (mainFrame.messagesDao.getMessage(response.getMsgQId()) == null)
            {
              Message message = new Message(response.getMsgQId(), response.getMsgQDate().getTime(), response.getMsgQText(), response.toString(), false, doAck, false);
              mainFrame.messagesDao.saveRecord(message);
              mainFrame.addMsgtoList(message);
            }
          }
          //mainFrame.setEnableMsgRecv(true);
        }
        else
        {
          mainFrame.setNextMsg("No messages");
          //mainFrame.setEnableMsgRecv(false);
        }
      }
      else
      {
        if (response.getResultCode() == 2002)
        {
          JOptionPane.showMessageDialog(mainFrame, "Rilevato errore in fase di polling messaggi.\nPossibile perdita sessione!", "Errore Polling", JOptionPane.WARNING_MESSAGE);
          this.gracefulStop();
          mainFrame.setActiveEPP(0);
        }
      }

    }
    catch (NullPointerException v)
    {
    }

    return gotMessage;
  }

  public synchronized HttpBaseResponse sendCommand(IEppRequest command) throws org.apache.xmlbeans.XmlException, IOException
  {
    HttpBaseResponse response = null;
    try
    {
      while (isWaitingEPPresponse)
      {
        Thread.sleep(100);
      }

      isWaitingEPPresponse = true;
      response = client.sendCommand(command);
      isWaitingEPPresponse = false;

    }
    catch (InterruptedException ex)
    {
      Debug.log("EPPthread", "Interrupted while waiting for EPP uplink availability.");
    }

    return response;
  }

  protected boolean isActive()
  {
    return isRunning;
  }

  private Client client = null;
  private boolean EPPclosable = true;
  private boolean isClosing = false;
  private boolean isRunning = false;
  private boolean isWaitingEPPresponse = false;
  private logger logger;
  private main mainFrame;
}