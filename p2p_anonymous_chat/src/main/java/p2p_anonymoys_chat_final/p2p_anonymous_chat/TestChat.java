package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class TestChat {
	
	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;
	
	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		
		class MessageListenerImpl implements MessageListener{
			int peerId;
			
			public MessageListenerImpl(int peerId) {
				this.peerId = peerId;
			}

			@Override
			public Object parseMessage(Object obj) {
				TextIO textIO = TextIoFactory.getTextIO();
				TextTerminal terminal = textIO.getTextTerminal();
				terminal.printf("\n[" + peerId +"] (DM Received) " + obj + "\n\n");
				return "success";
			}
		}
		
		TestChat testChat = new TestChat();
		final CmdLineParser parser = new CmdLineParser(testChat);
		
		try {
			parser.parseArgument(args);
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			terminal.getProperties().setPromptColor(Color.cyan);
			
			AnonymousChatImpl peer = new AnonymousChatImpl(id, master, new MessageListenerImpl(id));
			String room_name;
			while(true) {
				printMenu(terminal);
				
				int option = textIO.newIntInputReader()
						.withMaxVal(9)
						.withMinVal(0)
						.read("Option: ");
				
				switch (option) {
				case 0:
					terminal.printf("\nARE YOU SURE TO LEAVE NETWORK?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if(exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;
				case 1:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.createRoom(room_name)) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\n SUCCESSFULLY CREATED %s ROOM \n", room_name);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n THIS ROOM ALREADY EXIST \n"));
					break;
					
				case 2:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-secret-room")
							.read("Name: ");
					String password = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Password: ");
					terminal.printf("\nENTER PASSWORD\n");
					String password__1 = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Rewrite password: ");
					if(password.equals(password__1)) {
						if(peer.createSecretRoom(room_name, password)) {
							terminal.getProperties().setPromptColor(Color.GREEN);
							terminal.printf("\nSUCCESSFULLY CREATED %s SECRET ROOM\n", room_name);
							terminal.getProperties().setPromptColor(Color.cyan);
						}else
							terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n THIS ROOM ALREADY EXIST \n"));
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\nPASSWORD ERROR\n"));
					break;
				case 3:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.joinRoom(room_name)) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\nSUCCESSFULLY ENTERED IN %s ROOM\n", room_name);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else 
						terminal.executeWithPropertiesConfigurator(
							props -> ((TerminalProperties) props).setPromptColor("red"),
							t -> ((TextTerminal) t).println("\n THIS ROOM DOESN'T EXIST OR YOU ARE IN THAT ALREDY \n"));
					break;
				case 4:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					terminal.printf("\nENTER PASSWORD\n");
					String psw = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Password: ");
					if(peer.joinSecretRoom(room_name, psw)) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\nSUCCESSFULLY ENTERED IN %s SECRET ROOM\n", room_name);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n THIS SECRET ROOM DOESN'T EXIST OR PASSWORD IS WRONG OR YOU ARE IN THAT ALREADY \n"));
					break;
				case 5:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.leaveRoom(room_name)) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\nSUCCESSFULLY LEAVED %s ROOM\n", room_name);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else 
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n YOU AREN'T IN THIS ROOM \n"));
					break;
				case 6:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					terminal.printf("\nENTER MESSAGE\n");
					String message = textIO.newStringInputReader()
							.withDefaultValue("Hello there!")
							.read("Message: ");
					GregorianCalendar dt = new GregorianCalendar();
					String dt_time = dt.get(Calendar.DAY_OF_MONTH) + "/" + dt.get(Calendar.MONTH)+1 + "/" + dt.get(Calendar.YEAR) + " " + dt.get(Calendar.HOUR_OF_DAY) + ":" + dt.get(Calendar.MINUTE);
					
					if(peer.sendMessage(room_name, dt_time + " to room [" + room_name + "]--> " + message)) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\n SUCCESSFULLY SENT MESSAGE TO %s ROOM\n ", room_name);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n ERROR WHILE SENDIM MESSAGE \n"));
			
					break;
				case 7:
					ArrayList<String> rooms = peer.listRooms();
					if(rooms != null) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\nYOU ARE READING ON ROOMS: \n");
						for(String room: rooms) {
							terminal.printf("\n" + room + "\n");
						}
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n YOU AREN'T READING ON ANY ROOM\n"));
					break;
					
				case 8:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					ArrayList<String> backup = peer.getRoomBackup(room_name);
					if(backup != null) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						for(String msg: backup)
							terminal.printf("\n" + msg + "\n");
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
							props -> ((TerminalProperties) props).setPromptColor("red"),
							t -> ((TextTerminal) t).println("\n THIS ROOM DOSN'T EXIST OR YOU AREN'T IN THIS ROOM\n"));
					break;
				case 9:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default_room")
							.read("Name: ");
					int num_of_peers = peer.getPeersInRoom(room_name);
					if(num_of_peers != -1) {
						terminal.getProperties().setPromptColor(Color.GREEN);
						terminal.printf("\nNUMBER OF PEERS ACTIVE ON %s ROOM: %d\n", room_name, num_of_peers);
						terminal.getProperties().setPromptColor(Color.cyan);
					}else
						terminal.executeWithPropertiesConfigurator(
								props -> ((TerminalProperties) props).setPromptColor("red"),
								t -> ((TextTerminal) t).println("\n THIS ROOM DOESN'T EXIST OR YOU AREN'T IN THIS ROOM\n"));
					break;
				default:
					break;
				}
			}
			
		}catch (CmdLineException clEx) {
			System.out.println("ERROR: Unable to parse command-line optins: " + clEx);
			clEx.printStackTrace();
		}

	}
	
	@SuppressWarnings("rawtypes")
	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE CHAT ROOM\n");
		terminal.printf("\n2 - CREATE SECRET CHAT ROOM\n");
		terminal.printf("\n3 - JOIN TO CHAT ROOM\n");
		terminal.printf("\n4 - JOIN TO SECRET CHAT ROOM\n");
		terminal.printf("\n5 - EXIT FROM CHAT ROOM\n");
		terminal.printf("\n6 - SEND MESSAGE ON ROOM\n");
		terminal.printf("\n7 - VIEW LIST OF YUOURS CHATS\n");
		terminal.printf("\n8 - SHOW BACKUP OF A CHAT ROOM\n");
		terminal.printf("\n9 - SHOW HOW MANY PEERS ARE ACTIVE IN A ROOM\n");
		
		terminal.printf("\n0 - EXIT\n");
	}

}
