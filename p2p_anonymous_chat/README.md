# Anonymous Chat System on p2p network implemented with Tomp2p
### Architetture distribuite per il Cloud
### Università degli Studi di Salerno - Anno Accademico 2019/2020
### Prof. Alberto Negro
### Prof. Gennaro Cordasco
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
 
Il pacchetto ``src/test/java/distributed_system/p2p_anonymous_chat`` contiene tutte le classi di test che utilizzano la libreria JUnit, una per ogni metodo dell'interfaccia _AnonymousChat_

# 
### Dockerization
L'applicazione può essere eseguita in locale attraverso un container Docker.
 
Nella root directory del progetto è presente il Dockerfile in cui ci sono tutti i dettagli per costruire l'immagine Docker.
##### Come fare la build in Docker
Per prima cosa bisogna posizionarsi nella root del progetto "p2p\_anonymous\_chat" dove è presente il Dockerfile, successivamente si può costruire l'immagine eseguendo il comando:

``docker build --no-cache -t p2p-an-chat .``

Alternativamente a tutta la fase di build è possibile scaricare un'immagine Docker già fatta al link

https://hub.docker.com/r/antoniogiulio/p2p-anonymous-chat

o digitando il comando:

``docker pull antoniogiulio/p2p-anonymous-chat``
##### Come lanciare il master peer
Arrivati a questo punto bisogna lanciare un container istanza dell'immagine Docker appena creata che fungerà da master peer.
Bisogna lanciare il container in modalità interattiva con l'opzione -i e con due parametri:

``docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 p2p-an-chat``

La variabile MASTERIP rappresenta l'indirizzo IP del master, in questo caso localhost, e la variabile ID è l'id univoco del peer, nel caso del master è 0.

##### Come lanciare un peer generico
Dopo aver lanciato il master peer bisogna controllare l'id assegnatogli per lanciare altri peer. Bisogna eseguire il comando ``docker ps`` per visualizzare i container in esecuzione e poi ``docker inspect <masterContainer ID>`` per ottenere l'IP Address. 

Ora possiamo lanciare altri peer con il comando:

``docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 p2p-an-chat``


