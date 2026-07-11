import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BruteForcePassword {

    // ----- Codici ANSI per colorare il terminale -----
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String RED     = "\033[31m";
    private static final String GREEN   = "\033[32m";
    private static final String YELLOW  = "\033[33m";
    private static final String BLUE    = "\033[34m";
    private static final String CYAN    = "\033[36m";

    // ----- Set di caratteri disponibili -----
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*()-_=+[]{};:,.<>?/|~";

    // ----- Parametri di esecuzione -----
    private static char[] caratteri;
    private static int lunghezza;
    private static long spazioRicerca;
    private static int numThread;
    private static char[] passwordChars;

    // Ogni quanti tentativi un thread scrive il suo contatore locale su quello atomico
    private static final long FLUSH_INTERVAL = 100_000L;

    // ----- Stato condiviso tra thread -----
    private static final AtomicBoolean trovata = new AtomicBoolean(false);
    private static AtomicLong[] tentativiPerThread;
    private static volatile String[] ultimoTentativoPerThread;
    private static volatile String passwordTrovata = "";
    private static volatile int threadVincitore = -1;
    private static long tempoInizio;

    public static void main(String[] args) throws InterruptedException {
        stampaBanner();
        Scanner sc = new Scanner(System.in);

        caratteri = selezionaCaratteri(sc);

        System.out.print(BOLD + CYAN + "[?] Inserisci la password da craccare: " + RESET);
        String password = sc.nextLine();
        lunghezza = password.length();

        if (lunghezza == 0) {
            System.out.println(RED + "[!] Errore: la password non puo' essere vuota." + RESET);
            sc.close();
            return;
        }
        if (!verificaSet(password)) {
            sc.close();
            return;
        }

        passwordChars = password.toCharArray();
        spazioRicerca = (long) Math.pow(caratteri.length, lunghezza);

        // Numero di thread = numero di core CPU (o forzato via riga di comando)
        if (args.length > 0) {
            try { numThread = Integer.parseInt(args[0]); }
            catch (NumberFormatException e) { numThread = Runtime.getRuntime().availableProcessors(); }
        } else {
            numThread = Runtime.getRuntime().availableProcessors();
        }
        if (spazioRicerca < numThread) numThread = (int) Math.max(1, spazioRicerca);

        tentativiPerThread = new AtomicLong[numThread];
        ultimoTentativoPerThread = new String[numThread];
        for (int i = 0; i < numThread; i++) {
            tentativiPerThread[i] = new AtomicLong(0);
            ultimoTentativoPerThread[i] = "...";
        }

        System.out.println();
        System.out.println(BLUE + "[i] Lunghezza rilevata:    " + RESET + lunghezza + " caratteri");
        System.out.println(BLUE + "[i] Set di caratteri:      " + RESET + caratteri.length + " simboli");
        System.out.println(BLUE + "[i] Spazio di ricerca:     " + RESET + String.format("%,d", spazioRicerca) + " combinazioni");
        System.out.println(BLUE + "[i] Thread paralleli:      " + RESET + numThread + " (1 per core CPU)");
        System.out.println(BLUE + "[i] Ottimizzazioni:        " + RESET + "interleaving + iterativa + no String alloc");
        System.out.println(YELLOW + "\n[*] Avvio attacco brute force ottimizzato...\n" + RESET);

        tempoInizio = System.currentTimeMillis();

        // Ogni thread riceve un offset di partenza e un passo (= numThread).
        // Strategia INTERLEAVING: Thread-i prova le combinazioni i, i+N, i+2N, ...
        // Questo garantisce che tutti i thread scansionino tutto lo spazio in
        // parallelo, eliminando il problema di thread "sfortunati" con range
        // contigui in cui la password si trova alla fine.
        Thread[] threads = new Thread[numThread];
        for (int i = 0; i < numThread; i++) {
            final int idThread = i;
            threads[i] = new Thread(() -> cerca(idThread, idThread, numThread), "Thread-" + i);
        }

        // Thread "spia" che aggiorna la UI
        Thread watcher = new Thread(() -> {
            while (!trovata.get()) {
                stampaProgresso();
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
            }
        }, "WATCHER");
        watcher.setDaemon(true);

        for (Thread t : threads) t.start();
        watcher.start();
        for (Thread t : threads) t.join();

        long tempoTotale = System.currentTimeMillis() - tempoInizio;

        // Pulisco il blocco dashboard prima del risultato finale
        if (righeStampate > 0) {
            StringBuilder clear = new StringBuilder();
            for (int i = 0; i < righeStampate; i++) clear.append("\033[F\033[K");
            System.out.print(clear.toString());
        }

        if (trovata.get()) {
            stampaSuccesso(passwordTrovata, tempoTotale);
        } else {
            System.out.println(RED + "[!] Password non trovata." + RESET);
        }

        sc.close();
    }

    /**
     * Cuore dell'algoritmo: versione ITERATIVA con INTERLEAVING.
     *
     * Ogni combinazione e' rappresentata come numero in base k (k = caratteri.length).
     * Esempio con k=26: aaaaa=0, aaaab=1, ..., zzzzz=11.881.375
     *
     * Il thread parte dalla combinazione numero "offset" e salta di "passo"
     * combinazioni alla volta. Es. con 4 thread:
     *   Thread-0: prova 0, 4, 8, 12, ...
     *   Thread-1: prova 1, 5, 9, 13, ...
     *   Thread-2: prova 2, 6, 10, 14, ...
     *   Thread-3: prova 3, 7, 11, 15, ...
     *
     * Cosi' tutti i thread esplorano l'intero spazio in parallelo, garantendo
     * uno speedup ~Nx indipendentemente da dove si trovi la password.
     *
     * Ottimizzazioni applicate:
     *   1) Nessuna allocazione di String dentro al loop
     *   2) Contatore locale al thread, flushato su atomico ogni FLUSH_INTERVAL
     *   3) Confronto char[] vs char[] con uscita anticipata al primo mismatch
     *   4) INTERLEAVING: distribuzione equa dello spazio di ricerca
     *
     * @param idThread identificativo del thread (per le statistiche)
     * @param offset   primo numero di combinazione da provare
     * @param passo    intervallo tra una combinazione e la successiva
     */
    private static void cerca(int idThread, long offset, long passo) {
        final int k = caratteri.length;
        final int len = lunghezza;
        final char[] pwd = passwordChars;

        // Array della combinazione corrente e indici associati
        char[] comb = new char[len];
        int[]  indici = new int[len];

        long contLocale = 0;
        long numero = offset;

        while (numero < spazioRicerca) {

            // Decodifico il "numero" attuale nei suoi indici (base k)
            long n = numero;
            for (int i = len - 1; i >= 0; i--) {
                indici[i] = (int)(n % k);
                comb[i] = caratteri[indici[i]];
                n /= k;
            }

            // Controllo periodico del flag di terminazione (ogni 4096 tentativi)
            if ((contLocale & 4095) == 0 && trovata.get()) {
                tentativiPerThread[idThread].addAndGet(contLocale);
                return;
            }

            contLocale++;

            // ----- Confronto carattere per carattere (uscita anticipata) -----
            boolean match = true;
            for (int p = 0; p < len; p++) {
                if (comb[p] != pwd[p]) { match = false; break; }
            }
            if (match) {
                if (trovata.compareAndSet(false, true)) {
                    passwordTrovata = new String(comb);
                    threadVincitore = idThread;
                }
                tentativiPerThread[idThread].addAndGet(contLocale);
                return;
            }

            // ----- Flush periodico del contatore locale -----
            if (contLocale >= FLUSH_INTERVAL) {
                tentativiPerThread[idThread].addAndGet(contLocale);
                contLocale = 0;
                ultimoTentativoPerThread[idThread] = new String(comb);
            }

            // Salto al prossimo numero di mia competenza
            numero += passo;
        }

        // Flush finale del contatore locale
        tentativiPerThread[idThread].addAndGet(contLocale);
    }

    /** Banner ASCII iniziale */
    private static void stampaBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("+========================================================+");
        System.out.println("|     BRUTE FORCE PASSWORD CRACKER  v5.0 [INTERLEAVED]   |");
        System.out.println("|     ~ fair-share parallel search edition ~             |");
        System.out.println("+========================================================+");
        System.out.println(RESET);
    }

    /** Menu di scelta del set di caratteri */
    private static char[] selezionaCaratteri(Scanner sc) {
        System.out.println(BOLD + "[?] Scegli il set di caratteri:" + RESET);
        System.out.println("    " + GREEN + "1)" + RESET + " Lettere minuscole (a-z)              [26 caratteri]");
        System.out.println("    " + GREEN + "2)" + RESET + " Lettere minuscole + maiuscole         [52 caratteri]");
        System.out.println("    " + GREEN + "3)" + RESET + " Lettere + numeri                      [62 caratteri]");
        System.out.println("    " + GREEN + "4)" + RESET + " Tutto (lettere + numeri + simboli)    [90 caratteri]");
        System.out.print(BOLD + "[>] Scelta: " + RESET);

        int scelta;
        try { scelta = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { scelta = 1; }

        String set;
        switch (scelta) {
            case 2:  set = LOWERCASE + UPPERCASE; break;
            case 3:  set = LOWERCASE + UPPERCASE + DIGITS; break;
            case 4:  set = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS; break;
            default: set = LOWERCASE; break;
        }
        return set.toCharArray();
    }

    /** Verifica che ogni carattere appartenga al set */
    private static boolean verificaSet(String password) {
        for (char c : password.toCharArray()) {
            boolean ok = false;
            for (char k : caratteri) {
                if (k == c) { ok = true; break; }
            }
            if (!ok) {
                System.out.println(RED + "[!] Errore: il carattere '" + c +
                        "' non appartiene al set scelto." + RESET);
                return false;
            }
        }
        return true;
    }

    /** Somma i tentativi di tutti i thread */
    private static long tentativiTotali() {
        long tot = 0;
        for (AtomicLong al : tentativiPerThread) tot += al.get();
        return tot;
    }

    // Numero di righe scritte nell'ultimo refresh (per sapere quante "salire")
    private static int righeStampate = 0;

    /**
     * Dashboard live: stampa una riga per ogni thread + intestazione + totali.
     * Usa il codice ANSI "\033[F" per spostare il cursore in su e ridisegnare.
     */
    private static void stampaProgresso() {
        long tempoTrascorso = System.currentTimeMillis() - tempoInizio;
        if (tempoTrascorso == 0) tempoTrascorso = 1;

        long totale = tentativiTotali();
        long velocita = totale * 1000 / tempoTrascorso;
        double progressoPerc = (totale * 100.0) / spazioRicerca;
        if (progressoPerc > 100) progressoPerc = 100;
        long etaSec = velocita > 0 ? Math.max(0, (spazioRicerca - totale) / velocita) : 0;

        // Risalgo di N righe per sovrascrivere il blocco precedente
        StringBuilder sb = new StringBuilder();
        if (righeStampate > 0) {
            for (int i = 0; i < righeStampate; i++) sb.append("\033[F");
        }

        // Riga 1: intestazione con totali
        int barLength = 20;
        int filled = (int) (progressoPerc / 100 * barLength);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) bar.append(i < filled ? "#" : "-");
        bar.append("]");

        sb.append("\r\033[K")  // azzera la riga
                .append(YELLOW).append("[CRACKING] ").append(RESET)
                .append(bar)
                .append(String.format("  %5.1f%% | %,11d t/s | ETA: %ds", progressoPerc, velocita, etaSec))
                .append("\n");

        // Righe 2..N+1: una per ogni thread
        for (int i = 0; i < numThread; i++) {
            long t = tentativiPerThread[i].get();
            String ultimo = ultimoTentativoPerThread[i];
            String colore = sceltColoreThread(i);
            // Mini-barra del lavoro fatto dal singolo thread (in percentuale del totale)
            int miniBar = 10;
            int miniFilled = totale > 0 ? (int)(t * miniBar / Math.max(totale, 1)) : 0;
            StringBuilder mb = new StringBuilder();
            for (int j = 0; j < miniBar; j++) mb.append(j < miniFilled ? "=" : " ");

            sb.append("\r\033[K")  // azzera la riga
                    .append("  ").append(colore).append(String.format("Thread-%-2d", i)).append(RESET)
                    .append(" |").append(mb).append("| ")
                    .append(String.format("ultimo: %-8s ", ultimo))
                    .append(String.format("tentativi: %,12d", t))
                    .append("\n");
        }

        righeStampate = numThread + 1;
        System.out.print(sb.toString());
        System.out.flush();
    }

    /** Assegna un colore ciclico ai thread per distinguerli a colpo d'occhio */
    private static String sceltColoreThread(int i) {
        String[] colori = { GREEN, BLUE, YELLOW, CYAN, RED };
        return colori[i % colori.length];
    }

    /** Riquadro finale con statistiche complete */
    private static void stampaSuccesso(String password, long tempoMs) {
        long totale = tentativiTotali();
        long velocitaMedia = tempoMs > 0 ? totale * 1000 / tempoMs : totale;
        long lavoroVincitore = threadVincitore >= 0 ? tentativiPerThread[threadVincitore].get() : 0;
        double percLavoroVinc = totale > 0 ? (lavoroVincitore * 100.0 / totale) : 0;

        System.out.println();
        System.out.println(GREEN + BOLD + "[+] PASSWORD TROVATA!" + RESET);
        System.out.println(GREEN + "+------------------------------------------------------+" + RESET);
        stampaRiga("Password:",         password);
        stampaRiga("Trovata da:",       "Thread-" + threadVincitore +
                " (" + String.format("%.1f", percLavoroVinc) + "% del lavoro)");
        stampaRiga("Tentativi totali:", String.format("%,d", totale));
        stampaRiga("Tempo:",            tempoMs + " ms");
        stampaRiga("Velocita' totale:", String.format("%,d tentativi/sec", velocitaMedia));
        System.out.println(GREEN + "+------------------------------------------------------+" + RESET);

        // Dettaglio per thread
        System.out.println(BLUE + "\n[i] Dettaglio per thread:" + RESET);
        for (int i = 0; i < numThread; i++) {
            long t = tentativiPerThread[i].get();
            double perc = totale > 0 ? (t * 100.0 / totale) : 0;
            String marker = (i == threadVincitore) ? GREEN + " <-- VINCITORE" + RESET : "";
            System.out.printf("    Thread-%d: %,12d tentativi  (%5.1f%%)%s%n", i, t, perc, marker);
        }
    }

    /** Riga formattata del riquadro */
    private static void stampaRiga(String etichetta, String valore) {
        String contenuto = String.format(" %-18s %-35s", etichetta, valore);
        System.out.println(GREEN + "|" + RESET + contenuto + GREEN + "|" + RESET);
    }
}