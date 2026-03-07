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

import EPPClient.messages.Message;

import java.util.Vector;
import org.jdbi.v3.core.Handle;

public class messagesDao
{

  /**
   * Creates a new instance of AddressDao
   */
  public messagesDao()
  {
    this("messages");
  }

  public messagesDao(String addressBookName)
  {
    strDropMessageTable = "drop table " + tableName;

    strCreateMessageTable =
            "create table " + tableName + " (" +
                    "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    DATETIME              TIMESTAMP, " +
                    "    MESSAGE               VARCHAR(255), " +
                    "    MSGXML                CLOB, " +
                    "    READFLAG              SMALLINT, " +
                    "    ACKFLAG               SMALLINT, " +
                    "    ACTIONEDFLAG          SMALLINT" +
                    ")";

    strCreateMessageTableMYSQL =
            "create table " + tableName + " (" +
                    "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    DATETIME              TIMESTAMP DEFAULT '1970-01-01 01:00:01', " +
                    "    MESSAGE               VARCHAR(255), " +
                    "    MSGXML                TEXT, " +
                    "    READFLAG              SMALLINT, " +
                    "    ACKFLAG               SMALLINT, " +
                    "    ACTIONEDFLAG          SMALLINT" +
                    ")";

    strGetMessage =
            "SELECT * FROM " + tableName + " " +
                    "WHERE MSGID = ?";

    strSaveMessage =
            "INSERT INTO " + tableName + " " +
                    "   (MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    strGetListEntries =
            "SELECT MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG FROM " + tableName + " " +
                    "ORDER BY DATETIME ASC, MSGID ASC";

    strUpdateMessage =
            "UPDATE " + tableName + " " +
                    "SET READFLAG = ?, " +
                    "    ACKFLAG = ?, " +
                    "    ACTIONEDFLAG = ? " +
                    "WHERE MSGID = ?";

    strDeleteMessage =
            "DELETE FROM " + tableName + " " +
                    "WHERE MSGID = ?";

    // Initialize DbHelper
    if (!DbHelper.isInitialized())
    {
      DbHelper.initialize();
    }
    this.dbHelper = DbHelper.getInstance();

    // Try to create tables if they don't exist
    if (!dbHelper.tableExists(tableName))
    {
      // Check for MySQL/MariaDB to use appropriate table definition
      if (dbHelper.isMysqlOrMariaDb())
      {
        dbHelper.executeSql(strCreateMessageTableMYSQL);
      }
      else
      {
        dbHelper.executeSql(strCreateMessageTable);
      }
    }
  }

  public boolean dropTables()
  {
    dbHelper.executeSql(strDropMessageTable);
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

  public String saveRecord(Message record)
  {
    try (Handle h = dbHelper.connect())
    {
      java.sql.Timestamp timestamp = null;
      if (record.getDateTime() != null)
      {
        timestamp = new java.sql.Timestamp(record.getDateTime().getTime());
      }

      h.execute(strSaveMessage, record.getMsgId(), timestamp, record.getTitle(), record.getXml(), record.getRead() ? 1 : 0, record.getAck() ? 1 : 0, record.getActioned() ? 1 : 0);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return record.getMsgId();
  }

  public boolean editRecord(Message record)
  {
    boolean bEdited = false;
    try (Handle h = dbHelper.connect())
    {
      h.execute(strUpdateMessage, record.getRead() ? 1 : 0, record.getAck() ? 1 : 0, record.getActioned() ? 1 : 0, record.getMsgId());
      bEdited = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bEdited;
  }

  public boolean deleteRecord(String msgId)
  {
    boolean bDeleted = false;
    try (Handle h = dbHelper.connect())
    {
      h.execute(strDeleteMessage, msgId);
      bDeleted = true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return bDeleted;
  }

  public boolean deleteRecord(Message record)
  {
    return deleteRecord(record.getMsgId());
  }

  public Vector getListEntries()
  {
    return getSelectiveEntries(strGetListEntries);
  }

  public Vector getSelectiveEntries(String preparedStatement)
  {
    Vector listEntries = new Vector();
    try (Handle h = dbHelper.connect())
    {
      h.createQuery(strGetListEntries).map((rs, ctx) -> new Message(
              rs.getString("MSGID"),
              rs.getTimestamp("DATETIME"),
              rs.getString("MESSAGE"),
              rs.getString("MSGXML"),
              rs.getInt("READFLAG") == 1,
              rs.getInt("ACKFLAG") == 1,
              rs.getInt("ACTIONEDFLAG") == 1
      )).list().forEach(listEntries::add);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return listEntries;
  }

  public Message getMessage(String msgId)
  {
    Message message = null;
    try (Handle h = dbHelper.connect())
    {
      message = h.createQuery(strGetMessage).bind(0, msgId).map((rs, ctx) -> {
        java.util.Date datetime = rs.getTimestamp("DATETIME");
        String title = rs.getString("MESSAGE");
        String xml = rs.getString("MSGXML");
        boolean read = rs.getInt("READFLAG") == 1;
        boolean ack = rs.getInt("ACKFLAG") == 1;
        boolean actioned = rs.getInt("ACTIONEDFLAG") == 1;
        return new Message(msgId, datetime, title, xml, read, ack, actioned);
      }).findFirst().orElse(null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return message;
  }

  private String tableName = "messages";
  private final DbHelper dbHelper;
  private Handle handle;

  private final String strDropMessageTable;
  private final String strCreateMessageTable;
  private final String strCreateMessageTableMYSQL;
  private final String strGetMessage;
  private final String strSaveMessage;
  private final String strGetListEntries;
  private final String strUpdateMessage;
  private final String strDeleteMessage;
}
