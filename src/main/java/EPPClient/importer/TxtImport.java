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

package EPPClient.importer;

import EPPClient.main;
import EPPClient.uplink.EPPuplink;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TxtImport extends JFrame
{

  private main mainFrame;

  /**
   * Creates new form TxtImport
   */
  public TxtImport(main mainFrame)
  {
    initComponents();

    this.mainFrame = mainFrame;
    this.EPPuplink = mainFrame.EPPuplink;
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    jFileChooser1 = new JFileChooser();
    textArea1 = new TextArea();

    //======== this ========
    setTitle("Importazione Domini da file");
    setMinimumSize(new Dimension(600, 500));
    Container contentPane = getContentPane();

    //---- jFileChooser1 ----
    jFileChooser1.addActionListener(e -> jFileChooser1ActionPerformed(e));

    //---- textArea1 ----
    textArea1.setEditable(false);
    textArea1.setText("Questa procedura permette di acquisire nel database locale eventuali domini gi\u00e0 presenti sul server EPP del Registro.\nPer poter procedere occorre predisporre un file .txt contenente l'elenco dei domini, uno per riga.\n");

    GroupLayout contentPaneLayout = new GroupLayout(contentPane);
    contentPane.setLayout(contentPaneLayout);
    contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(textArea1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                                    .addComponent(jFileChooser1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE))
                            .addContainerGap())
    );
    contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addComponent(textArea1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(jFileChooser1, GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                            .addContainerGap())
    );
    pack();
    setLocationRelativeTo(getOwner());
  }// </editor-fold>//GEN-END:initComponents

  private void jFileChooser1ActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_jFileChooser1ActionPerformed
    if (evt.getActionCommand().equals("ApproveSelection"))
    {
      if (jFileChooser1.getSelectedFile() != null)
      {
        File file = jFileChooser1.getSelectedFile();

        try
        {
          BufferedReader in = new BufferedReader(new FileReader(file));
          String str;
          textArea1.setText(textArea1.getText() + "\n======LOG IMPORTAZIONE======");
          ImportDomain domainImporter = new ImportDomain(mainFrame, true);
          while ((str = in.readLine()) != null)
          {
            textArea1.setText(textArea1.getText() + "\n" + str);
            if (domainImporter.execute(str))
            {
              textArea1.setText(textArea1.getText() + " IMPORTATO;");
            }
            else
            {
              textArea1.setText(textArea1.getText() + " NON IMPORTATO;");
            }
          }
          textArea1.setText(textArea1.getText() + "\n======FINE IMPORTAZIONE======");
          in.close();
        }
        catch (FileNotFoundException e)
        {
          textArea1.setText(textArea1.getText() + "\n++++++Impossibile aprire il file++++++");
        }
        catch (IOException e)
        {
          textArea1.setText(textArea1.getText() + "\n++++++Errore sconosciuto durante l'apertura del file++++++");
        }


      }
      else
      {
        System.out.println("non è stato selezionato alcun file...");
      }
    }
    else
    {
      this.setVisible(false);
    }
  }//GEN-LAST:event_jFileChooser1ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JFileChooser jFileChooser1;
  private TextArea textArea1;
  // End of variables declaration//GEN-END:variables

  public void setEPPEnablement(boolean EPPstatus)
  {
    jFileChooser1.setEnabled(EPPstatus);
  }

  private EPPuplink EPPuplink;
}
