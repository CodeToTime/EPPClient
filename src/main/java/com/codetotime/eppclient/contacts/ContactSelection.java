/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of EPPClient.
 *
 * EPPClient is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EPPClient is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EPPClient.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.codetotime.eppclient.contacts;

import com.codetotime.eppclient.db.ContactsDao;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Dialog for selecting a contact from the local database to assign to a domain field (registrant,
 * admin, or tech contact).
 */
public class ContactSelection extends JDialog
    implements ActionListener, ListSelectionListener, PropertyChangeListener {
  private String typedText = null;
  private final JTextField textField;
  private final JPanel parentPanel;

  AddressListPanel addressListPanel = new AddressListPanel();

  private JOptionPane optionPane;

  private final String btnEnter = "Enter";
  private final String btnCancel = "Cancel";

  /**
   * Returns null if the typed string was invalid; otherwise, returns the string as the user entered
   * it.
   */
  public String getValidatedText() {
    return typedText;
  }

  /**
   * Creates a contact selection dialog for the given domain, defaulting to contact type 0.
   *
   * @param frame the parent frame
   * @param parent the panel that will receive the selected contact ID
   * @param domainName the domain name context for the selection
   */
  public ContactSelection(Frame frame, JPanel parent, String domainName) {
    this(frame, parent, domainName, 0);
  }

  /**
   * Creates a contact selection dialog for the given domain and contact type.
   *
   * @param frame the parent frame
   * @param parent the panel that will receive the selected contact ID
   * @param domainName the domain name context for the selection
   * @param contactType the contact role: 0 = registrant, 1 = admin, 2 = tech
   */
  public ContactSelection(Frame frame, JPanel parent, String domainName, int contactType) {
    super(frame, true);
    this.parentPanel = parent;

    setTitle("CONTACT selection");

    textField = new JTextField(10);

    String txtContactType = "";
    switch (contactType) {
      case 1:
        txtContactType = "Registrant-";
        break;
      case 2:
        txtContactType = "Admin-";
        break;
      case 3:
        txtContactType = "Tech-";
        break;
      default:
        break;
    }

    contactsDb = new ContactsDao();
    contactsDb.connect();
    List<ListEntry> entries;

    if (contactType == 1) {
      entries = contactsDb.getRegistrantEntries();
    } else {
      entries = contactsDb.getListEntries();
    }

    addressListPanel.addListEntries(entries);
    addressListPanel.addListSelectionListener(this);

    String msgString1 =
        "Please select the contact to be linked as "
            + txtContactType
            + "Contact to\ndomain name '"
            + domainName
            + "'";

    Object[] array = {msgString1, addressListPanel};

    // Create an array specifying the number of dialog buttons
    // and their text.
    Object[] options = {btnEnter, btnCancel};

    // Create the JOptionPane.
    optionPane =
        new JOptionPane(
            array,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION,
            null,
            options,
            options[0]);

    // Make this dialog display it.
    setContentPane(optionPane);

    // Handle window closing correctly.
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
            /*
             * Instead of directly closing the window,
             * we're going to change the JOptionPane's
             * value property.
             */
            optionPane.setValue(JOptionPane.CLOSED_OPTION);
          }
        });

    // Ensure the text field always gets the first focus.
    addComponentListener(
        new ComponentAdapter() {
          public void componentShown(ComponentEvent ce) {
            textField.requestFocusInWindow();
          }
        });

    // Register an event handler that puts the text into the option pane.
    textField.addActionListener(this);

    // Register an event handler that reacts to option pane state changes.
    optionPane.addPropertyChangeListener(this);
  }

  /** This method handles events for the text field. */
  public void actionPerformed(ActionEvent e) {
    optionPane.setValue(btnEnter);
  }

  /** This method reacts to state changes in the option pane. */
  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();

    if (isVisible()
        && (e.getSource() == optionPane)
        && (JOptionPane.VALUE_PROPERTY.equals(prop)
            || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
      Object value = optionPane.getValue();

      if (value == JOptionPane.UNINITIALIZED_VALUE) {
        // ignore reset
        return;
      }
      // Reset the JOptionPane's value.
      // If you don't do this, then if the user
      // presses the same button next time, no
      // property change event will be fired.
      optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

      if (btnEnter.equals(value)) {
        typedText = addressListPanel.getSelectedListEntry().getContactId();
        clearAndHide();

      } else { // user closed dialog or clicked cancel
        //                parentPanel.setLabel("It's OK.  "
        //                         + "We won't force you to type "
        //                         + magicWord + ".");
        typedText = null;
        clearAndHide();
      }
    }
  }

  /** {@inheritDoc} */
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }
    JList<?> entryList = (JList<?>) e.getSource();
    selectedEntry = entryList.getSelectedIndex();
    ListEntry entry = (ListEntry) entryList.getSelectedValue();
    if (entry != null) {
      String contactId = entry.getContactId();
      // Address address = contactsDb.getAddress(contactId);
      // addressPanel.setAddress(address);
    }
  }

  /** This method clears the dialog and hides it. */
  public void clearAndHide() {
    textField.setText(null);
    setVisible(false);
  }

  private int selectedEntry = -1;
  private ContactsDao contactsDb;
}
