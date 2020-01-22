package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import java.util.ArrayList;

public interface AnonymousChat {
	
	public boolean createRoom(String _room_name);
	
	public boolean createSecretRoom(String _room_name, String _password);
	
	public boolean joinRoom(String _room_name);
	
	public boolean joinSecretRoom(String _room_name, String _password);
	
	public boolean leaveRoom(String _room_name);
	
	public boolean sendMessage(String _room_name, String _text_message);
	
	public int getPeersInRoom(String _room_name);
	
	public ArrayList<String> listsRoom();

	public ArrayList<String> getRoomBackup(String _room_name);
	
	public boolean leaveNetwork();
	

}
