package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
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
			FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty()) {
				HashSet<PeerAddress> peers_in_room = new HashSet<PeerAddress>();
				peers_in_room.add(dht.peer().peerAddress());
				dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
				chat_rooms.add(_room_name);
				return true;				
			}
			return false;			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean createSecretRoom(String _room_name, String _password) {
		try {
			FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
			futureGet.awaitUninterruptibly();
			if(futureGet.isSuccess() && futureGet.isEmpty()) {
				dht.put(Number160.createHash(_room_name+"_psw")).data(new Data(_password)).start().awaitUninterruptibly();
				this.createRoom(_room_name);
				return true;
			}else
				return false;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean joinRoom(String _room_name) {
		try {
			if (!chat_rooms.contains(_room_name)) {
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
			if(!chat_rooms.contains(_room_name)) {
				FutureGet futureGet = dht.get(Number160.createHash(_room_name+"_psw")).start();
				futureGet.awaitUninterruptibly();
				if(futureGet.isSuccess()) {
					if(futureGet.isEmpty())
						return false;
					String effective_psw = (String) futureGet.dataMap().values().iterator().next().object();
					if(_password.equals(effective_psw)) {
						this.joinRoom(_room_name);
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

	@Override
	public boolean leaveRoom(String _room_name) {
		try {
			FutureGet futureGet = dht.get(Number160.createHash(_room_name)).start();
			futureGet.awaitUninterruptibly();
			if(futureGet.isSuccess()) {
				HashSet<PeerAddress> peers_in_room;
				peers_in_room = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				peers_in_room.remove(dht.peer().peerAddress());
				dht.put(Number160.createHash(_room_name)).data(new Data(peers_in_room)).start().awaitUninterruptibly();
				chat_rooms.remove(_room_name);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean sendMessage(String _room_name, String _text_message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPeersInRoom(String _room_name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<String> getRoomBackup(String _room_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean leaveNetwork() {
		for(String room: chat_rooms)
			leaveRoom(room);
		dht.peer().announceShutdown().start().awaitUninterruptibly();
		return false;
	}

}
