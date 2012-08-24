/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathek.controller.filmeLaden.importieren;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import mediathek.controller.filmeLaden.FilmeLaden;
import mediathek.controller.filmeLaden.ListenerFilmeLaden;
import mediathek.controller.filmeLaden.ListenerFilmeLadenElement;
import mediathek.controller.io.IoXmlFilmlisteLesen;
import mediathek.daten.ListeFilme;
import mediathek.tool.Log;

public class ImportFilmliste {

    public ListeFilme listeFilme;
    public String[] filmlisteMetaDaten;
    private EventListenerList listeners = new EventListenerList();
    private IoXmlFilmlisteLesen ioXmlFilmlisteLesen = null;
    public FilmUpdateServer filmUpdateServer = new FilmUpdateServer();

    /**
     *
     * @param ddaten
     */
    public ImportFilmliste() {
        ioXmlFilmlisteLesen = new IoXmlFilmlisteLesen();
        ioXmlFilmlisteLesen.addAdListener(new BeobLaden());
    }

    // #######################################
    // Filme von Server/Datei importieren
    // #######################################
    public void filmeImportierenAuto() {
        new Thread(new FilmeImportierenAutoThread()).start();
    }

    private class FilmeImportierenAutoThread implements Runnable {

        @Override
        public synchronized void run() {
            //wenn auto-update-url dann erst mal die Updateserver aktualiseren laden
            boolean ret = false;
            FilmeLaden.updateUrl = filmUpdateServer.suchen();
            if (!FilmeLaden.updateUrl.equals("")) {
                for (int i = 0; i < 10; ++i) {
                    //10 mal mit einem anderen Server probieren
                    if (urlLaden(FilmeLaden.updateUrl, true)) {
                        // hat geklappt, nix wie weiter
                        ret = true;
                        break;
                    }
                    FilmeLaden.updateUrl = filmUpdateServer.listeUpdateServer.getRand(i); //nächste Adresse in der Liste wählen
                }
            } else {
                fertigMelden();
            }
            if (!ret /* listeFilme ist schon wieder null -> "FilmeLaden" */) {
                JOptionPane.showMessageDialog(null, "Das Laden der Filmliste hat nicht geklappt!", "Fehler", JOptionPane.ERROR_MESSAGE);
                Log.fehlerMeldung(951235497, "Filme laden", "Es konnten keine Filme geladen werden!");
            }
        }
    }

    // #######################################
    // Filme aus Datei laden
    // #######################################
    public void filmeDirektImportierenDatei(String pfad, boolean istUrl) {
        urlLaden(pfad, istUrl);
    }

    public void filmeImportierenDatei(String pfad, boolean istUrl) {
        new Thread(new filmeImportierenDateiThread(pfad, istUrl)).start();
    }

    private class filmeImportierenDateiThread implements Runnable {

        String pfad;
        boolean istUrl;

        public filmeImportierenDateiThread(String ppfad, boolean iistUrl) {
            pfad = ppfad;
            istUrl = iistUrl;
        }

        @Override
        public synchronized void run() {
            if (!urlLaden(pfad, istUrl)) {
                JOptionPane.showMessageDialog(null, "Das Laden der Filmliste hat nicht geklappt!", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //===================================
    // private
    //===================================
    private boolean urlLaden(String dateiUrl, boolean istUrl) {
        boolean ret = false;
        try {
            if (!dateiUrl.equals("")) {
                Log.systemMeldung("Filmliste laden von: " + dateiUrl);
                listeFilme = new ListeFilme();
                ret = ioXmlFilmlisteLesen.filmlisteLesen(dateiUrl, istUrl, listeFilme);
            }
        } catch (Exception ex) {
            Log.fehlerMeldung(965412378, "ImportListe.urlLaden: ", ex);
        }
        return ret;
    }

    // #######################################
    // Listener
    // #######################################
    private synchronized void fertigMelden() {
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.fertig(new ListenerFilmeLadenElement("", "", 0, 0));
        }
    }

    public void addAdListener(ListenerFilmeLaden listener) {
        listeners.add(ListenerFilmeLaden.class, listener);
    }

    private class BeobLaden extends ListenerFilmeLaden {

        @Override
        public synchronized void start(ListenerFilmeLadenElement filmListenerElement) {
            for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
                l.start(filmListenerElement);
            }
        }

        @Override
        public synchronized void progress(ListenerFilmeLadenElement filmListenerElement) {
            for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
                l.progress(filmListenerElement);
            }
        }

        @Override
        public synchronized void fertig(ListenerFilmeLadenElement filmListenerElement) {
            for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
                l.fertig(filmListenerElement);
            }
        }
    }
}