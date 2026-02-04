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

public class ListEntry
{

  /**
   * Creates a new instance of ListEntry
   */
  public ListEntry()
  {
    this("", "");
  }

  public ListEntry(String name, String contactId)
  {
    this.name = name;
//        this.firstName = firstName;
//        this.middleName = middleName;
    this.contactId = contactId;
  }

  public String getContactName()
  {
    return name;
  }

  public void setContactName(String name)
  {
    this.name = name;
  }

  public String getContactId()
  {
    return contactId;
  }

  public void setContactId(String contactId)
  {
    this.contactId = contactId;
  }

  public String toString()
  {
    String value = name + ": " + contactId; //+ ", " + firstName + " " + middleName + ": " + id;
    return value;

  }

  private String name;
  private String contactId;
}
