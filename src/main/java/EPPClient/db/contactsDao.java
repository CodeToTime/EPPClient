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

package EPPClient.db;

import EPPClient.config.EPPparams;
import EPPClient.contacts.Address;
import EPPClient.contacts.ListEntry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jdbi.v3.core.Handle;

public class contactsDao
{

  /**
   * Creates a new instance of AddressDao
   */
  public contactsDao()
  {
    this("contacts");
  }

  public contactsDao(String addressBookName)
  {
    strDropContactTable = "drop table " + tableName;

    strCreateContactTable =
            "create table " + tableName + " (" +
                    "    CONTACTID             VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    NAME                  VARCHAR(255), " +
                    "    ORG                   VARCHAR(255), " +
                    "    STREET                VARCHAR(255), " +
                    "    CITY                  VARCHAR(255), " +
                    "    STATEORPROVINCE       VARCHAR(255), " +
                    "    POSTALCODE            VARCHAR(10 ), " +
                    "    COUNTRYCODE           VARCHAR(2  ), " +
                    "    VOICE                 VARCHAR(255), " +
                    "    FAX                   VARCHAR(255), " +
                    "    EMAIL                 VARCHAR(255), " +
                    "    CONSENTFORPUBLISHING  SMALLINT    , " +
                    "    ISREGISTRANT          SMALLINT    , " +
                    "    NATIONALITYCODE       VARCHAR(2  ), " +
                    "    ENTITYTYPE            INT         , " +
                    "    REGCODE               VARCHAR(16 ), " +
                    "    STATUS                VARCHAR(255), " +
                    "    SCHOOLCODE            VARCHAR(64 ), " +
                    "    UOCODE                VARCHAR(64 ), " +
                    "    IPACODE               VARCHAR(64 )  " +
                    ")";

    strGetContact =
            "SELECT * FROM " + tableName + " " +
                    "WHERE CONTACTID = ?";

    strSaveContact =
            "INSERT INTO " + tableName + " " +
                    "   (CONTACTID, NAME, ORG, STREET, CITY, STATEORPROVINCE, POSTALCODE, " +
                    "    COUNTRYCODE, VOICE, FAX, EMAIL, CONSENTFORPUBLISHING, ISREGISTRANT, " +
                    "    NATIONALITYCODE, ENTITYTYPE, REGCODE, STATUS, SCHOOLCODE, UOCODE, IPACODE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    strGetListEntries =
            "SELECT CONTACTID, NAME FROM " + tableName + " " +
                    "ORDER BY NAME ASC";

    strGetRegistrantEntries =
            "SELECT CONTACTID, NAME FROM " + tableName + " " +
                    "WHERE ISREGISTRANT = 1 ORDER BY NAME ASC";

    strUpdateContact =
            "UPDATE " + tableName + " " +
                    "SET NAME = ?, " +
                    "    ORG = ?, " +
                    "    STREET = ?, " +
                    "    CITY = ?, " +
                    "    STATEORPROVINCE = ?, " +
                    "    POSTALCODE = ?, " +
                    "    COUNTRYCODE = ?, " +
                    "    VOICE = ?, " +
                    "    FAX = ?, " +
                    "    EMAIL = ?, " +
                    "    CONSENTFORPUBLISHING = ? ," +
                    "    ISREGISTRANT = ? ," +
                    "    NATIONALITYCODE = ? ," +
                    "    ENTITYTYPE = ? ," +
                    "    REGCODE = ? ," +
                    "    STATUS = ? ," +
                    "    SCHOOLCODE = ?, " +
                    "    UOCODE = ?, " +
                    "    IPACODE = ? " +
                    "WHERE CONTACTID = ?";

    strDeleteContact =
            "DELETE FROM " + tableName + " " +
                    "WHERE CONTACTID = ?";

    // Initialize DbHelper
    if (!DbHelper.isInitialized())
    {
      DbHelper.initialize();
    }
    this.dbHelper = DbHelper.getInstance();

    // Try to create tables if they don't exist, otherwise add missing columns
    if (!dbHelper.tableExists(tableName))
    {
      dbHelper.executeSql(strCreateContactTable);
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
        addColumnIfNotExists(dbConnection, tableName, "SCHOOLCODE", "VARCHAR(64)");
        addColumnIfNotExists(dbConnection, tableName, "UOCODE", "VARCHAR(64)");
        addColumnIfNotExists(dbConnection, tableName, "IPACODE", "VARCHAR(64)");
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
    dbHelper.executeSql(strDropContactTable);
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

  public String saveRecord(Address record)
  {
    try (Handle h = dbHelper.connect())
    {
      h.execute(strSaveContact, record.getContactId(), record.getContactName(), record.getOrg(), record.getStreet(), record.getCity(), record.getStateOrProvince(), record.getPostalCode(), record.getCountryCode(), record.getVoice(), record.getFax(), record.getEmail(), record.getConsentForPublishing() ? 1 : 0, record.getIsRegistrant() ? 1 : 0, record.getNationalityCode(), record.getEntityType(), record.getRegCode(), "", record.getSchoolCode(), record.getUoCode(), record.getIpaCode());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return record.getContactId();
  }

  public boolean editRecord(Address record)
  {
    boolean bEdited = false;
    try (Handle h = dbHelper.connect())
    {
      h.execute(strUpdateContact, record.getContactName(), record.getOrg(), record.getStreet(), record.getCity(), record.getStateOrProvince(), record.getPostalCode(), record.getCountryCode(), record.getVoice(), record.getFax(), record.getEmail(), record.getConsentForPublishing() ? 1 : 0, record.getIsRegistrant() ? 1 : 0, record.getNationalityCode(), record.getEntityType(), record.getRegCode(), "", record.getSchoolCode(), record.getUoCode(), record.getIpaCode(), record.getContactId());
      bEdited = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bEdited;
  }

  public boolean deleteRecord(String contactID)
  {
    boolean bDeleted = false;
    try (Handle h = dbHelper.connect())
    {
      h.execute(strDeleteContact, contactID);
      bDeleted = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bDeleted;
  }

  public boolean deleteRecord(Address record)
  {
    return deleteRecord(record.getContactId());
  }

  public List<ListEntry> getRegistrantEntries()
  {
    return getSelectiveEntries(strGetRegistrantEntries);
  }

  public List<ListEntry> getListEntries()
  {
    return getSelectiveEntries(strGetListEntries);
  }

  public List<ListEntry> getSelectiveEntries(String sql)
  {
    List<ListEntry> listEntries = new ArrayList<ListEntry>();
    try (Handle h = dbHelper.connect())
    {
      h.createQuery(sql).map((rs, ctx) -> new ListEntry(rs.getString(2), rs.getString(1))).list().forEach(listEntries::add);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return listEntries;
  }

  public Address getAddress(String contactId)
  {
    Address address = null;
    try (Handle h = dbHelper.connect())
    {
      address = h.createQuery(strGetContact).bind(0, contactId).map((rs, ctx) -> {
        int[] oldStatus = null;
        String statusStr = rs.getString("STATUS");
        if (statusStr != null && statusStr.length() > 0)
        {
          String[] oldStatuses = statusStr.split(",");
          oldStatus = new int[oldStatuses.length];
          for (int i = 0; i < oldStatuses.length; i++)
          {
            oldStatus[i] = Integer.valueOf(oldStatuses[i]);
          }
        }
        return new Address(rs.getString("NAME"), rs.getString("ORG"), rs.getString("STREET"), rs.getString("CITY"), rs.getString("STATEORPROVINCE"), rs.getString("POSTALCODE"), rs.getString("COUNTRYCODE"), rs.getString("VOICE"), rs.getString("FAX"), rs.getString("EMAIL"), rs.getInt("CONSENTFORPUBLISHING") == 1, rs.getInt("ISREGISTRANT") == 1, rs.getString("NATIONALITYCODE"), rs.getInt("ENTITYTYPE"), rs.getString("REGCODE"), oldStatus, rs.getString("CONTACTID"), rs.getString("SCHOOLCODE"), rs.getString("IPACODE"), rs.getString("UOCODE"));
      }).findFirst().orElse(null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return address;
  }

  private String tableName = "contacts";
  private final DbHelper dbHelper;
  private Handle handle;

  private final String strDropContactTable;
  private final String strCreateContactTable;
  private final String strGetContact;
  private final String strSaveContact;
  private final String strGetListEntries;
  private final String strGetRegistrantEntries;
  private final String strUpdateContact;
  private final String strDeleteContact;

}
