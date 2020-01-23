package p2p_anonymoys_chat_final.p2p_anonymous_chat;

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
	final private HashMap<String, String> nick_map = new HashMap<String, String>();
	
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

	@Override
	public boolean createRoom(String _room_name) {
		try {
			if(this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if (futureGet.isSuccess() && futureGet.isEmpty()) {
					HashSet<PeerAddress> peers_in_room = new HashSet<PeerAddress>();
					peers_in_room.add(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					chat_rooms.add(_room_name);
					nick_map.put(_room_name, this.generateNickname());
					this.sendMessage(_room_name, "I'm creator of this room!");
					return true;				
				}
				return false;
			}		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean createSecretRoom(String _room_name, String _password) {
		try {
			if(this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isEmpty()) System.out.println("ho successo!!!");
				if(futureGet.isSuccess()) {
					dht.put(Number160.createHash(_room_name+"_psw")).data(new Data(_password)).start().awaitUninterruptibly();
					this.createRoom(_room_name + "_secret");
					return true;
				}else
					return false;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean joinRoom(String _room_name) {
		try {
			if (this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					if(futureGet.isEmpty())
						return false;
					HashSet<PeerAddress> peers_in_room;
					peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					peers_in_room.add(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					chat_rooms.add(_room_name);
					nick_map.put(_room_name, this.generateNickname());
					this.sendMessage(_room_name, "Hello There!");
					return true;
				}				
			}else
				return false;			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;		
	}

	@Override
	public boolean joinSecretRoom(String _room_name, String _password) {
		try {
			if(this.parseName(_room_name) == null) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					if(futureGet.isEmpty())
						return false;
					String effective_psw = (String) futureGet.dataMap().values().iterator().next().object();
					if(_password.equals(effective_psw)) {
						this.joinRoom(_room_name + "_secret");
						return true;
					}else
						return false;				
				}
			}else
				return false;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean leaveRoom(String _room_name) {
		try {
			_room_name = this.parseName(_room_name);
			if(_room_name != null) {				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
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
					this.sendMessage(_room_name, "-- leaving this room --");
					HashSet<PeerAddress> peers_in_room;
					peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
					peers_in_room.remove(dht.peer().peerAddress());
					dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
					chat_rooms.remove(_room_name);
					nick_map.remove(_room_name);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMessage(String _room_name, String _text_message) {
		boolean flag_1 = false;
		boolean flag_2 = false;
		
		try {
			_room_name = this.parseName(_room_name);
			if(_room_name != null) {
				
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
				
				/*
				 * parte relativa al backup
				 */
				FutureGet futureGet_b = dht.get(Number160.createHash(_room_name+"_backup")).start();
				futureGet_b.awaitUninterruptibly();
				ArrayList<String> allChat = null;
				if(futureGet_b.isSuccess()) {
					if(futureGet_b.isEmpty())
						allChat = new ArrayList<String>();
					else
						allChat = (ArrayList<String>) futureGet_b.dataMap().values().iterator().next().object();
					flag_2 = true;						
				}
				allChat.add("<<Guest:"+nick_map.get(_room_name)+">> "+_text_message);
				dht.put(Number160.createHash(_room_name+"_backup")).data(new Data(allChat)).start().awaitUninterruptibly();
				if(flag_1 && flag_2){
					return true;
				}
			}else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;		
	}

	@Override
	public int getPeersInRoom(String _room_name) {
		try {
			_room_name = this.parseName(_room_name);
			if(_room_name != null) {
				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					if(futureGet.isEmpty())
						return -1;
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

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<String> getRoomBackup(String _room_name) {
		try {
			_room_name = this.parseName(_room_name);
			if(_room_name != null) {
				
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_backup")).start();
				futureGet.awaitUninterruptibly();
				ArrayList<String> allChat = null;
				if(futureGet.isSuccess()) {
					allChat = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
					return allChat;
				}				
			}else 
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public ArrayList<String> listsRoom() {
		try {
			if(chat_rooms.size() == 0)
				return null;
			return chat_rooms;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean leaveNetwork() {
		for(String room: new ArrayList<String>(chat_rooms))
			leaveRoom(room);
		dht.peer().announceShutdown().start().awaitUninterruptibly();
		return false;
	}
	
	private String generateNickname() {
		String nickname;
		Random rnd = new Random();
		nickname = ((char)(rnd.nextInt(57)+65)) + "" + ((char)(rnd.nextInt(57)+65)) + "-" + rnd.nextInt(10000);
		
		return nickname;
	}
	
	private String parseName(String _room_name) {
		if(chat_rooms.contains(_room_name))
			return _room_name;
		else if(chat_rooms.contains(_room_name+"_secret"))
			return _room_name.concat("_secret");
		else
			return null;
	}



}
