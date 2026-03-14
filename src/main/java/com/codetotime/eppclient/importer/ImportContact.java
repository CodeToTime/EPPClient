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

import com.codetotime.eppclient.contacts.Address;
import com.codetotime.eppclient.db.contactsDao;
import com.codetotime.eppclient.main;
import com.codetotime.eppclient.uplink.EppUplink;
import it.nic.epp.client.commands.query.ContactInfo;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.ext.ContactInfoResponseExt;
import it.nic.epp.client.responses.resData.ContactInfoResponseResData;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Imports a single EPP contact from the registry and saves it to the local database. */
public class ImportContact {
  private static final Logger log = LoggerFactory.getLogger(ImportContact.class);

  /**
   * Creates an import task for the given contact ID.
   *
   * @param mainFrame the application main frame providing the EPP connection
   * @param contactId the EPP contact handle to import
   */
  public ImportContact(main mainFrame, String contactId) {
    this.EPPuplink = mainFrame.EPPuplink;
    this.contactdb = mainFrame.contactsDao;
    this.contactId = contactId;
  }

  /**
   * Fetches the contact info from the registry and stores it in the local database.
   *
   * @return {@code true} if the contact was imported successfully
   */
  public boolean execute() {
    boolean importStatus = false;

    ContactInfo contactInfo = new ContactInfo();
    ContactInfoResponseResData contactInfoResData = null;
    ContactInfoResponseExt contactInfoExt = null;
    contactInfo.setId(contactId);

    try {
      HttpBaseResponse response = EPPuplink.sendCommand(contactInfo);
      if (response.isSuccessfully()) {
        contactInfoResData = (ContactInfoResponseResData) response.getResponseResData();
        contactInfoExt = (ContactInfoResponseExt) response.getResponseExtension();
        Address RegistrantContact = new Address();

        /*  contactInfoResData.getName(), contactInfoResData.getOrg(), "",
            contactInfoResData.getCity(), contactInfoResData.getSp(), contactInfoResData.getPc(),
            contactInfoResData.getCc(), contactInfoResData.getVoice(), contactInfoResData.getFax(),
            contactInfoResData.getEmail(), contactInfoExt.getConsentForPublishing(),
            contactInfoExt.isRegistrant(), contactInfoExt.getNationalityCode(),
            contactInfoExt.getEntityType(), contactInfoExt.getRegCode(), null,
            contactInfoResData.getId()

        RegistrantContact = new Address(contactInfoResData.getName(), contactInfoResData.getOrg(),
            contactInfoResData.getStreet(0), contactInfoResData.getCity(),
            contactInfoResData.getSp(), contactInfoResData.getPc(), contactInfoResData.getCc(),
            contactInfoResData.getVoice(), contactInfoResData.getFax(),
            contactInfoResData.getEmail(), contactInfoExt.getConsentForPublishing(),
            contactInfoExt.isRegistrant(), contactInfoExt.getNationalityCode(),
            contactInfoExt.getEntityType(), contactInfoExt.getRegCode(), null,
            contactInfoResData.getId());*/

        try {
          RegistrantContact.setContactName(contactInfoResData.getName());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setOrg(contactInfoResData.getOrg());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setStreet(contactInfoResData.getStreet(0));
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setCity(contactInfoResData.getCity());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setStateOrProvince(contactInfoResData.getSp());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setPostalCode(contactInfoResData.getPc());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setCountryCode(contactInfoResData.getCc());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setVoice(contactInfoResData.getVoice());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setFax(contactInfoResData.getFax());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setEmail(contactInfoResData.getEmail());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setSchoolCode(contactInfoExt.getSchoolCode());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setConsentForPublishing(contactInfoExt.getConsentForPublishing());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setIsRegistrant(contactInfoExt.isRegistrant());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setNationalityCode(contactInfoExt.getNationalityCode());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setEntityType(contactInfoExt.getEntityType());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setRegCode(contactInfoExt.getRegCode());
        } catch (NullPointerException e) {
          // discard field;
        }

        try {
          RegistrantContact.setContactId(contactInfoResData.getId());
        } catch (NullPointerException e) {
          // discard field;
        }

        if (contactdb.getAddress(contactId) == null) {
          contactdb.saveRecord(RegistrantContact);
        } else {
          contactdb.editRecord(RegistrantContact);
        }

        importStatus = true;
        RegistrantContact = null;
      }
    } catch (XmlException v) {
      log.error("XmlException in execute", v);
    } catch (IOException v) {
      log.error("IOException in execute", v);
    }

    return importStatus;
  }

  private EppUplink EPPuplink;
  private contactsDao contactdb;
  private String contactId;
}
