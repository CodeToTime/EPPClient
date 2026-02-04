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

package EPPClient.contacts;

import EPPClient.config.EPPparams;

public class Address
{

  /**
   * Creates a new instance of Address
   */
  public Address()
  {
  }

  public Address(String name, String org, String street, String city, String stateOrProvince, String postalCode,
                 String countryCode, String voice, String fax, String email, boolean consentForPublishing, boolean isRegistrant,
                 String nationalityCode, Integer entityType, String regCode, int[] oldStatus, String contactId, String schoolCode, String ipaCode, String uoCode)
  {

    this.name = name;
    this.org = org;
    this.street = street;
    this.city = city;
    this.stateOrProvince = stateOrProvince;
    this.postalCode = postalCode;
    this.countryCode = countryCode;
    this.voice = voice;
    this.fax = fax;
    this.email = email;
    this.consentForPublishing = consentForPublishing;
    this.isRegistrant = isRegistrant;
    this.nationalityCode = nationalityCode;
    if (entityType instanceof Integer)
    {
      this.entityType = entityType;
    }
    else
    {
      this.entityType = 0;
    }

    this.regCode = regCode;
    this.newStatus = oldStatus;
    this.oldStatus = oldStatus;
    this.contactId = contactId;
    this.schoolCode = schoolCode;
    this.ipaCode = ipaCode;
    this.uoCode = uoCode;
  }

  public void setIpaCode(String ipaCode)
  {
    this.ipaCode = ipaCode;
  }

  public String getIpaCode()
  {
    return this.ipaCode;
  }

  public void setUoCode(String uoCode)
  {
    this.uoCode = uoCode;
  }

  public String getUoCode()
  {
    return this.uoCode;
  }

  public void setSchoolCode(String schoolCode)
  {
    this.schoolCode = schoolCode;
  }

  public String getSchoolCode()
  {
    return this.schoolCode;
  }

  public void setContactId(String contactId)
  {
    this.contactId = contactId;
  }

  public String getContactId()
  {
    return this.contactId;
  }

  public void setAutoContactId(boolean autoContactId)
  {
    this.autoContactId = autoContactId;
  }

  public boolean getAutoContactId()
  {
    return this.autoContactId;
  }

  public void setContactName(String name)
  {
    this.name = name;
  }

  public String getContactName()
  {
    return this.name;
  }

  public void setOrg(String org)
  {
    this.org = org;
  }

  public String getOrg()
  {
    return this.org;
  }

  public void setStreet(String street)
  {
    this.street = street;
  }

