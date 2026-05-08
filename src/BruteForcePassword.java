import java.util.Scanner;

public class BruteForcePassword {

    // ----- Codici ANSI per colorare il terminale -----
    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String RED    = "\033[31m";
    private static final String GREEN  = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE   = "\033[34m";
    private static final String CYAN   = "\033[36m";

    // ----- Set di caratteri disponibili -----
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%&*";

    // ----- Variabili di stato -----
    private static char[] caratteri;
    private static int lunghezza;
    private static long spazioRicerca;

    private static long tentativi = 0;
    private static boolean trovata = false;
    private static String passwordTrovata = "";
    private static long tempoInizio;
    private static long ultimoUpdate = 0;

    public static void main(String[] args) {
        stampaBanner();

        Scanner sc = new Scanner(System.in);

        // 1) Selezione del set di caratteri
        caratteri = selezionaCaratteri(sc);

        // 2) Input password
        System.out.print(BOLD + CYAN + "[?] Inserisci la password da craccare: " + RESET);
        String password = sc.nextLine();

        // 3) La lunghezza viene calcolata automaticamente
        lunghezza = password.length();
        if (lunghezza == 0) {
            System.out.println(RED + "[!] Errore: la password non puo' essere vuota." + RESET);
            sc.close();
            return;
        }

        // 4) Controllo che tutti i caratteri siano nel set scelto
        if (!verificaSet(password)) {
            sc.close();
            return;
        }

        // 5) Calcolo dello spazio di ricerca (n^lunghezza)
        spazioRicerca = (long) Math.pow(caratteri.length, lunghezza);

        // 6) Info iniziali
        System.out.println();
        System.out.println(BLUE + "[i] Lunghezza rilevata:    " + RESET + lunghezza + " caratteri");
        System.out.println(BLUE + "[i] Set di caratteri:      " + RESET + caratteri.length + " simboli");
        System.out.println(BLUE + "[i] Spazio di ricerca:     " + RESET + String.format("%,d", spazioRicerca) + " combinazioni");
        System.out.println(YELLOW + "\n[*] Avvio attacco brute force...\n" + RESET);

        tempoInizio = System.currentTimeMillis();

        // 7) Generazione ricorsiva
        char[] combinazione = new char[lunghezza];
        generaCombinazioni(combinazione, 0, password);

        long tempoTotale = System.currentTimeMillis() - tempoInizio;

        // 8) Pulisco la riga di progresso e stampo il risultato
        System.out.print("\r" + " ".repeat(90) + "\r");
        if (trovata) {
            stampaSuccesso(passwordTrovata, tempoTotale);
        } else {
            System.out.println(RED + "[!] Password non trovata." + RESET);
        }

        sc.close();
    }

    /** Stampa il banner iniziale */
    private static void stampaBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("+==================================================+");
        System.out.println("|       BRUTE FORCE PASSWORD CRACKER  v2.0         |");
        System.out.println("|             ~ educational use only ~             |");
        System.out.println("+==================================================+");
        System.out.println(RESET);
    }

    /** Mostra il menu e ritorna l'array di caratteri scelto */
    private static char[] selezionaCaratteri(Scanner sc) {
        System.out.println(BOLD + "[?] Scegli il set di caratteri:" + RESET);
        System.out.println("    " + GREEN + "1)" + RESET + " Lettere minuscole (a-z)              [26 caratteri]");
        System.out.println("    " + GREEN + "2)" + RESET + " Lettere minuscole + maiuscole         [52 caratteri]");
        System.out.println("    " + GREEN + "3)" + RESET + " Lettere + numeri                      [62 caratteri]");
        System.out.println("    " + GREEN + "4)" + RESET + " Tutto (lettere + numeri + simboli)    [69 caratteri]");
        System.out.print(BOLD + "[>] Scelta: " + RESET);

        int scelta;
        try {
            scelta = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            scelta = 1;
        }

        String set;
        switch (scelta) {
            case 2:  set = LOWERCASE + UPPERCASE; break;
            case 3:  set = LOWERCASE + UPPERCASE + DIGITS; break;
            case 4:  set = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS; break;
            default: set = LOWERCASE; break;
        }
        return set.toCharArray();
    }

    /** Verifica che ogni carattere della password appartenga al set scelto */
    private static boolean verificaSet(String password) {
        for (char c : password.toCharArray()) {
            boolean ok = false;
            for (char k : caratteri) {
                if (k == c) { ok = true; break; }
            }
            if (!ok) {
                System.out.println(RED + "[!] Errore: il carattere '" + c +
                        "' non appartiene al set scelto. Scegli un set piu' ampio." + RESET);
                return false;
            }
        }
        return true;
    }

    /** Algoritmo ricorsivo che genera tutte le combinazioni */
    private static void generaCombinazioni(char[] combinazione, int posizione, String password) {
        if (trovata) return;

        // Caso base: combinazione completa
        if (posizione == lunghezza) {
            tentativi++;
            String tentativo = new String(combinazione);

            // Aggiorno la barra di progresso al massimo ogni 100 ms
            long ora = System.currentTimeMillis();
            if (ora - ultimoUpdate > 100) {
                stampaProgresso(tentativo, ora);
                ultimoUpdate = ora;
            }

            if (tentativo.equals(password)) {
                trovata = true;
                passwordTrovata = tentativo;
            }
            return;
        }

        // Caso ricorsivo: provo ogni carattere nella posizione corrente
        for (int i = 0; i < caratteri.length; i++) {
            combinazione[posizione] = caratteri[i];
            generaCombinazioni(combinazione, posizione + 1, password);
            if (trovata) return;
        }
    }

    /** Stampa la barra di progresso e le statistiche live (sovrascrive la riga) */
    private static void stampaProgresso(String tentativoCorrente, long ora) {
        long tempoTrascorso = ora - tempoInizio;
        if (tempoTrascorso == 0) tempoTrascorso = 1;

        long velocita = tentativi * 1000 / tempoTrascorso;
        double progressoPerc = (tentativi * 100.0) / spazioRicerca;
        long etaSec = velocita > 0 ? (spazioRicerca - tentativi) / velocita : 0;

        // Costruzione barra di progresso
        int barLength = 20;
        int filled = (int) (progressoPerc / 100 * barLength);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "#" : "-");
        }
        bar.append("]");

        System.out.printf("\r" + YELLOW + "[CRACKING]" + RESET +
                        " %s tentativo: %-8s %5.1f%% | %,9d t/s | ETA: %ds   ",
                bar.toString(),
                tentativoCorrente,
                progressoPerc,
                velocita,
                etaSec);
        System.out.flush();
    }

    /** Riquadro di successo finale */
    private static void stampaSuccesso(String password, long tempoMs) {
        long velocitaMedia = tempoMs > 0 ? tentativi * 1000 / tempoMs : tentativi;

        System.out.println();
        System.out.println(GREEN + BOLD + "[+] PASSWORD TROVATA!" + RESET);
        System.out.println(GREEN + "+--------------------------------------------------+" + RESET);
        stampaRiga("Password:",        password);
        stampaRiga("Tentativi:",       String.format("%,d", tentativi));
        stampaRiga("Tempo:",           tempoMs + " ms");
        stampaRiga("Velocita' media:", String.format("%,d tentativi/sec", velocitaMedia));
        System.out.println(GREEN + "+--------------------------------------------------+" + RESET);
    }

    /** Riga formattata del riquadro finale */
    private static void stampaRiga(String etichetta, String valore) {
        String contenuto = String.format(" %-18s %-29s", etichetta, valore);
        System.out.println(GREEN + "|" + RESET + contenuto + GREEN + "|" + RESET);
    }
}