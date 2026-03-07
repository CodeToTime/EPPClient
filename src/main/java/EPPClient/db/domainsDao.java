/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 * SPDX-FileCopyrightText: 2026 Matteo Trubini @ CUBIC S.R.L. <https://cubicsrl.it/>
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

package EPPClient.db;

import EPPClient.config.EPPparams;
import EPPClient.domains.Domain;
import EPPClient.domains.ListEntry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.jdbi.v3.core.Handle;

public class domainsDao
{

  /**
   * Creates a new instance of AddressDao
   */
  public domainsDao()
  {
    this("domains");
  }

  public domainsDao(String addressBookName)
  {
    strDropDomainTable = "drop table " + tableName;

    strCreateAddressTable =
            "create table " + tableName + " (" +
                    "    DOMAINNAME     VARCHAR(255) NOT NULL PRIMARY KEY," +
                    "    REGISTRANT     VARCHAR(255), " +
                    "    ADMIN          VARCHAR(255), " +
                    "    TECH           VARCHAR(255), " +
                    "    NAMESERVER     VARCHAR(255), " +
                    "    AUTHINFO       VARCHAR(255), " +
                    "    STATUS         VARCHAR(255), " +
                    "    EXPIRE         DATE, " +
                    "    VALIDATIONCODE VARCHAR(255), " +
                    "    ISDNSSEC       SMALLINT, " +
                    "    DNSSECKEYTAG   VARCHAR(255), " +
                    "    DNSSECALG      INTEGER, " +
                    "    DNSSECDIGESTTYPE INTEGER, " +
                    "    DNSSECDIGEST   VARCHAR(255) " +
                    ")";

    strGetAddress =
            "SELECT * FROM " + tableName + " " +
                    "WHERE DOMAINNAME = ?";

    strSaveAddress =
            "INSERT INTO " + tableName + " " +
                    "   (DOMAINNAME, REGISTRANT, ADMIN, TECH, NAMESERVER, AUTHINFO, STATUS, EXPIRE, VALIDATIONCODE, ISDNSSEC, DNSSECKEYTAG, DNSSECALG, DNSSECDIGESTTYPE, DNSSECDIGEST) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    strGetListEntries =
            "SELECT DOMAINNAME FROM " + tableName + " " +
                    "ORDER BY DOMAINNAME ASC";

    strUpdateAddress =
            "UPDATE " + tableName + " " +
                    "SET REGISTRANT = ?, " +
                    "    ADMIN = ?, " +
                    "    TECH = ?, " +
                    "    NAMESERVER = ?, " +
                    "    AUTHINFO = ?, " +
                    "    STATUS = ?, " +
                    "    EXPIRE = ?, " +
                    "    VALIDATIONCODE = ?, " +
                    "    ISDNSSEC = ?, " +
                    "    DNSSECKEYTAG = ?, " +
                    "    DNSSECALG = ?, " +
                    "    DNSSECDIGESTTYPE = ?, " +
                    "    DNSSECDIGEST = ? " +
                    "WHERE DOMAINNAME = ?";

    strDeleteAddress =
            "DELETE FROM " + tableName + " " +
                    "WHERE DOMAINNAME = ?";

    // Initialize DbHelper
    if (!DbHelper.isInitialized())
    {
      DbHelper.initialize();
    }
    this.dbHelper = DbHelper.getInstance();

    // Try to create tables if they don't exist, otherwise add missing columns
    if (!dbHelper.tableExists(tableName))
    {
      dbHelper.executeSql(strCreateAddressTable);
    }
    else
    {
      addMissingColumns();
    }
  }

