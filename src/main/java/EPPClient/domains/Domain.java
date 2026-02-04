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

package EPPClient.domains;

import it.nic.epp.client.commands.converters.AbstractHostsConverter;
import it.nic.epp.client.commands.converters.AbstractHostsConverter.Host;

import java.util.Date;
import java.util.Vector;

public class Domain
{

  /**
   * Creates a new instance of Address
   */
  public Domain()
  {
    this.oldStatusV = new Vector();
    this.newStatusV = new Vector();
  }

  public Domain(String domainName, String registrant, String[] admin, String[] tech, String[] nameServer, String authInfo, Vector oldStatus, Date expire, String validationCode, boolean isDNSSec, String keyTag, int alg, int digestType, String digest)
  {

    this.domainName = domainName;
    this.registrant = registrant;
    this.admin = admin;
    this.oldAdmin = admin;
    this.tech = tech;
    this.oldTech = tech;

    if (nameServer instanceof String[])
    {
      for (int i = 0; i < nameServer.length; i++)
      {
        if (nameServer[i].contains(":") && !nameServer[i].contains("@"))
        {
          nameServer[i] = nameServer[i].replace(":", "@");
        }
      }
    }

    this.nameServer = nameServer;
    this.oldNameServer = nameServer;
    this.authInfo = authInfo;
    this.oldAuthInfo = authInfo;
    this.newStatusV = oldStatus;
    this.oldStatusV = oldStatus;
    this.expire = expire;
    this.validationCode = validationCode;
    this.isDNSSec = isDNSSec;
    this.keyTag = keyTag;
    this.alg = alg;
    this.digestType = digestType;
    this.digest =digest;
  }

