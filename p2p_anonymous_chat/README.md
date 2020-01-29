# Anonymous Chat System on p2p network implemented with Tomp2p
### Architetture distribuite per il Cloud
### Università degli Studi di Salerno - Anno Accademico 2019/2020
### Prof. Alberto Negro
### Prof. Carlo Cordasco
### Dott. Carmine Spagnuolo
#### Studente: Antonio Giulio 0522500732
# 
### Problem Statement
Il problema consiste nel progettare e sviluppare un API Java che modella un sistema di chat anonima basata su reti peer to peer. Ogni peer del sistema può inviare messaggi su chat rooms pubbliche in maniera anonima. 
 
Il task originario prevedeva l'implementazione di metodi per creare una nuova room, entrare in una room, abbandonare una room e inviare messaggi. Successivamente sono state aggiunte altre funzionalità, come la possibilità di creare chat private accessibili tramite password, consultare i backup delle chat per capire quali argomenti sono stati trattati, verificare quanti peer sono attivi in una chat e consultare la lista delle chat a cui si è iscritti.
 
Inoltre è stato aggiunto un meccanismo che genera nicknames randomici da utilizzare nelle chat rooms al fine di rendere più comprensibile lo scambio di messaggi conservando allo stesso tempo l'anonimia.
 
### Soluzione proposta
Per realizzare questo sistema è stato utilizzato il modello Topic based del paradigma di comunicazione asincrona Publish/Subscribe su un'architettura p2p.
 
Le chat rooms, quindi, sono state trattate come dei topic a cui i peers possono iscriversi al fine di produrre e consumare messaggi. Ogniqualvolta viene iniviato un messaggio in una room, tutti i peer che ne fanno parte ricevono una notifica contenente il messaggio stesso.
 
Tutto il meccanismo è stato implementanto utilizzando la libreria Java TomP2P che ci consente di utilizzare una Distributed Hash Table (DHT) in cui inserire i vari peer in modo da creare una rete. 
Dunque, le chat room sono state inserite nella Dht come entità che hanno per chiave il nome della room e per valore una struttura dati che contiene i Peer Address dei peer che ne fanno parte.
In questo modo per inviare un messaggio a tutti i peer di una room e quindi simulare una chat, è sufficiente ottenere la lista dei peer di una room. 
# 
#### Struttura del progetto
E' stato utilizzato Apache Maven come software di project management.
 
Nel file pom.xml, project object model, sono state aggiunte le dipendenze a tutte le librerie di riferimento, fra cui TomP2P per la Dht e JUnit per la fase di testing.

```
<repositories>
    <repository>
    	<id>tomp2p.net</id>
        <url>http://tomp2p.net/dev/mvn/</url>
    </repository>
  </repositories>
  <dependency>
    	<groupId>net.tomp2p</groupId>
    	<artifactId>tomp2p-all</artifactId>
     	<version>5.0-Beta8</version>
   </dependency>
   <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
   </dependency>    
```
Il pacchetto ``src/main/java/distributed_system/p2p_anonymous_chat`` contiene le classi:


 - _MessageListener_ un'interfaccia che funge da listener per la notifica dei messaggi.


 - _AnonymousChat_ è l'interfaccia che descrive i metodi che modellano tutte le features del sistema.
 
 
 - _AnonymousChatImpl_ è la classe che implementa i metodi dell'interfaccia _AnonymousChat_ e dove viene utilizzata la libreria TomP2P.
 
 
 - _TestChat_ è la classe contenente il metodo main il quale realizza un'interfaccia command line per utilizzare il sistema. 
 
 