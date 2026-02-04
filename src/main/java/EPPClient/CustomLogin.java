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

import EPPClient.config.EPPparams;
import it.nic.epp.client.commands.session.Login;

public class CustomLogin extends Login {

    static {
        configureDNSSEC();
    }

    public CustomLogin() {
        super();
    }

    public CustomLogin(String clID, String pw) {
        super(clID, pw);
    }

    private static void configureDNSSEC() {
        String dnssecEnabled = EPPparams.getParameter("EppClient.implement.DNSSEC");

        boolean isDNSSECEnabled = Boolean.parseBoolean(dnssecEnabled);

        System.setProperty("EppClient.implement.secDNS", String.valueOf(isDNSSECEnabled));
        System.setProperty("EppClient.implement.extsecDNS", String.valueOf(isDNSSECEnabled));
    }
}