  public Domain(String domainName, String registrant, String[] admin, String[] tech, AbstractHostsConverter.Host[] nameServer, String authInfo, String[] oldStatus, Date expire)
  {
    String[] nameServerStr;
    if (nameServer != null)
    {
      nameServerStr = new String[nameServer.length];
      for (int i = 0; i < nameServer.length; i++)
      {
        AbstractHostsConverter.Host ns = nameServer[i];
        nameServerStr[i] = ns.getName();
        for (int j = 0; j < ns.getIpSize(); j++)
        {
          nameServerStr[i] += "@" + ns.getIp(j).address;
        }
      }
    }
    else
    {
      nameServerStr = new String[0];
    }


    this.oldStatusV = new Vector();
    for (int i = 0; i < oldStatus.length; i++)
    {
      if (oldStatus[i].equals("clientDeleteProhibited"))
      {
        this.oldStatusV.add(0);
      }
      else if (oldStatus[i].equals("clientTransferProhibited"))
      {
        this.oldStatusV.add(1);
      }
      else if (oldStatus[i].equals("clientUpdateProhibited"))
      {
        this.oldStatusV.add(2);
      }
      else if (oldStatus[i].equals("pendingDelete"))
      {
        this.oldStatusV.add(99);
      }
    }


    this.domainName = domainName;
    this.registrant = registrant;
    this.admin = admin;
    this.oldAdmin = admin;
    this.tech = tech;
    this.oldTech = tech;
    this.nameServer = nameServerStr;
    this.oldNameServer = nameServerStr;
    this.authInfo = authInfo;
    this.oldAuthInfo = authInfo;
    this.newStatusV = oldStatusV;
    this.expire = expire;
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

  public void setOldAdmin(String[] oldAdmin)
  {
    this.oldAdmin = oldAdmin;
  }

  public String[] getOldAdmin()
  {
    return this.oldAdmin;
  }

  public void setAdmin(String[] admin)
  {
    this.admin = admin;
  }

  public String[] getAdmin()
  {
    return this.admin;
  }

  public void setOldTech(String[] oldTech)
  {
    this.oldTech = oldTech;
  }

  public String[] getOldTech()
  {
    return this.oldTech;
  }

  public void setTech(String[] tech)
  {
    this.tech = tech;
  }

  public String[] getTech()
  {
    return this.tech;
  }

  public void setNameServer(String[] nameServer)
  {
    this.nameServer = nameServer;
  }

  public String[] getNameServer()
  {
    return this.nameServer;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

  public int getDigestType() {
    return digestType;
  }

  public void setDigestType(int digestType) {
    this.digestType = digestType;
  }

  public int getAlg() {
    return alg;
  }

  public void setAlg(int alg) {
    this.alg = alg;
  }

  public boolean isDNSSec() {
    return isDNSSec;
  }

  public void setDNSSec(boolean DNSSec) {
    isDNSSec = DNSSec;
  }

  public String getKeyTag() {
    return keyTag;
  }

  public void setKeyTag(String keyTag) {
    this.keyTag = keyTag;
  }

  public void setNameServer(Host[] nameServers)
  {
    if (nameServers != null)
    {
      String[] newNameServer = new String[nameServers.length];
      for (int i = 0; i < nameServers.length; i++)
      {
        newNameServer[i] = nameServers[i].name + ":" + nameServers[i].getIps()[0].address;
      }
      setNameServer(newNameServer);
    }
  }

  public void setOldNameServer(String[] oldNameServer)
  {
    this.oldNameServer = oldNameServer;
  }

  public String[] getOldNameServer()
  {
    return this.oldNameServer;
  }

  public void setAuthInfo(String authInfo)
  {
    this.authInfo = authInfo;
  }

  public void setOldAuthInfo(String oldAuthInfo)
  {
    this.oldAuthInfo = oldAuthInfo;
  }

  public String getAuthInfo()
  {
    return this.authInfo;
  }

  public String getOldAuthInfo()
  {
    return this.oldAuthInfo;
  }

/*    public void setOldStatus(int[] oldStatus) {
        this.oldStatus = oldStatus;
    }*/

  public void setOldStatus(Vector oldStatus)
  {
    this.oldStatusV = oldStatus;
  }

/*    public int[] getOldStatus() {
        return this.oldStatus;
    }*/

  public Vector getOldStatusV()
  {
    return this.oldStatusV;
  }

/*    public void setNewStatus(int[] newStatus) {
        this.newStatus = newStatus;
    }*/

  public void setNewStatus(Vector newStatus)
  {
    this.newStatusV = newStatus;
  }

/*    public int[] getNewStatus() {
        return this.newStatus;
    }*/

  public Vector getNewStatusV()
  {
    return this.newStatusV;
  }


  public void setExpire(Date expire)
  {
    this.expire = expire;
  }

  public Date getExpire()
  {
    return this.expire;
  }

  public void setOpType(int opType)
  {
    this.opType = opType;
  }

  public int getOpType()
  {
    return this.opType;
  }

  public void setIsRegistrantChanged(boolean isRegistrantChanged)
  {
    this.isRegistrantChanged = isRegistrantChanged;
  }

  public boolean getIsRegistrantChanged()
  {
    return this.isRegistrantChanged;
  }

  public void setValidationCode(String validationCode) {
    this.validationCode = validationCode;
  }

  public String getValidationCode() {
    return this.validationCode;
  }

  private String contactId;
  private boolean autoContactId;
  private String domainName;
  private String registrant;
  private String[] admin;
  private String[] oldAdmin;
  private String[] tech;
  private String[] oldTech;
  private String[] nameServer;
  private String[] oldNameServer;
  private String authInfo;
  private String oldAuthInfo;
  private Date expire;
  private int opType;
  private boolean isRegistrantChanged;

/*    private int[] newStatus;
    private int[] oldStatus;*/

  private Vector newStatusV;
  private Vector oldStatusV;

  private String validationCode;

  private static final int PRIMENO = 37;

  private boolean isDNSSec;
  private int alg;
  private int digestType;
  private String digest;
  private String keyTag;
}
