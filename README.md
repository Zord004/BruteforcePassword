#  Brute Force Password Cracker

> This project is intended for educational purposes and authorized security testing only.
Do not use against systems without explicit permission.


## Caratteristiche

-  **Lunghezza dinamica**: la password può essere di qualsiasi lunghezza, calcolata automaticamente
- **4 set di caratteri selezionabili**: minuscole, +maiuscole, +numeri, +simboli
-  **Interfaccia colorata** con codici ANSI per terminale
-  **Barra di progresso live** con tentativi/secondo, percentuale completata e ETA
-  **Statistiche finali** complete (tentativi, tempo, velocità media)
- **Validazione input**: avvisa se la password contiene caratteri fuori dal set scelto
-  **Algoritmo ricorsivo** con interruzione immediata al match

---

## 🔧 Requisiti

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

---



##  Come funziona l'algoritmo

L'algoritmo utilizza un approccio **ricorsivo** per generare in ordine alfabetico tutte le combinazioni possibili dei caratteri del set scelto:

1. **Input**: l'utente seleziona il set di caratteri e inserisce la password
2. **Calcolo lunghezza**: il programma rileva automaticamente quanti caratteri ha la password con `password.length()`
3. **Generazione ricorsiva**: il metodo `generaCombinazioni()` riempie un array di caratteri posizione per posizione, provando ogni simbolo del set in ogni posizione
4. **Confronto**: quando l'array è completo, la stringa viene confrontata con la password inserita
5. **Terminazione**: appena trova una corrispondenza, un flag `trovata = true` blocca tutte le chiamate ricorsive in cascata

### Schema dell'algoritmo ricorsivo

```
generaCombinazioni(array, posizione):
    se trovata → return
    se posizione == lunghezza:
        confronta array con password
        se uguali → trovata = true
        return
    per ogni carattere c nel set:
        array[posizione] = c
        generaCombinazioni(array, posizione + 1)
```

---

## Complessità e prestazioni

Il numero di combinazioni possibili cresce **esponenzialmente** secondo la formula:

> **k^n**, dove k è la dimensione del set di caratteri e n la lunghezza della password

| Set di caratteri | k | Lunghezza 4 | Lunghezza 5 | Lunghezza 6 | Lunghezza 8 |
|------------------|---|-------------|-------------|-------------|-------------|
| Solo minuscole | 26 | 456.976 | 11.881.376 | 308.915.776 | 208 miliardi |
| + Maiuscole | 52 | 7.311.616 | 380.204.032 | ~19 miliardi | ~53.000 miliardi |
| + Numeri | 62 | 14.776.336 | ~916 milioni | ~56 miliardi | ~218.000 miliardi |
| + Simboli | 69 | 22.667.121 | ~1,5 miliardi | ~107 miliardi | ~510.000 miliardi |

 **Attenzione**: con password di 7-8 caratteri e set ampio, il tempo di esecuzione può diventare proibitivo (ore o giorni). Questo dimostra **perché le password lunghe e con simboli misti sono più sicure**.


##  Avvertenze

> Questo programma è stato sviluppato **esclusivamente a scopo didattico**, per dimostrare in modo pratico:
> - il funzionamento di un algoritmo ricorsivo
> - la crescita esponenziale dello spazio di ricerca
> - l'importanza di scegliere password lunghe e complesse

L'utilizzo di tecniche brute force contro sistemi, account o servizi senza autorizzazione costituisce **reato** in tutti i paesi. L'autore non si assume alcuna responsabilità per usi impropri.



**Realizzato con Java**
