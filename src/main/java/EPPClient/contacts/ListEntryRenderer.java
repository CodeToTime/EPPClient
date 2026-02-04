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

import javax.swing.*;
import java.awt.*;

public class ListEntryRenderer extends DefaultListCellRenderer
{

  /**
   * Creates a new instance of ListEntryRenderer
   */
  public ListEntryRenderer()
  {
  }

  public Component getListCellRendererComponent(JList list, Object value,
                                                int index, boolean isSelected, boolean cellHasFocus)
  {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    ListEntry entry = (ListEntry) value;
    this.setText(entry.getContactId() + " " + entry.getContactName());
    return this;
  }

}
