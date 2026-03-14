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

package com.codetotime.eppclient.importer;

import com.codetotime.eppclient.main;
import com.codetotime.eppclient.uplink.EppUplink;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Frame for importing contacts or domains in bulk from a plain-text file. */
public class TxtImport extends JFrame {
  private static final Logger log = LoggerFactory.getLogger(TxtImport.class);

  private main mainFrame;

  /** Creates new form TxtImport. */
  public TxtImport(main mainFrame) {
    initComponents();

    this.mainFrame = mainFrame;
    this.eppUplink = mainFrame.eppUplink;
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    importFileChooser = new JFileChooser();
    logTextArea = new TextArea();

    // ======== this ========
    setTitle("Importazione Domini da file");
    setMinimumSize(new Dimension(600, 500));

    // ---- importFileChooser ----
    importFileChooser.addActionListener(e -> importFileChooserActionPerformed(e));

    // ---- logTextArea ----
    logTextArea.setEditable(false);
    logTextArea.setText(
        "Questa procedura permette di acquisire nel database locale eventuali domini già"
            + " presenti sul server EPP del Registro.\nPer poter procedere occorre predisporre"
            + " un file .txt contenente l'elenco dei domini, uno per riga.\n");

    Container contentPane = getContentPane();
    GroupLayout contentPaneLayout = new GroupLayout(contentPane);
    contentPane.setLayout(contentPaneLayout);
    contentPaneLayout.setHorizontalGroup(
        contentPaneLayout
            .createParallelGroup()
            .addGroup(
                GroupLayout.Alignment.TRAILING,
                contentPaneLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        contentPaneLayout
                            .createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(
                                logTextArea,
                                GroupLayout.Alignment.LEADING,
                                GroupLayout.DEFAULT_SIZE,
                                723,
                                Short.MAX_VALUE)
                            .addComponent(
                                importFileChooser,
                                GroupLayout.Alignment.LEADING,
                                GroupLayout.DEFAULT_SIZE,
                                723,
                                Short.MAX_VALUE))
                    .addContainerGap()));
    contentPaneLayout.setVerticalGroup(
        contentPaneLayout
            .createParallelGroup()
            .addGroup(
                GroupLayout.Alignment.TRAILING,
                contentPaneLayout
                    .createSequentialGroup()
                    .addComponent(
                        logTextArea,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addGap(20, 20, 20)
                    .addComponent(importFileChooser, GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addContainerGap()));
    pack();
    setLocationRelativeTo(getOwner());
  } // </editor-fold>//GEN-END:initComponents

  private void importFileChooserActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jFileChooser1ActionPerformed
    if (evt.getActionCommand().equals("ApproveSelection")) {
      if (importFileChooser.getSelectedFile() != null) {
        File file = importFileChooser.getSelectedFile();

        try {
          BufferedReader in = new BufferedReader(new FileReader(file));
          String str;
          logTextArea.setText(logTextArea.getText() + "\n======LOG IMPORTAZIONE======");
          ImportDomain domainImporter = new ImportDomain(mainFrame, true);
          while ((str = in.readLine()) != null) {
            logTextArea.setText(logTextArea.getText() + "\n" + str);
            if (domainImporter.execute(str)) {
              logTextArea.setText(logTextArea.getText() + " IMPORTATO;");
            } else {
              logTextArea.setText(logTextArea.getText() + " NON IMPORTATO;");
            }
          }
          logTextArea.setText(logTextArea.getText() + "\n======FINE IMPORTAZIONE======");
          in.close();
        } catch (FileNotFoundException e) {
          logTextArea.setText(logTextArea.getText() + "\n++++++Impossibile aprire il file++++++");
        } catch (IOException e) {
          logTextArea.setText(
              logTextArea.getText()
                  + "\n++++++Errore sconosciuto durante l'apertura del file++++++");
        }

      } else {
        log.info("TxtImport: non è stato selezionato alcun file...");
      }
    } else {
      this.setVisible(false);
    }
  } // GEN-LAST:event_jFileChooser1ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JFileChooser importFileChooser;
  private TextArea logTextArea;

  // End of variables declaration//GEN-END:variables

  /**
   * Enables or disables the file chooser based on whether the EPP connection is active.
   *
   * @param eppStatus {@code true} if the EPP connection is active
   */
  public void setEppEnablement(boolean eppStatus) {
    importFileChooser.setEnabled(eppStatus);
  }

  private EppUplink eppUplink;
}
