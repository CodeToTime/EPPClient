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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateChecker {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/CodeToTime/EPPClient/releases";

    public static void checkForUpdates(Component parent, String currentVersion) {
        // Non controllare se è una versione SNAPSHOT
        if (currentVersion.contains("SNAPSHOT")) {
            return;
        }

        new Thread(() -> {
            try {
                List<ReleaseInfo> newerReleases = fetchNewerReleases(currentVersion);
                if (!newerReleases.isEmpty()) {
                    SwingUtilities.invokeLater(() -> showUpdateDialog(parent, currentVersion, newerReleases));
                }
            } catch (Exception e) {
                // Silenziosamente ignora errori di rete
                System.err.println("Errore durante il controllo aggiornamenti: " + e.getMessage());
            }
        }).start();
    }

    private static List<ReleaseInfo> fetchNewerReleases(String currentVersion) throws Exception {
        List<ReleaseInfo> newerReleases = new ArrayList<>();

        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() != 200) {
            return newerReleases;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JsonArray releases = JsonParser.parseString(response.toString()).getAsJsonArray();

        for (JsonElement element : releases) {
            JsonObject release = element.getAsJsonObject();
            
            // Salta prerelease e draft
            if (release.get("prerelease").getAsBoolean() || release.get("draft").getAsBoolean()) {
                continue;
            }

            String tagName = release.get("tag_name").getAsString();
            String releaseName = release.has("name") && !release.get("name").isJsonNull() 
                    ? release.get("name").getAsString() : tagName;
            String body = release.has("body") && !release.get("body").isJsonNull() 
                    ? release.get("body").getAsString() : "";

            if (isNewerVersion(tagName, currentVersion)) {
                newerReleases.add(new ReleaseInfo(tagName, releaseName, body));
            }
        }

        return newerReleases;
    }

    private static boolean isNewerVersion(String remoteVersion, String currentVersion) {
        // Rimuovi prefisso 'v' se presente
        String remote = remoteVersion.startsWith("v") ? remoteVersion.substring(1) : remoteVersion;
        String current = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

        // Rimuovi suffissi come -SNAPSHOT
        remote = remote.split("-")[0];
        current = current.split("-")[0];

        String[] remoteParts = remote.split("\\.");
        String[] currentParts = current.split("\\.");

        int maxLength = Math.max(remoteParts.length, currentParts.length);

        for (int i = 0; i < maxLength; i++) {
            int remotePart = i < remoteParts.length ? parseVersionPart(remoteParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

            if (remotePart > currentPart) {
                return true;
            } else if (remotePart < currentPart) {
                return false;
            }
        }

        return false;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void showUpdateDialog(Component parent, String currentVersion, List<ReleaseInfo> newerReleases) {
        ReleaseInfo latestRelease = newerReleases.get(0);

        StringBuilder message = new StringBuilder();
        message.append("============================================================\n");
        message.append("       NUOVA VERSIONE DISPONIBILE DI EPPCLIENT!\n");
        message.append("============================================================\n\n");
        
        message.append("  Versione attuale:  ").append(currentVersion).append("\n");
        message.append("  Ultima versione:   ").append(latestRelease.tagName).append("\n\n");
        
        message.append("------------------------------------------------------------\n");
        message.append("                         CHANGELOG\n");
        message.append("------------------------------------------------------------\n\n");

        for (ReleaseInfo release : newerReleases) {
            message.append("[ ").append(release.tagName);
            if (!release.name.equals(release.tagName)) {
                message.append(" - ").append(release.name);
            }
            message.append(" ]\n");
            message.append("------------------------------------------------------------\n");
            if (!release.body.isEmpty()) {
                message.append(formatBodyPlain(release.body));
            }
            message.append("\n");
        }

        message.append("============================================================\n");
        message.append("  Maggiori informazioni su:\n");
        message.append("  https://github.com/CodeToTime/EPPClient/releases\n");
        message.append("============================================================\n");

        JTextArea textArea = new JTextArea(message.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 400));

        JOptionPane.showMessageDialog(parent, scrollPane, "Aggiornamento disponibile", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String formatBodyPlain(String body) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = body.split("\n");
        for (String line : lines) {
            if (line.matches("^[\\-\\*]\\s*.*")) {
                line = "  - " + line.replaceFirst("^[\\-\\*]\\s*", "");
            } else {
                line = "  " + line;
            }
            formatted.append(line).append("\n");
        }
        return formatted.toString();
    }

    private static class ReleaseInfo {
        final String tagName;
        final String name;
        final String body;

        ReleaseInfo(String tagName, String name, String body) {
            this.tagName = tagName;
            this.name = name;
            this.body = body;
        }
    }
}
