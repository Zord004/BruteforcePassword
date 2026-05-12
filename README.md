#  Brute Force Password Cracker

> This project is intended for educational purposes and authorized security testing only.
Do not use against systems without explicit permission.
> L'utilizzo di tecniche brute force contro sistemi, account o servizi senza autorizzazione costituisce **reato** in tutti i paesi. L'autore non si assume alcuna responsabilità per usi impropri.

## Caratteristiche

-  **Lunghezza dinamica**: la password può essere di qualsiasi lunghezza, calcolata automaticamente
- **4 set di caratteri selezionabili**: minuscole, +maiuscole, +numeri, +simboli
-  **Interfaccia colorata** con codici ANSI per terminale
-  **Barra di progresso live** con tentativi/secondo, percentuale completata e ETA
-  **Statistiche finali** complete (tentativi, tempo, velocità media)
- **Validazione input**: avvisa se la password contiene caratteri fuori dal set scelto
-  **Algoritmo ricorsivo** con interruzione immediata al match

---

## Requisiti

- **Java JDK 8** o versioni successive
- Un terminale che supporti i codici ANSI (consigliato):
    -  Linux / macOS Terminal
    -  Windows Terminal, PowerShell 7+
    -  IntelliJ IDEA, VS Code, BlueJ recenti
    -  Vecchi cmd.exe di Windows: i colori potrebbero apparire come testo

Per verificare la versione di Java installata:
```bash
java -version
javac -version
```

---

##  Installazione ed esecuzione

### 1. Scaricare il file
Salvare il file `BruteForcePassword.java` in una cartella a piacere.

### 2. Compilare il programma
Aprire un terminale nella cartella del file ed eseguire:
```bash
javac BruteForcePassword.java
```
Verrà generato il file `BruteForcePassword.class`.

### 3. Eseguire il programma
```bash
java BruteForcePassword
```

##  Avvertenze

> Questo programma è stato sviluppato **esclusivamente a scopo didattico**, per dimostrare in modo pratico:
> - il funzionamento di un algoritmo ricorsivo
> - la crescita esponenziale dello spazio di ricerca
> - l'importanza di scegliere password lunghe e complesse

L'utilizzo di tecniche brute force contro sistemi, account o servizi senza autorizzazione costituisce **reato** in tutti i paesi. L'autore non si assume alcuna responsabilità per usi impropri.
