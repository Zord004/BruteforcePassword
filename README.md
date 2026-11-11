# Brute Force Password Cracker

⚠️ **ATTENZIONE / DISCLAIMER:** 
*Questo progetto è destinato esclusivamente a scopi didattici e per test di sicurezza autorizzati. Non utilizzare contro sistemi senza un permesso esplicito. L'utilizzo di tecniche di brute force contro sistemi, account o servizi senza autorizzazione costituisce reato. L'autore non si assume alcuna responsabilità per usi impropri.*

---

## 🇮🇹 Versione Italiana

### Caratteristiche
- **Lunghezza dinamica:** la password può essere di qualsiasi lunghezza, calcolata automaticamente.
- **Set di caratteri personalizzabili:** 4 set selezionabili (minuscole, maiuscole, numeri, simboli).
- **Interfaccia UI da terminale:** interfaccia colorata tramite codici ANSI.
- **Monitoraggio in tempo reale:** barra di progresso live con tentativi/secondo, percentuale completata ed ETA.
- **Statistiche finali complete:** report su tentativi totali, tempo impiegato, lavoro per singolo thread e velocità media.
- **Validazione input:** avvisa se la password inserita contiene caratteri fuori dal set scelto.
- **Algoritmo Multithread Ottimizzato:** approccio iterativo parallelo basato su *interleaving* (equa divisione del carico sui core della CPU), assenza di allocazione di stringhe nel loop e interruzione immediata al momento del match.

### Requisiti
- **Java JDK 8** o versioni successive
- Un terminale che supporti i codici ANSI (consigliato per i colori):
  - Linux / macOS Terminal
  - Windows Terminal, PowerShell 7+
  - Terminali integrati di IntelliJ IDEA, VS Code
  - *Nota: sui vecchi cmd.exe di Windows i colori potrebbero apparire come testo non formattato.*

Per verificare la versione di Java installata, esegui:
```bash
java -version
javac -version
```

# Brute Force Password Cracker

⚠️ **DISCLAIMER:** *This project is intended for educational purposes and authorized security testing only. Do not use against systems without explicit permission.*
*The use of brute force techniques against systems, accounts, or services without authorization is illegal. The author assumes no responsibility for any misuse.*

## Features
- **Dynamic length**: The password can be of any length, calculated automatically.
- **Customizable character sets**: 4 selectable sets (lowercase, uppercase, numbers, symbols).
- **Colorful Terminal UI**: ANSI code support for terminal styling.
- **Live tracking**: Real-time progress bar showing attempts/second, completion percentage, and ETA.
- **Final statistics**: Comprehensive metrics (total attempts, time elapsed, average speed).
- **Input validation**: Warns if the target password contains characters outside the chosen set.
- **Recursive algorithm**: Features immediate termination upon a successful match.

## Requirements
- **Java JDK 8** or newer
- A terminal that supports ANSI codes (recommended for UI colors):
  - Linux / macOS Terminal
  - Windows Terminal, PowerShell 7+
  - Recent IntelliJ IDEA, VS Code, or BlueJ integrated terminals
  - *Note: On older Windows cmd.exe, colors may display as plain text tags.*

To check your installed Java version:
```bash
java -version
javac -version
