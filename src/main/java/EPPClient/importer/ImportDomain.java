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

package EPPClient.importer;


import EPPClient.db.contactsDao;
import EPPClient.db.domainsDao;
import EPPClient.domains.Domain;
import EPPClient.logger;
import EPPClient.main;
import EPPClient.uplink.EPPuplink;
import it.nic.epp.client.commands.query.DomainInfo;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.resData.DomainInfoResponseResData;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;

public class ImportDomain
{

  public ImportDomain(main mainFrame, boolean recurseContacts)
  {
    this.mainFrame = mainFrame;
    this.EPPuplink = mainFrame.EPPuplink;
    this.domaindb = mainFrame.domainsDao;
    this.contactdb = mainFrame.contactsDao;
    this.recurseContacts = recurseContacts;

    logger = new logger("DOMAIN");
  }

  public boolean execute(String domainName)
  {
    boolean importStatus = true;

    DomainInfo domainInfo = new DomainInfo();
    domainInfo.setName(domainName);

    try
    {

      logger.logmessage("CLIENT: " + domainInfo.toString());
      HttpBaseResponse response = EPPuplink.sendCommand(domainInfo);
      logger.logmessage("SERVER: " + response.toString());

      if (response.isSuccessfully())
      {
        DomainInfoResponseResData domainInfoResData = (DomainInfoResponseResData) response.getResponseResData();

        Domain transferredDomain = new Domain(domainInfoResData.getName(), domainInfoResData.getRegistrant(), domainInfoResData.getAdmins(), domainInfoResData.getTechs(), domainInfoResData.getNs(), domainInfoResData.getAuthInfo(), domainInfoResData.getStatuses(), domainInfoResData.getExDate().getTime());

        if (domaindb.getDomain(domainInfoResData.getName()) == null)
        {
          domaindb.saveRecord(transferredDomain);
        }
        else
        {
          domaindb.editRecord(transferredDomain);
        }

        if (recurseContacts)
        {

          ImportContact importRegistrant = new ImportContact(mainFrame, domainInfoResData.getRegistrant());
          importRegistrant.execute();
          importRegistrant = null;

          for (String admin : domainInfoResData.getAdmins())
          {
            ImportContact importAdmin = new ImportContact(mainFrame, admin);
            importAdmin.execute();
            importAdmin = null;
          }

          for (String tech : domainInfoResData.getTechs())
          {
            ImportContact importTech = new ImportContact(mainFrame, tech);
            importTech.execute();
            importTech = null;
          }
        }
      }
      else
      {
        importResultCode = response.getResultCode();
        importReasonCode = response.getReasonCode();

        importStatus = false;
      }
    }
    catch (XmlException v)
    {
      v.printStackTrace();
    }
    catch (IOException v)
    {
      v.printStackTrace();
    }


    return importStatus;
  }

  public Integer getResultCode()
  {
    return importResultCode;
  }

  public Integer getReasonCode()
  {
    return importReasonCode;
  }

  private int importResultCode;
  private int importReasonCode;
  private EPPuplink EPPuplink;
  private domainsDao domaindb;
  private contactsDao contactdb;
  //private String domainName;
  private boolean recurseContacts;
  private boolean closedb = false;
  private logger logger;
  private main mainFrame;
}
