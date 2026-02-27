# EPPClient

EPPClient è un'applicazione desktop Java per l'interazione con i server EPP (Extensible Provisioning Protocol) del Registro .it, a supporto dei Registrar nella gestione di domini, contatti e messaggi EPP. Fornisce un'interfaccia grafica e strumenti idonei alle principali operazioni di registrazione e mantenimento.

## Cos'è e a cosa serve
- Gestione dei domini .it tramite EPP (creazione, aggiornamento, trasferimento, ecc.).
- Gestione della rubrica contatti e della messaggistica EPP.
- Funzionalità operative a supporto dell'attività quotidiana del Registrar.

## Origini del progetto
Il progetto nasce come rimodernizzazione del software originale sviluppato dall'associazione [AssoTLD](https://www.assotld.it/) per i propri soci. Il software è stato aggiornato e rilasciato come software libero, con l'aggiunta di varie funzionalità, tra cui il supporto a DNSSEC, la gestione dei domini .gov.it e .edu.it e l'esportazione del database in formato CSV, oltre all'aggiornamento di tutte le dipendenze obsolete.
## Licenze

- Le informazioni dettagliate sono disponibili nella cartella `LICENSES/` alla radice del progetto e nel file `REUSE.toml` (convenzioni [REUSE](https://reuse.software/)).
- Le licenze utilizzate includono Apache 2.0 e GPL-3.0-or-later, dove indicato.

---

## Libreria client EPP del Registro .it

Il progetto utilizza la libreria ufficiale del Registro .it, fornita sotto forma di file JAR denominato `epp-client-cmd-line-*.jar`. Questa libreria è indispensabile per la compilazione e l'esecuzione del client EPP e fornisce tutte le funzionalità necessarie per interagire correttamente con i server del Registro .it.

La libreria è disponibile **esclusivamente per i Registrar autorizzati** tramite il portale del registro e non può essere distribuita pubblicamente. Per integrare la libreria nel progetto, il file JAR deve essere posizionato all'interno della cartella `libs/`.

Il nome esatto del JAR da utilizzare è indicato nel file `build.gradle` alla sezione `dependencies`.

---

## Guida per lo Sviluppo

Per la compilazione e l'esecuzione in ambiente di sviluppo si utilizza Gradle.

### Requisiti
- Java 21

### Esecuzione in sviluppo
Per avviare l'applicazione direttamente tramite Gradle, eseguire:
```bash
./gradlew run
```

### Compilazione
Per compilare il progetto senza eseguirlo:
```bash
./gradlew build
```

---

## Creazione del Fat Jar (Shadow Jar)

Il "fat jar" è un file JAR che include tutte le dipendenze necessarie per eseguire l'applicazione in modo autonomo.

### Generazione del Jar eseguibile
Per generare il fat jar, eseguire il comando:
```bash
./gradlew shadowJar
```
Al termine, il file generato sarà disponibile nella cartella:
`build/libs/`

Il nome del file sarà simile a:
`eppclient-<versione>-all.jar`

(Esempio: `eppclient-2.3.1-SNAPSHOT-all.jar`)

### Esecuzione del Fat Jar
Per eseguire l'applicazione utilizzare il comando `java -jar`:
```bash
java -jar build/libs/eppclient-<versione>-all.jar
```
*(Verificare il nome del file generato presente nella cartella build/libs)*

---

## Opzioni di debug

Per abilitare i messaggi di debug nella console, utilizzare il flag `-Deppclient.debug=true`:
```bash
java -Deppclient.debug=true -jar build/libs/eppclient-<versione>-all.jar
```

Questa opzione attiva la stampa di messaggi di log utili per il troubleshooting.

---

## Note sulle dipendenze e compatibilità

Questa sezione descrive le principali dipendenze del progetto e le scelte adottate per garantire la compatibilità con la libreria del Registro .it e il corretto funzionamento dell'applicazione.

### xmlbeans (3.1.0)

La libreria è stata aggiornata alla versione **3.1.0**, l'ultima compatibile con la libreria del Registro .it, che internamente utilizza la versione 2.6.0.

### Apache HttpComponents: httpclient (4.5.14) e httpcore (4.4.16)

La vecchia libreria `commons-httpclient` è stata **aggiornata** a `org.apache.httpcomponents:httpclient` e `httpcore`. Non è stato adottato `httpclient5` perché richiederebbe un refactoring esteso e la versione attuale non presenta vulnerabilità note su Maven Central.

### epp-client-cmd-line (Registro .it)

La libreria ufficiale del Registro .it è disponibile solo per i Registrar autorizzati tramite il portale NIC. È obbligatoria per la compilazione e l'esecuzione del client. Il JAR deve essere collocato nella cartella `libs/` del progetto.

### derby & derbytools (10.17.1.0)

Il progetto Derby è stato abbandonato nel 2025. Per il momento si è deciso di continuare ad utilizzarlo nel progetto, per garantire compatibilità con le installazioni esistenti.

---

## Software Bill of Materials (SBOM)

Componenti e versioni principali attualmente in uso (si veda `build.gradle`):
- org.apache.xmlbeans:xmlbeans:3.1.0
- org.apache.httpcomponents:httpclient:4.5.14
- org.apache.httpcomponents:httpcore:4.4.16
- epp-client-cmd-line:1.19.1 (Fornito dal Registro .it, JAR locale da collocare in `libs/`)
- org.apache.derby:derby:10.17.1.0
- org.apache.derby:derbytools:10.17.1.0
- org.slf4j:slf4j-api:2.0.17
- org.slf4j:slf4j-simple:2.0.17
- com.google.code.gson:gson:2.13.2
- test: org.junit:junit-bom:5.10.0; org.junit.jupiter:junit-jupiter

Runtime/Build:
- Java 21

---