  public String getStreet()
  {
    return this.street;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public String getCity()
  {
    return this.city;
  }

  public void setStateOrProvince(String stateOrProvince)
  {
    this.stateOrProvince = stateOrProvince;
  }

  public String getStateOrProvince()
  {
    return this.stateOrProvince;
  }

  public void setPostalCode(String postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getPostalCode()
  {
    return this.postalCode;
  }

  public void setCountryCode(String countryCode)
  {
    this.countryCode = countryCode;
  }

  public String getCountryCode()
  {
    return this.countryCode;
  }

  public void setVoice(String voice)
  {
    this.voice = voice;
  }

  public String getVoice()
  {
    return this.voice;
  }

  public void setFax(String fax)
  {
    this.fax = fax;
  }

  public String getFax()
  {
    return this.fax;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getEmail()
  {
    return this.email;
  }

  public void setConsentForPublishing(boolean consentForPublishing)
  {
    this.consentForPublishing = consentForPublishing;
  }

  public boolean getConsentForPublishing()
  {
    return this.consentForPublishing;
  }

  public void setIsRegistrant(boolean isRegistrant)
  {
    this.isRegistrant = isRegistrant;
  }

  public boolean getIsRegistrant()
  {
    return this.isRegistrant;
  }

  public void setNationalityCode(String nationalityCode)
  {
    this.nationalityCode = nationalityCode;
  }

  public String getNationalityCode()
  {
    return this.nationalityCode;
  }

  public void setEntityType(int entityType)
  {
    this.entityType = entityType;
  }

  public int getEntityType()
  {
    return this.entityType;
  }

  public void setRegCode(String regCode)
  {
    this.regCode = regCode;
  }

  public String getRegCode()
  {
    return this.regCode;
  }

  public void setOldStatus(int[] oldStatus)
  {
    this.oldStatus = oldStatus;
  }

  public int[] getOldStatus()
  {
    return this.oldStatus;
  }

  public void setNewStatus(int[] newStatus)
  {
    this.newStatus = newStatus;
  }

  public int[] getNewStatus()
  {
    return this.newStatus;
  }

  public void setIsNewContact(boolean isNewContact)
  {
    this.isNewContact = isNewContact;
  }

  public boolean getIsNewContact()
  {
    return this.isNewContact;
  }

  public String getRandomContactId()
  {
    String contactId = Integer.toString((int) (Math.random() * 100000000));
    contactId = EPPparams.getParameter("EppClient.contactPrefix") + contactId.substring(contactId.length() - 7);
    return contactId;
  }

  public boolean isNameChanged()
  {
    return isNameChanged;
  }

  public boolean isOrgChanged()
  {
    return isOrgChanged;
  }

  public boolean isStreetChanged()
  {
    return isStreetChanged;
  }

  public boolean isCityChanged()
  {
    return isCityChanged;
  }

  public boolean isStateOrProvinceChanged()
  {
    return isStateOrProvinceChanged;
  }

  public boolean isPostalCodeChanged()
  {
    return isPostalCodeChanged;
  }

  public boolean isCountryCodeChanged()
  {
    return isCountryCodeChanged;
  }

  public boolean isVoiceChanged()
  {
    return isVoiceChanged;
  }

  public boolean isFaxChanged()
  {
    return isFaxChanged;
  }

  public boolean isEmailChanged()
  {
    return isEmailChanged;
  }

  public boolean isConsentForPublishingChanged()
  {
    return isConsentForPublishingChanged;
  }

  public void isIsRegistrantChanged(boolean isIsRegistrantChanged)
  {
    this.isIsRegistrantChanged = isIsRegistrantChanged;
  }

  public boolean isIsRegistrantChanged()
  {
    return isIsRegistrantChanged;
  }

  public boolean isNationalityCodeChanged()
  {
    return isNationalityCodeChanged;
  }

  public boolean isEntityTypeChanged()
  {
    return isEntityTypeChanged;
  }

  public boolean isRegCodeChanged()
  {
    return isRegCodeChanged;
  }

  public boolean isSchoolCodeChanged()
  {
    return isSchoolCodeChanged;
  }

  private String contactId;
  private boolean autoContactId;
  private String name;
  private String org;
  private String street;
  private String city;
  private String stateOrProvince;
  private String postalCode;
  private String countryCode;
  private String voice;
  private String fax;
  private String email;
  private boolean consentForPublishing;
  private boolean isRegistrant;
  private String nationalityCode;
  private int entityType;
  private String regCode;
  private boolean isNewContact;
  private String schoolCode;
  private String ipaCode;
  private String uoCode;
  private int[] newStatus;
  private int[] oldStatus;

  private boolean isNameChanged;
  private boolean isOrgChanged;
  private boolean isStreetChanged;
  private boolean isCityChanged;
  private boolean isStateOrProvinceChanged;
  private boolean isPostalCodeChanged;
  private boolean isCountryCodeChanged;
  private boolean isVoiceChanged;
  private boolean isFaxChanged;
  private boolean isEmailChanged;
  private boolean isConsentForPublishingChanged;
  private boolean isIsRegistrantChanged;
  private boolean isNationalityCodeChanged;
  private boolean isEntityTypeChanged;
  private boolean isRegCodeChanged;
  private boolean isSchoolCodeChanged;

  private static final int PRIMENO = 37;

}
