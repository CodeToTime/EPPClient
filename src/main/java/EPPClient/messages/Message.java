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

package EPPClient.messages;

import java.util.Date;

public class Message
{

  public Message()
  {
  }

  public Message(String msgId, Date datetime, String title, String xml, boolean read, boolean ack, boolean actioned)
  {

    this.msgId = msgId;
    this.datetime = datetime;
    this.title = title;
    this.xml = xml;
    this.read = read;
    this.ack = ack;
    this.actioned = actioned;
  }

  public void setMsgId(String msgId)
  {
    this.msgId = msgId;
  }

  public String getMsgId()
  {
    return this.msgId;
  }

  public void setDateTime(Date datetime)
  {
    this.datetime = datetime;
  }

  public Date getDateTime()
  {
    return this.datetime;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setXml(String xml)
  {
    this.xml = xml;
  }

  public String getXml()
  {
    return this.xml;
  }

  public void setRead(boolean read)
  {
    this.read = read;
  }

  public boolean getRead()
  {
    return this.read;
  }

  public void setAck(boolean ack)
  {
    this.ack = ack;
  }

  public boolean getAck()
  {
    return this.ack;
  }

  public void setActioned(boolean actioned)
  {
    this.actioned = actioned;
  }

  public boolean getActioned()
  {
    return this.actioned;
  }

  private String msgId;
  private Date datetime;
  private String title;
  private String xml;
  private boolean read;
  private boolean ack;
  private boolean actioned;

  private static final int PRIMENO = 37;

}
