/*
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

package EPPClient;

import EPPClient.BuildInfo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class InfoDialog {

    public static void show(Frame parent) {

        JDialog dialog = new JDialog(parent,
                "EPP Client - Informazioni compilazione", true);
        dialog.setSize(560, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(buildText());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new LineBorder(Color.LIGHT_GRAY));

        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);

        dialog.setLayout(new BorderLayout(6, 6));
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private static String buildText() {
        return
                "INFORMAZIONI DI COMPILAZIONE\n" +
                        "----------------------------------------\n" +

                        "Compilato su richiesta di: " + BuildInfo.BUILD_COMPANY +"\n\n" +

                        "Partita IVA: " + BuildInfo.BUILD_PIVA +"\n\n" +

                        "ID interno ordine: " + BuildInfo.BUILD_ORDER + "\n\n" +

                        "TAG Registrar (NIC): " + BuildInfo.BUILD_REGISTRAR_TAG + "\n\n" +

                        "Data di compilazione: " + BuildInfo.BUILD_DATE + "\n" +

                        "----------------------------------------\n" +
                        "L’azienda ha certificato quanto segue:\n\n" +

                        "1) Di essere un Registrar accreditato presso il Registro .it (NIC.it).\n" +
                        "2) Di essere autorizzata all’utilizzo della libreria EPP del Registro .it.\n" +
                        "3) Di aver verificato il codice del software EPP Client e di accettarlo nello stato in cui viene fornito (\"AS IS\").\n" +

                        "----------------------------------------\n" +
                        "Le dichiarazioni sopra riportate sono rese sotto la piena responsabilità del richiedente.\n\n" +

                        "Il software è fornito senza garanzie di alcun tipo.";
    }
}
