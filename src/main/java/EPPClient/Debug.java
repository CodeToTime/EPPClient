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

package EPPClient;

/**
 * Helper per la gestione dei messaggi di debug.
 * Utilizzare con il flag -Deppclient.debug=true per abilitare i messaggi.
 */
public class Debug {

    private static final String DEBUG_PROPERTY = "eppclient.debug";

    /**
     * Verifica se il debug è abilitato.
     * @return true se il flag -Deppclient.debug=true
     */
    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY, "false"));
    }

    /**
     * Stampa un messaggio di debug solo se il debug è abilitato.
     * @param message il messaggio da stampare
     */
    public static void log(String message) {
        if (isEnabled()) {
            System.out.println(message);
        }
    }

    /**
     * Stampa un messaggio di debug con un tag sorgente solo se il debug è abilitato.
     * @param source la sorgente del messaggio
     * @param message il messaggio da stampare
     */
    public static void log(String source, String message) {
        if (isEnabled()) {
            System.out.println("[" + source + "] " + message);
        }
    }

    /**
     * Stampa un messaggio di errore di debug solo se il debug è abilitato.
     * @param message il messaggio di errore da stampare
     */
    public static void error(String message) {
        if (isEnabled()) {
            System.err.println(message);
        }
    }

    /**
     * Stampa un messaggio di errore di debug con un tag sorgente solo se il debug è abilitato.
     * @param source la sorgente del messaggio
     * @param message il messaggio di errore da stampare
     */
    public static void error(String source, String message) {
        if (isEnabled()) {
            System.err.println("[" + source + "] " + message);
        }
    }

    /**
     * Stampa lo stack trace di un'eccezione solo se il debug è abilitato.
     * @param e l'eccezione di cui stampare lo stack trace
     */
    public static void printStackTrace(Exception e) {
        if (isEnabled()) {
            e.printStackTrace();
        }
    }
}
