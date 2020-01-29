package distibuted_system.p2p_anonymous_chat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class AnonymousChatImpl implements AnonymousChat {
	
	final private Peer peer;
	final private PeerDHT dht;
	final private int DEFAULT_MASTER_PORT = 4000;
	
	final private ArrayList<String> chat_rooms = new ArrayList<String>();
	//Struttura che tiene traccia dei Nickname da usare nelle varie Room a cui si è iscritti.
	final private HashMap<String, String> nick_map = new HashMap<String, String>();
	
	/**
	 * Nel costruttore viene inizializzato il peer e viene associato alla dht.
	 * @param
	 * 		_id: rappresenta l'id del peer.
	 * 		_master_peer: è l'indirizzo ip del peer noto della rete, il master peer.
	 * 		_listener: rappresenta un'istanza della classe MessageListener per la notifica dei messaggi ricevuti.
	 */
	public AnonymousChatImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
		
		peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		dht = new PeerBuilderDHT(peer).start();
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		
		if (fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in a master peer bootstrap.");
		}
		
		peer.objectDataReply(new ObjectDataReply() {
			
			@Override
			public Object reply(PeerAddress sender, Object request) throws Exception {
				
				return _listener.parseMessage(request);
			}
		});
	}
	
	/**
	 * Questo metodo ci consente di creare una Room a cui gli altri peer potranno iscriversi.
	 * 
	 * @param
	 * 		_room_name: rappresenta il nome da assegnare alla Room.
	 * @return	
	 * 		true: se la room viene effettivamente creata.
	 * 		false: se siamo già iscritti ad una room con lo stesso nome, che sia pubblica o privata;
	 * 			   se esiste una room segreta con lo stesso nome;
	 * 			   se esiste una room pubblica con lo stesso nome; 		
	 */
	@Override
	public boolean createRoom(String _room_name) {
		try {
			
			//controlliamo se il room name è già nella nostra chat list
			if(this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				
				/**
				 * Questo passaggio ci consente di evitare di creare una room pubblica con lo 
				 * stesso nome di una room segreta. Se la room segreta corrispondente al nome corrente 
				 * non è vuota non possiamo creare questa room.
				 */
				if(!_room_name.contains("_secret")) {
					FutureGet futureGet_control = dht.get(Number160.createHash(_room_name+"_secret")).start();
					futureGet_control.awaitUninterruptibly();
					if(!futureGet_control.isEmpty())
						return false;
				}
				
				//Controlliamo che non esista già una room a cui non siamo iscritti con lo stesso nome.
				if (futureGet.isSuccess() && futureGet.isEmpty()) {
					
					//A questo punto la room può essere creata!
					//Il peer deve essere automaticamente inserito in questa room in quanto è il creatore.
					HashSet<PeerAddress> peers_in_room = new HashSet<PeerAddress>();
					peers_in_room.add(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					chat_rooms.add(_room_name);
					
					//Associo un nickname randomico da usare all'interno della room per questa iscrizione.
					nick_map.put(_room_name, this.generateNickname());
					this.sendMessage(_room_name, "I'm creator of this room [" + nick_map.get(_room_name) + "]!");
					return true;				
				}
				return false;
			}		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *Questo metodo ci consente di creare una room segreta a cui è associata una password, 
	 *solo i peer che sono a conoscenza anche della password possono accedervi.
	 *
	 *@param
	 *		_room_name: rappresenta il nome da assegnare alla room.
	 *		_password: rappresenta la password da associare a questa room.
	 *@return 
	 *		true: se la room viene effettivamente creata.
	 *		false: se siamo già iscritti ad una room con lo stesso nome, che sia pubblica o privata;
	 *			   se esiste una room pubblica con lo stesso nome;
	 *			   se il metodo createRoom restituisce false.			   
	 */
	@Override
	public boolean createSecretRoom(String _room_name, String _password) {
		try {
			
			//Controlliamo se il room name è già nella nostra chat list
			if(this.parseName(_room_name) == null) {
				//Viene creata un'entità all'interno della dht che conserverà la password associata a questa room
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					dht.put(Number160.createHash(_room_name+"_psw")).data(new Data(_password)).start().awaitUninterruptibly();
					
					//questa sezione evita che venga creata una room segreta con lo stesso nome di una room pubblica
					FutureGet futureGet_control = dht.get(Number160.createHash(_room_name)).start();
					futureGet_control.awaitUninterruptibly();
					if(futureGet_control.isSuccess() && futureGet_control.isEmpty())
						
						//deleghiamo la creazione della room al metodo createRoom
						if(this.createRoom(_room_name + "_secret"))
							return true;
				}else
					return false;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Questo metodo ci consente di entrare in una chat room pubblica di cui è noto il nome
	 * @param
	 * 		_room_name: rappresenta il nome della room a cui accedere
	 * @return
	 * 		true: se la si riesce ad accedere alla room.
	 * 		false: se il peer è già iscritto alla room;
	 * 			   se la room non esiste.
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean joinRoom(String _room_name) {
		try {
			
			//Controlliamo se il room name è già nella nostra chat list
			if (this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					
					//se isEmpty() ritorna true significa che la room a cui vogliamo accedere non esiste
					if(futureGet.isEmpty())
						return false;
					
					//Aggiungiamo questo peer a quelli già presenti nella room
					HashSet<PeerAddress> peers_in_room;
					peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					peers_in_room.add(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					
					//il nome della room viene aggiunto alla lista
					chat_rooms.add(_room_name);
					//viene generato un nickname randomico da usare nella room
					nick_map.put(_room_name, this.generateNickname());
					//viene inviato un messaggio alla room.
					this.sendMessage(_room_name, "[room: " + _room_name + "] Hello There [" + nick_map.get(_room_name) + "]!");
					return true;
				}				
			}			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;		
	}

	/**
	 * Questo metodo ci consente di accedere ad una chat room segreta fornendo il nome e la password.
	 * 
	 * @param
	 * 		_room_name: rappresenta il nome della room segreta a cui si vuole accedere.
	 * 		_password: rappresenta la password da utilizzare per accedere alla room.
	 * @return
	 */
	@Override
	public boolean joinSecretRoom(String _room_name, String _password) {
		try {
			
			//Controlliamo se il room name è già nella nostra chat list
			if(this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
				futureGet.awaitUninterruptibly();
				
				//Cerchiamo di ricavare la password della room dalla dht per fare un confronto con quella fornita in input
				if(futureGet.isSuccess()) {
					
					//Se isEmpty() ritorna true significa che la room non è mai stata creata
					if(futureGet.isEmpty())
						return false;
					String effective_psw = (String) futureGet.dataMap().values().iterator().next().object();
					
					//Se la password fornita in input e quella reale corrispondono si può accedere alla room segreta
					if(_password.equals(effective_psw)) {
						this.joinRoom(_room_name + "_secret");
						return true;
					}else
						return false;				
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Questo metodo ci consente di uscire da una chat room.
	 * 
	 * @param
	 * 		_room_name: rappresenta il nome della room dalla quale vogliamo uscire
	 * @return
	 * 		true: se l'uscita ha successo
	 * 		false: se non siamo iscritti alla room corrispondente al nome fornito come parametro;
	 * 			   
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean leaveRoom(String _room_name) {
		try {
			
			//Viene fatto un parsing del name in input in caso la room in questione sia segreta.
			_room_name = this.parseName(_room_name);
			
			//Se parseName ritorna null non siamo iscritti alla room in questione
			if(_room_name != null) {				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					/**
					 * Se questo peer è l'ultimo all'interno della room, la room deve essere distrutta, o meglio 
					 * rimossa dalla dht al fine di poter ricreare una room con lo stesso nome in futuro.
					 * Insieme ad essa devono essere rimosse dalla dht anche le entità che conservano il backup della chat
					 * e l'eventuale password in caso si tratti di una room segreta.
					 */
					if(this.getPeersInRoom(_room_name) == 1) {
						FutureRemove futureRemove_s = dht.remove(Number160.createHash(_room_name)).start();
						futureRemove_s.awaitUninterruptibly();
						FutureRemove futureRemove_b = dht.remove(Number160.createHash(_room_name+"_backup")).start();
						futureRemove_b.awaitUninterruptibly();
						FutureRemove futureRemove_p = dht.remove(Number160.createHash(_room_name+"_psw")).start();
						futureRemove_p.awaitUninterruptibly();
						if(futureRemove_s.isSuccess() && futureRemove_b.isSuccess() && futureRemove_p.isSuccess()) {
							chat_rooms.remove(_room_name);
							nick_map.remove(_room_name);
							return true;
						}
					}
	
					this.sendMessage(_room_name, "[" + _room_name + "] -- leaving this room -- [" + nick_map.get(_room_name) + "]!");
					
					//Se questo peer non è l'unico all'interno della room, viene semplicemente rimosso dalla lista dei peer della room.
					HashSet<PeerAddress> peers_in_room;
					peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					peers_in_room.remove(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					chat_rooms.remove(_room_name);
					nick_map.remove(_room_name);
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Questo metodo ci consente di inviare un messaggio in una room a cui si è iscritti.
	 * 
	 * @param
	 * 		_room_name: rappresenta il nome della room in cui si vuole inviare un messaggio.
	 * 		_text_message:	rappresenta il messaggio da inviare.
	 * @return
	 * 		true: se il messaggio viene inviato con successo e ne viene fatto anche il backup
	 * 		false: se il nome della room in input non corrisponde a nessuna room a cui si è iscritti
	 * 			oppure ad una room che non esiste.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMessage(String _room_name, String _text_message) {
		boolean flag_1 = false;
		boolean flag_2 = false;
		
		try {
			
			//Viene fatto un parsing del name in input in caso la room in questione sia segreta.
			_room_name = this.parseName(_room_name);
			//se parseName ritorna null la room a cui si vuole mandare un messaggio non esiste o non vi abbiamo accesso
			if(_room_name != null) {
				
				//invio del messaggio a tutti i peer che fanno parte della room
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					HashSet<PeerAddress> peers_in_room;
					peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					for(PeerAddress peer: peers_in_room) {
						FutureDirect futureDirect = dht.peer().sendDirect(peer).object("<<Guest:"+nick_map.get(_room_name)+">> "+_text_message).start();
						futureDirect.awaitUninterruptibly();
					}
					flag_1 = true;
				}
				
				/**
				 * Viene creata un'entità a parte nella dht che servirà a conservare il backup della chat 
				 */
				FutureGet futureGet_b = dht.get(Number160.createHash(_room_name+"_backup")).start();
				futureGet_b.awaitUninterruptibly();
				ArrayList<String> allChat = null;
				if(futureGet_b.isSuccess()) {
					
					//se non è stato ancora inviato nessun messaggio nella room, l'entità backup non esiste ancora 
					if(futureGet_b.isEmpty())
						allChat = new ArrayList<String>();
					else
						allChat = (ArrayList<String>) futureGet_b.dataMap().values().iterator().next().object();						
				}
				allChat.add("<<Guest:"+nick_map.get(_room_name)+">> "+_text_message);
				dht.put(Number160.createHash(_room_name+"_backup")).data(new Data(allChat)).start().awaitUninterruptibly();
				flag_2 = true;
				if(flag_1 && flag_2){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;		
	}

	/**
	 * Questo metodo ci consente di verificare quanti peers sono attivi in una room di cui siamo parte.
	 * 
	 *@param
	 *		_room_name: rappresenta il nome della room di cui vogliamo informazioni.
	 *@return
	 *		_num_of_peers: il numero dei peer attivi che fanno parte della room in questione.
	 *		-1: se la room non esiste o non vi abbiamo accesso.
	 */
	@Override
	public int getPeersInRoom(String _room_name) {
		try {
			
			//Viene fatto un parsing del name in input in caso la room in questione sia segreta.
			_room_name = this.parseName(_room_name);
			//se parseName restituisce null questo peer non è iscritto alla room.
			if(_room_name != null) {
				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					
					//se isEmpty restituisce true la room non esiste
					if(futureGet.isEmpty())
						return -1;
					
					//ricaviamo quanti peers fanno parte della room
					@SuppressWarnings("unchecked")
					HashSet<PeerAddress> peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					return peers_in_room.size();
				}
				return -1;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Questo metodo ci consente di ottenere tutti i messaggi inviati ad una room a cui siamo iscritti.
	 * 
	 * @param
	 * 		_room_name: nome della room di cui vogliamo ottenere il backup.
	 * @return
	 * 		_backup: ArrayList<String> contenente tutti i messaggi inviati sulla room.
	 * 		null se la room non esiste o non vi siamo iscritti.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<String> getRoomBackup(String _room_name) {
		try {
			//Viene fatto un parsing del name in input in caso la room in questione sia segreta.
			_room_name = this.parseName(_room_name);
			//se parseName restituisce null questo peer non è iscritto alla room.
			if(_room_name != null) {
				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_backup")).start();
				futureGet.awaitUninterruptibly();
				ArrayList<String> allChat = null;
				if(futureGet.isSuccess()) {
					allChat = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
					return allChat;
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Questo metodo ci consente di ottenere la lista di tutte le room a cui siamo iscritti.
	 * 
	 *@return
	 *		null se la lista delle chat è vuota;
	 *		chat_list.
	 */
	@Override
	public ArrayList<String> listRooms() {
		try {
			if(chat_rooms.size() == 0)
				return null;
			return chat_rooms;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Questo metodo si occupa di abbandonare tutte le room a cui siamo iscritti 
	 * e di fare lo shutdown del peer.	 * 
	 */
	@Override
	public boolean leaveNetwork() {
		try {
			for(String room: new ArrayList<String>(chat_rooms))
				leaveRoom(room);
			dht.peer().announceShutdown().start().awaitUninterruptibly();
			dht.peer().shutdown();
			return true;			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Questo metodo genera un nickname randomico formato da 2 caratteri e 4 numeri divisi da un trattino.
	 * 
	 * @return
	 * 		nickname
	 */
	private String generateNickname() {
		String nickname;
		Random rnd = new Random();
		nickname = ((char)(rnd.nextInt(57)+65)) + "" + ((char)(rnd.nextInt(57)+65)) + "-" + rnd.nextInt(10000);
		
		return nickname;
	}
	
	/**
	 * Questo metodo si occupa di controllare se il nome fornito in input corrisponde ad una room (segreta o pubblica) 
	 * a cui siamo già iscritti e nel caso sia una room segreta aggiunge al nome "_secret".
	 * 
	 *@param
	 *		_room_name: rappresenta il nome della room.
	 *@return
	 *		_room_name se siamo inscritti alla room ed è pubblica
	 *		_room_name_secret se siamo iscritti alla room ed è segreta
	 *		null se non siamo iscritti alla room.
	 */
	private String parseName(String _room_name) {
		if(chat_rooms.contains(_room_name))
			return _room_name;
		else if(chat_rooms.contains(_room_name+"_secret"))
			return _room_name.concat("_secret");
		else
			return null;
	}
}
