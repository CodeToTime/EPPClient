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

package com.codetotime.eppclient.importer;

import com.codetotime.eppclient.db.contactsDao;
import com.codetotime.eppclient.db.domainsDao;
import com.codetotime.eppclient.domains.Domain;
import com.codetotime.eppclient.main;
import com.codetotime.eppclient.uplink.EPPuplink;
import it.nic.epp.client.commands.query.DomainInfo;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.resData.DomainInfoResponseResData;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports a domain and optionally its linked contacts from the registry into the local database.
 */
public class ImportDomain {

  /**
   * Creates an import task using the given main frame's EPP connection.
   *
   * @param mainFrame the application main frame providing the EPP connection
   * @param recurseContacts {@code true} to also import all contacts linked to the domain
   */
  public ImportDomain(main mainFrame, boolean recurseContacts) {
    this.mainFrame = mainFrame;
    this.EPPuplink = mainFrame.EPPuplink;
    this.domaindb = mainFrame.domainsDao;
    this.contactdb = mainFrame.contactsDao;
    this.recurseContacts = recurseContacts;

    log = LoggerFactory.getLogger(ImportDomain.class);
  }

  /**
   * Fetches the domain info from the registry and stores it in the local database.
   *
   * @param domainName the fully qualified domain name to import
   * @return {@code true} if the domain was imported successfully
   */
  public boolean execute(String domainName) {
    boolean importStatus = true;

    DomainInfo domainInfo = new DomainInfo();
    domainInfo.setName(domainName);

    try {

      log.debug("CLIENT: {}", domainInfo);
      HttpBaseResponse response = EPPuplink.sendCommand(domainInfo);
      log.debug("SERVER: {}", response);

      if (response.isSuccessfully()) {
        DomainInfoResponseResData domainInfoResData =
            (DomainInfoResponseResData) response.getResponseResData();

        Domain transferredDomain =
            new Domain(
                domainInfoResData.getName(),
                domainInfoResData.getRegistrant(),
                domainInfoResData.getAdmins(),
                domainInfoResData.getTechs(),
                domainInfoResData.getNs(),
                domainInfoResData.getAuthInfo(),
                domainInfoResData.getStatuses(),
                domainInfoResData.getExDate().getTime());

        if (domaindb.getDomain(domainInfoResData.getName()) == null) {
          domaindb.saveRecord(transferredDomain);
        } else {
          domaindb.editRecord(transferredDomain);
        }

        if (recurseContacts) {

          ImportContact importRegistrant =
              new ImportContact(mainFrame, domainInfoResData.getRegistrant());
          importRegistrant.execute();
          importRegistrant = null;

          for (String admin : domainInfoResData.getAdmins()) {
            ImportContact importAdmin = new ImportContact(mainFrame, admin);
            importAdmin.execute();
            importAdmin = null;
          }

          for (String tech : domainInfoResData.getTechs()) {
            ImportContact importTech = new ImportContact(mainFrame, tech);
            importTech.execute();
            importTech = null;
          }
        }
      } else {
        importResultCode = response.getResultCode();
        importReasonCode = response.getReasonCode();

        importStatus = false;
      }
    } catch (XmlException v) {
      log.error("XmlException in execute", v);
    } catch (IOException v) {
      log.error("IOException in execute", v);
    }

    return importStatus;
  }

  public Integer getResultCode() {
    return importResultCode;
  }

  public Integer getReasonCode() {
    return importReasonCode;
  }

  private int importResultCode;
  private int importReasonCode;
  private EPPuplink EPPuplink;
  private domainsDao domaindb;
  private contactsDao contactdb;
  // private String domainName;
  private boolean recurseContacts;
  private boolean closedb = false;
  private Logger log;
  private main mainFrame;
}