  private void addMissingColumns()
  {
    try (Connection dbConnection = dbHelper.getConnection())
    {
      addColumnIfNotExists(dbConnection, tableName, "EXPIRE", "DATE");
      addColumnIfNotExists(dbConnection, tableName, "VALIDATIONCODE", "VARCHAR(255)");
      addColumnIfNotExists(dbConnection, tableName, "ISDNSSEC", "SMALLINT");
      addColumnIfNotExists(dbConnection, tableName, "DNSSECKEYTAG", "VARCHAR(255)");
      addColumnIfNotExists(dbConnection, tableName, "DNSSECALG", "INTEGER");
      addColumnIfNotExists(dbConnection, tableName, "DNSSECDIGESTTYPE", "INTEGER");
      addColumnIfNotExists(dbConnection, tableName, "DNSSECDIGEST", "VARCHAR(255)");
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Verifica se una colonna esiste nella tabella specificata.
   * Gestisce le differenze di case-sensitivity tra Derby (maiuscolo) e MySQL/MariaDB (minuscolo).
   */
  private boolean columnExists(Connection conn, String tableName, String columnName)
  {
    try (ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, null))
    {
      while (columns.next())
      {
        if (columns.getString("COLUMN_NAME").equalsIgnoreCase(columnName))
        {
          return true;
        }
      }
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Aggiunge una colonna alla tabella se non esiste già.
   */
  private void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType)
  {
    if (columnExists(conn, tableName, columnName))
    {
      return; // La colonna esiste già, niente da fare
    }

    String fullTableName = EPPparams.getParameter("EppClient.dbname") + "." + tableName;
    try (Statement statement = conn.createStatement())
    {
      statement.execute("ALTER TABLE " + fullTableName + " ADD COLUMN " + columnName + " " + columnType);
      System.out.println("Aggiunta colonna " + columnName + " alla tabella " + fullTableName);
    }
    catch (SQLException ex)
    {
      // La colonna potrebbe già esistere (race condition) o altro errore
      ex.printStackTrace();
    }
  }

  public boolean dropTables()
  {
    dbHelper.executeSql(strDropDomainTable);
    return true;
  }

  public boolean connect()
  {
    try
    {
      handle = dbHelper.connect();
      return handle != null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return false;
    }
  }

  public void disconnect()
  {
    dbHelper.disconnect(handle);
    handle = null;
  }

  public String saveRecord(Domain record)
  {
    try (Handle h = dbHelper.connect())
    {
      String newAdmin = "";
      String[] newAdmins = record.getAdmin();
      if (newAdmins != null)
      {
        for (int adminIndex = 0; adminIndex < newAdmins.length; adminIndex++)
        {
          if (newAdmin.length() > 0) newAdmin = newAdmin + ";";
          newAdmin = newAdmin + newAdmins[adminIndex];
        }
      }

      String newTech = "";
      String[] newTechs = record.getTech();
      if (newTechs != null)
      {
        for (int techIndex = 0; techIndex < newTechs.length; techIndex++)
        {
          if (newTech.length() > 0) newTech = newTech + ";";
          newTech = newTech + newTechs[techIndex];
        }
      }

      String newNameServer = "";
      String[] newNameServers = record.getNameServer();
      if (newNameServers != null)
      {
        for (int nameServerIndex = 0; nameServerIndex < newNameServers.length; nameServerIndex++)
        {
          if (newNameServer.length() > 0) newNameServer = newNameServer + ";";
          newNameServer = newNameServer + newNameServers[nameServerIndex];
        }
      }

      String newStatus = "";
      if (record.getNewStatusV() != null)
        for (int statusIndex = 0; statusIndex < record.getNewStatusV().size(); statusIndex++)
        {
          if (newStatus.length() > 0) newStatus = newStatus + ",";
          newStatus += record.getNewStatusV().get(statusIndex);
        }

      java.sql.Date expireDate = null;
      if (record.getExpire() instanceof java.util.Date)
      {
        expireDate = new java.sql.Date(((java.util.Date) record.getExpire()).getTime());
      }

      h.execute(strSaveAddress, record.getDomainName(), record.getRegistrant(), newAdmin, newTech, newNameServer, record.getAuthInfo(), newStatus, expireDate, record.getValidationCode(), record.isDNSSec() ? 1 : 0, record.getKeyTag(), record.getAlg(), record.getDigestType(), record.getDigest());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return record.getDomainName();
  }

  private String arrayToString(String[] arr)
  {
    if (arr == null)
      return "";
    return String.join(";", arr);
  }

  private String vectorToString(Vector<Integer> vec)
  {
    if (vec == null || vec.isEmpty())
      return "";
    return vec.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
  }

  public boolean editRecord(Domain record)
  {
    boolean bEdited = false;
    try (Handle h = dbHelper.connect())
    {
      String admin = arrayToString(record.getAdmin());
      String tech = arrayToString(record.getTech());
      String nameServer = arrayToString(record.getNameServer());
      String status = vectorToString(record.getNewStatusV());
      java.sql.Date expireDate = null;
      if (record.getExpire() instanceof java.util.Date)
      {
        expireDate = new java.sql.Date(((java.util.Date) record.getExpire()).getTime());
      }

      h.execute(strUpdateAddress, record.getRegistrant(), admin, tech, nameServer, record.getAuthInfo(), status, expireDate, record.getValidationCode(), record.isDNSSec() ? 1 : 0, record.getKeyTag(), record.getAlg(), record.getDigestType(), record.getDigest(), record.getDomainName());
      bEdited = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bEdited;
  }

  public boolean deleteRecord(String domainName)
  {
    boolean bDeleted = false;
    try (Handle h = dbHelper.connect())
    {
      h.execute(strDeleteAddress, domainName);
      bDeleted = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bDeleted;
  }

  public boolean deleteRecord(Domain record)
  {
    return deleteRecord(record.getDomainName());
  }

  public List<ListEntry> getListEntries()
  {
    List<ListEntry> listEntries = new ArrayList<ListEntry>();
    try (Handle h = dbHelper.connect())
    {
      h.createQuery(strGetListEntries).map((rs, ctx) -> new ListEntry(rs.getString(1))).list().forEach(listEntries::add);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return listEntries;
  }

  public Domain getDomain(String domainName)
  {
    Domain domain = null;
    try (Handle h = dbHelper.connect())
    {
      domain = h.createQuery(strGetAddress).bind(0, domainName).map((rs, ctx) -> {
        String[] oldAdmin = splitString(rs.getString("ADMIN"));
        String[] oldTech = splitString(rs.getString("TECH"));
        String[] oldNameServer = splitString(rs.getString("NAMESERVER"));

        Vector<Integer> oldStatusV = new Vector<>();
        String statusStr = rs.getString("STATUS");
        if (statusStr != null && statusStr.length() > 0)
        {
          for (String s : statusStr.split(","))
          {
            oldStatusV.add(Integer.parseInt(s));
          }
        }

        java.util.Date expireDate = null;
        java.sql.Date exp = rs.getDate("EXPIRE");
        if (exp != null)
        {
          expireDate = new java.util.Date(exp.getTime());
        }

        return new Domain(rs.getString("DOMAINNAME"), rs.getString("REGISTRANT"), oldAdmin, oldTech, oldNameServer, rs.getString("AUTHINFO"), oldStatusV, expireDate, rs.getString("VALIDATIONCODE"), rs.getInt("ISDNSSEC") == 1, rs.getString("DNSSECKEYTAG"), rs.getInt("DNSSECALG"), rs.getInt("DNSSECDIGESTTYPE"), rs.getString("DNSSECDIGEST"));
      }).findFirst().orElse(null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return domain;
  }

  private String[] splitString(String str)
  {
    if (str == null || str.isEmpty())
      return null;
    return str.split(";");
  }

  private String tableName = "domains";
  private final DbHelper dbHelper;
  private Handle handle;

  private final String strDropDomainTable;
  private final String strCreateAddressTable;
  private final String strGetAddress;
  private final String strSaveAddress;
  private final String strGetListEntries;
  private final String strUpdateAddress;
  private final String strDeleteAddress;

}
