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

package EPPClient.domains.transfer;

public class DomainTransfer
{

  /**
   * Creates a new instance of Address
   */
  public DomainTransfer()
  {
  }

  public DomainTransfer(String domainName, String registrant, String authInfo, String newAuthInfo, boolean isTrade, boolean isCancel)
  {

    this.domainName = domainName;
    this.registrant = registrant;
    this.authInfo = authInfo;
    this.newAuthInfo = newAuthInfo;
    this.isTrade = isTrade;
    this.isCancel = isCancel;
  }

  public void setDomainName(String domainName)
  {
    this.domainName = domainName;
  }

  public String getDomainName()
  {
    return this.domainName;
  }

  public void setRegistrant(String registrant)
  {
    this.registrant = registrant;
  }

  public String getRegistrant()
  {
    return this.registrant;
  }

  public void setAuthInfo(String authInfo)
  {
    this.authInfo = authInfo;
  }

  public String getAuthInfo()
  {
    return this.authInfo;
  }

  public void setNewAuthInfo(String newAuthInfo)
  {
    this.newAuthInfo = newAuthInfo;
  }

  public String getNewAuthInfo()
  {
    return this.newAuthInfo;
  }

  public void setIsTrade(boolean isTrade)
  {
    this.isTrade = isTrade;
  }

  public boolean getIsTrade()
  {
    return this.isTrade;
  }

  public void setIsCancel(boolean isCancel)
  {
    this.isCancel = isCancel;
  }

  public boolean getIsCancel()
  {
    return this.isCancel;
  }

  private String contactId;
  private boolean autoContactId;
  private String domainName;
  private String registrant;
  private String authInfo;
  private String newAuthInfo;
  private boolean isTrade;
  private boolean isCancel;

  private static final int PRIMENO = 37;

}
