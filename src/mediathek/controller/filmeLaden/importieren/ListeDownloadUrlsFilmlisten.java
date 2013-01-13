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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

public class ListeDownloadUrlsFilmlisten extends LinkedList<DatenUrlFilmliste> {

    public boolean addWithCheck(DatenUrlFilmliste film) {
        ListIterator<DatenUrlFilmliste> it = listIterator();
        while (it.hasNext()) {
            if (it.next().arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].equals(film.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR])) {
                return false;
            }
        }
        return add(film);
    }

    public void sort() {
        int nr = 0;
        Collections.<DatenUrlFilmliste>sort(this);
        Iterator<DatenUrlFilmliste> it = this.iterator();
        while (it.hasNext()) {
            String str = String.valueOf(nr++);
            while (str.length() < 3) {
                str = "0" + str;
            }
            it.next().arr[FilmlistenSuchen.FILM_UPDATE_SERVER_NR_NR] = str;
        }
    }

    public String[][] getTableObjectData() {
        DatenUrlFilmliste filmUpdate;
        String[][] object;
        ListIterator<DatenUrlFilmliste> iterator = this.listIterator();
        object = new String[this.size()][FilmlistenSuchen.FILM_UPDATE_SERVER_MAX_ELEM];
        int i = 0;
        while (iterator.hasNext()) {
            filmUpdate = iterator.next();
            object[i] = filmUpdate.arr;
            ++i;
        }
        return object;
    }

    public int getNr(String url) {
        int nr = 0;
        ListIterator<DatenUrlFilmliste> iterator = this.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].equals(url)) {
                break;
            }
            ++nr;
        }
        if (nr >= this.size()) {
            nr = 0;
        }
        return nr;
    }

    public DatenUrlFilmliste getDatenUrlFilmliste(String url) {
        DatenUrlFilmliste update;
        ListIterator<DatenUrlFilmliste> iterator = this.listIterator();
        while (iterator.hasNext()) {
            update = iterator.next();
            if (update.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].equals(url)) {
                return update;
            }
        }
        return null;
    }

    public String getRand(ArrayList<String> bereitsGebraucht, int errcount) {
        final int MAXMINUTEN = 50;
        int minCount = 3;
        if (errcount > 0) {
            minCount = 3 + 2 * errcount;
        }
        String ret = "";
        if (!this.isEmpty()) {
            DatenUrlFilmliste filmUpdate;
            LinkedList<DatenUrlFilmliste> listeZeit = new LinkedList<DatenUrlFilmliste>();
            LinkedList<DatenUrlFilmliste> listePrio = new LinkedList<DatenUrlFilmliste>();
            //aktuellsten auswählen
            Iterator<DatenUrlFilmliste> it = this.iterator();
            Date today = new Date(System.currentTimeMillis());
            String date;
            Date d;
            int minuten = 200;
            int count = 0;
            while (it.hasNext()) {
                filmUpdate = it.next();
                if (bereitsGebraucht != null) {
                    if (bereitsGebraucht.contains(filmUpdate.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR])) {
                        // wurde schon versucht
                        continue;
                    }
                }
                date = filmUpdate.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_DATUM_NR] + " " + filmUpdate.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_ZEIT_NR];
                try {
                    d = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(date);
                    minuten = Math.round((today.getTime() - d.getTime()) / (1000 * 60));
                } catch (ParseException ex) {
                }
                if (minuten < MAXMINUTEN) {
                    listeZeit.add(filmUpdate);
                    ++count;
                } else if (count < minCount) {
                    listeZeit.add(filmUpdate);
                    ++count;
                }
            }
            //nach prio gewichten
            it = listeZeit.iterator();
            while (it.hasNext()) {
                filmUpdate = it.next();
                if (filmUpdate.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_PRIO_NR].equals(FilmlistenSuchen.FILM_UPDATE_SERVER_PRIO_1)) {
                    listePrio.add(filmUpdate);
                } else {
                    listePrio.add(filmUpdate);
                    listePrio.add(filmUpdate);
                }
            }
            if (listePrio.size() > 0) {
                int nr = new Random().nextInt(listePrio.size());
                filmUpdate = listePrio.get(nr);
            } else {
                int nr = new Random().nextInt(this.size());
                filmUpdate = this.get(nr);
            }
            ret = filmUpdate.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR];
        }
        return ret;
    }
}