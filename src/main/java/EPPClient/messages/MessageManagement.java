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

import EPPClient.db.messagesDao;
import EPPClient.main;
import EPPClient.uplink.EPPuplink;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ListIterator;
import java.util.Vector;

public class MessageManagement extends JFrame
{

  java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  Vector messageDetailWindows = new Vector();
  private main mainFrame;

  /**
   * Creates new form MessageManagement
   */
  public MessageManagement(main mainFrame)
  {
    initComponents();
    this.mainFrame = mainFrame;
    this.EPPuplink = mainFrame.EPPuplink;
    this.db = mainFrame.messagesDao;

    messagesTable.addMouseListener(new java.awt.event.MouseAdapter()
    {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          messagesTableMouseDoubleClicked(e);
        }
      }
    });

    model.addColumn("msgId");
    model.addColumn("date/time");
    model.addColumn("title");
    model.addColumn("read");
    model.addColumn("ack");

    messagesTable.getColumnModel().getColumn(0).setMinWidth(50);
    messagesTable.getColumnModel().getColumn(0).setMaxWidth(50);
    messagesTable.getColumnModel().getColumn(1).setMinWidth(120);
    messagesTable.getColumnModel().getColumn(1).setMaxWidth(120);
    messagesTable.getColumnModel().getColumn(3).setMinWidth(50);
    messagesTable.getColumnModel().getColumn(3).setMaxWidth(50);
    messagesTable.getColumnModel().getColumn(4).setMinWidth(50);
    messagesTable.getColumnModel().getColumn(4).setMaxWidth(50);

  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    jScrollPane1 = new JScrollPane();
    messagesTable = new JTable();
    refresh = new JButton();
    refresh1 = new JButton();

    //======== this ========
    setTitle("Gestione MESSAGGI");
    Container contentPane = getContentPane();

    //======== jScrollPane1 ========
    {

      //---- messagesTable ----
      messagesTable.setModel(model);
      jScrollPane1.setViewportView(messagesTable);
    }

    //---- refresh ----
    refresh.setText("refresh list");
    refresh.addActionListener(e -> refreshActionPerformed(e));

    //---- refresh1 ----
    refresh1.setText("ACK ALL");
    refresh1.addActionListener(e -> ACKALLActionPerformed(e));

    GroupLayout contentPaneLayout = new GroupLayout(contentPane);
    contentPane.setLayout(contentPaneLayout);
    contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                    .addGroup(contentPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                                    .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                            .addComponent(refresh1)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(refresh)))
                            .addContainerGap())
    );
    contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(refresh)
                                    .addComponent(refresh1))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                            .addContainerGap())
    );
    pack();
    setLocationRelativeTo(getOwner());
  }// </editor-fold>//GEN-END:initComponents

  public void setEPPEnablement(boolean EPPstatus)
  {
    this.EPPstatus = EPPstatus;
    for (int i = 0; i < messageDetailWindows.size(); i++)
    {
      ((MessageDetail) messageDetailWindows.get(i)).setEPPEnablement(EPPstatus);
    }
  }

  private void refreshActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_refreshActionPerformed
    updateTableContent();
  }//GEN-LAST:event_refreshActionPerformed

  private void ACKALLActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_ACKALLActionPerformed
    ackAllMessages();
  }//GEN-LAST:event_ACKALLActionPerformed

  @Override
  public void setVisible(boolean b)
  {
    super.setVisible(b);
    if (b)
      updateTableContent();
  }

  private void ackAllMessages()
  {
    try
    {
      Boolean terminateMassAck = false;
      Integer imsg = 1;
      while (!terminateMassAck)
      {

        refresh1.setText("ACK ALL " + imsg);

        if (!EPPuplink.pollMsg())
        {
          terminateMassAck = true;
        }
      }
      refresh1.setText("ACK ALL");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

  }

  private void messagesTableMouseDoubleClicked(java.awt.event.MouseEvent evt)
  {
    boolean detailsOpened = false;
    if (messagesTable.getSelectedRow() >= 0)
    {
      for (int i = 0; i < messageDetailWindows.size(); i++)
      {
        if (((MessageDetail) messageDetailWindows.get(i)).getMsgId().equals(messagesTable.getValueAt(messagesTable.getSelectedRow(), 0)))
        {
          ((MessageDetail) messageDetailWindows.get(i)).setVisible(true);
          detailsOpened = true;
          if (i > 10)
            break;
        }
      }
      if (!detailsOpened)
      {
        Message selectedMessage = db.getMessage((String) messagesTable.getValueAt(messagesTable.getSelectedRow(), 0));
        MessageDetail messageDetail = new MessageDetail(mainFrame, this, selectedMessage);
        messageDetail.setEPPEnablement(EPPstatus);
        messageDetail.setVisible(true);
        messageDetailWindows.add(messageDetail);
      }
    }
  }

  public void updateTableContent()
  {
    while (model.getRowCount() > 0)
    {
      model.removeRow(0);
    }
    Vector entries = db.getListEntries();
    ListIterator iter = entries.listIterator();
    while (iter.hasNext())
    {
      Message message = (Message) iter.next();
      addMessage(message);
    }
  }

  public void addMessage(Message message)
  {
    model.insertRow(0, new Object[]{message.getMsgId(), dateFormatter.format(message.getDateTime()), message.getTitle(), message.getRead(), message.getAck()});
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JScrollPane jScrollPane1;
  private JTable messagesTable;
  private JButton refresh;
  private JButton refresh1;
  // End of variables declaration//GEN-END:variables
  private EPPuplink EPPuplink;
  //DefaultTableModel model = new DefaultTableModel();
  NotEditableTableModel model = new NotEditableTableModel();
  private messagesDao db;
  private boolean EPPstatus;
}


class NotEditableTableModel extends DefaultTableModel
{
  @Override
  public boolean isCellEditable(int column, int row)
  {
    return false;
  }
}