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

import EPPClient.contacts.Address;
import EPPClient.db.contactsDao;
import EPPClient.main;
import EPPClient.uplink.EPPuplink;
import it.nic.epp.client.commands.query.ContactInfo;
import it.nic.epp.client.responses.HttpBaseResponse;
import it.nic.epp.client.responses.ext.ContactInfoResponseExt;
import it.nic.epp.client.responses.resData.ContactInfoResponseResData;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;

public class ImportContact
{
  public ImportContact(main mainFrame, String contactId)
  {
    this.EPPuplink = mainFrame.EPPuplink;
    this.contactdb = mainFrame.contactsDao;
    this.contactId = contactId;
  }

  public boolean execute()
  {
    boolean importStatus = false;

    ContactInfo contactInfo = new ContactInfo();
    ContactInfoResponseResData contactInfoResData = null;
    ContactInfoResponseExt contactInfoExt = null;
    contactInfo.setId(contactId);

    try
    {
      HttpBaseResponse response = EPPuplink.sendCommand(contactInfo);
      if (response.isSuccessfully())
      {
        contactInfoResData = (ContactInfoResponseResData) response.getResponseResData();
        contactInfoExt = (ContactInfoResponseExt) response.getResponseExtension();
        Address RegistrantContact = new Address();

/*                contactInfoResData.getName(), contactInfoResData.getOrg(), "", contactInfoResData.getCity(), contactInfoResData.getSp(), contactInfoResData.getPc(),
                            contactInfoResData.getCc(), contactInfoResData.getVoice(), contactInfoResData.getFax(), contactInfoResData.getEmail(), contactInfoExt.getConsentForPublishing(), contactInfoExt.isRegistrant(),
                            contactInfoExt.getNationalityCode(), contactInfoExt.getEntityType(), contactInfoExt.getRegCode(), null, contactInfoResData.getId()


                        RegistrantContact = new Address(contactInfoResData.getName(), contactInfoResData.getOrg(), contactInfoResData.getStreet(0), contactInfoResData.getCity(), contactInfoResData.getSp(), contactInfoResData.getPc(),
                            contactInfoResData.getCc(), contactInfoResData.getVoice(), contactInfoResData.getFax(), contactInfoResData.getEmail(), contactInfoExt.getConsentForPublishing(), contactInfoExt.isRegistrant(),
                            contactInfoExt.getNationalityCode(), contactInfoExt.getEntityType(), contactInfoExt.getRegCode(), null, contactInfoResData.getId());*/

        try
        {
          RegistrantContact.setContactName(contactInfoResData.getName());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setOrg(contactInfoResData.getOrg());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setStreet(contactInfoResData.getStreet(0));
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setCity(contactInfoResData.getCity());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setStateOrProvince(contactInfoResData.getSp());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setPostalCode(contactInfoResData.getPc());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setCountryCode(contactInfoResData.getCc());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setVoice(contactInfoResData.getVoice());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setFax(contactInfoResData.getFax());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setEmail(contactInfoResData.getEmail());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setSchoolCode(contactInfoExt.getSchoolCode());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setConsentForPublishing(contactInfoExt.getConsentForPublishing());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setIsRegistrant(contactInfoExt.isRegistrant());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setNationalityCode(contactInfoExt.getNationalityCode());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setEntityType(contactInfoExt.getEntityType());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setRegCode(contactInfoExt.getRegCode());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        try
        {
          RegistrantContact.setContactId(contactInfoResData.getId());
        }
        catch (NullPointerException e)
        {
          //discard field;
        }

        if (contactdb.getAddress(contactId) == null)
        {
          contactdb.saveRecord(RegistrantContact);
        }
        else
        {
          contactdb.editRecord(RegistrantContact);
        }

        importStatus = true;
        RegistrantContact = null;
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

  private EPPuplink EPPuplink;
  private contactsDao contactdb;
  private String contactId;
}
