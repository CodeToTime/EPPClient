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

import EPPClient.db.contactsDao;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ContactSelection extends JDialog
        implements ActionListener, ListSelectionListener,
        PropertyChangeListener
{
  private String typedText = null;
  private JTextField textField;
  private JPanel dd;

  AddressListPanel addressListPanel = new AddressListPanel();

  private JOptionPane optionPane;

  private String btnString1 = "Enter";
  private String btnString2 = "Cancel";

  /**
   * Returns null if the typed string was invalid;
   * otherwise, returns the string as the user entered it.
   */
  public String getValidatedText()
  {
    return typedText;
  }

  public ContactSelection(Frame aFrame, JPanel parent, String domainName)
  {
    this(aFrame, parent, domainName, 0);
  }

  public ContactSelection(Frame aFrame, JPanel parent, String domainName, int contactType)
  {
    super(aFrame, true);
    dd = parent;

    setTitle("CONTACT selection");

    textField = new JTextField(10);

    String txtContactType = "";
    switch (contactType)
    {
      case 1:
        txtContactType = "Registrant-";
        break;
      case 2:
        txtContactType = "Admin-";
        break;
      case 3:
        txtContactType = "Tech-";
        break;
    }


    String msgString1 = "Please select the contact to be linked as " + txtContactType + "Contact to\ndomain name '" + domainName + "'";

//        Object[] array = {msgString1, msgString2, textField};

    db = new contactsDao();
    db.connect();
    List<ListEntry> entries;

    if (contactType == 1)
    {
      entries = db.getRegistrantEntries();
    }
    else
    {
      entries = db.getListEntries();
    }

    addressListPanel.addListEntries(entries);
    addressListPanel.addListSelectionListener(this);

    Object[] array = {msgString1, addressListPanel};

    //Create an array specifying the number of dialog buttons
    //and their text.
    Object[] options = {btnString1, btnString2};

    //Create the JOptionPane.
    optionPane = new JOptionPane(array,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION,
            null,
            options,
            options[0]);

    //Make this dialog display it.
    setContentPane(optionPane);

    //Handle window closing correctly.
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent we)
      {
        /*
         * Instead of directly closing the window,
         * we're going to change the JOptionPane's
         * value property.
         */
        optionPane.setValue(JOptionPane.CLOSED_OPTION);
      }
    });

    //Ensure the text field always gets the first focus.
    addComponentListener(new ComponentAdapter()
    {
      public void componentShown(ComponentEvent ce)
      {
        textField.requestFocusInWindow();
      }
    });

    //Register an event handler that puts the text into the option pane.
    textField.addActionListener(this);

    //Register an event handler that reacts to option pane state changes.
    optionPane.addPropertyChangeListener(this);
  }

  /**
   * This method handles events for the text field.
   */
  public void actionPerformed(ActionEvent e)
  {
    optionPane.setValue(btnString1);
  }

  /**
   * This method reacts to state changes in the option pane.
   */
  public void propertyChange(PropertyChangeEvent e)
  {
    String prop = e.getPropertyName();

    if (isVisible()
            && (e.getSource() == optionPane)
            && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
            JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
    {
      Object value = optionPane.getValue();

      if (value == JOptionPane.UNINITIALIZED_VALUE)
      {
        //ignore reset
        return;
      }
      //Reset the JOptionPane's value.
      //If you don't do this, then if the user
      //presses the same button next time, no
      //property change event will be fired.
      optionPane.setValue(
              JOptionPane.UNINITIALIZED_VALUE);

      if (btnString1.equals(value))
      {
        typedText = addressListPanel.getSelectedListEntry().getContactId();
        String ucText = typedText.toUpperCase();
        clearAndHide();

      }
      else
      { //user closed dialog or clicked cancel
//                dd.setLabel("It's OK.  "
//                         + "We won't force you to type "
//                         + magicWord + ".");
        typedText = null;
        clearAndHide();
      }
    }
  }


  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting())
    {
      return;
    }
    JList entryList = (JList) e.getSource();
    selectedEntry = entryList.getSelectedIndex();
    ListEntry entry = (ListEntry) entryList.getSelectedValue();
    if (entry != null)
    {
      String contactId = entry.getContactId();
      Address address = db.getAddress(contactId);
      //addressPanel.setAddress(address);
    }
    else
    {
      //addressPanel.clear();
    }
  }

  /**
   * This method clears the dialog and hides it.
   */
  public void clearAndHide()
  {
    textField.setText(null);
    setVisible(false);
  }

  private int selectedEntry = -1;
  private contactsDao db;
}

