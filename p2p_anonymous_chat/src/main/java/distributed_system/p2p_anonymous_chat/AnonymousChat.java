package distributed_system.p2p_anonymous_chat;

import java.util.ArrayList;

public interface AnonymousChat {
	
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
	public boolean createRoom(String _room_name);
	
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
	public boolean createSecretRoom(String _room_name, String _password);
	
	/**
	 * Questo metodo ci consente di entrare in una chat room pubblica di cui è noto il nome
	 * @param
	 * 		_room_name: rappresenta il nome della room a cui accedere
	 * @return
	 * 		true: se la si riesce ad accedere alla room.
	 * 		false: se il peer è già iscritto alla room;
	 * 			   se la room non esiste.
	 */
	public boolean joinRoom(String _room_name);
	
	/**
	 * Questo metodo ci consente di accedere ad una chat room segreta fornendo il nome e la password.
	 * 
	 * @param
	 * 		_room_name: rappresenta il nome della room segreta a cui si vuole accedere.
	 * 		_password: rappresenta la password da utilizzare per accedere alla room.
	 * @return
	 */
	public boolean joinSecretRoom(String _room_name, String _password);
	
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
	public boolean leaveRoom(String _room_name);
	
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
	public boolean sendMessage(String _room_name, String _text_message);
	
	/**
	 * Questo metodo ci consente di verificare quanti peers sono attivi in una room di cui siamo parte.
	 * 
	 *@param
	 *		_room_name: rappresenta il nome della room di cui vogliamo informazioni.
	 *@return
	 *		_num_of_peers: il numero dei peer attivi che fanno parte della room in questione.
	 *		-1: se la room non esiste o non vi abbiamo accesso.
	 */
	public int getPeersInRoom(String _room_name);
	
	/**
	 * Questo metodo ci consente di ottenere la lista di tutte le room a cui siamo iscritti.
	 * 
	 *@return
	 *		null se la lista delle chat è vuota;
	 *		chat_list.
	 */
	public ArrayList<String> listRooms();

	/**
	 * Questo metodo ci consente di ottenere tutti i messaggi inviati ad una room a cui siamo iscritti.
	 * 
	 * @param
	 * 		_room_name: nome della room di cui vogliamo ottenere il backup.
	 * @return
	 * 		_backup: ArrayList<String> contenente tutti i messaggi inviati sulla room.
	 * 		null se la room non esiste o non vi siamo iscritti.
	 */
	public ArrayList<String> getRoomBackup(String _room_name);
	
	/**
	 * Questo metodo si occupa di abbandonare tutte le room a cui siamo iscritti 
	 * e di fare lo shutdown del peer.	 * 
	 */
	public boolean leaveNetwork();
	

}
