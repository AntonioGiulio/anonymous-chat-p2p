package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import java.util.HashMap;
import java.util.Random;

public class DumbTest {

	public static void main(String[] args) {
		Random rnd = new Random();
		
		
		String s = ((char)(rnd.nextInt(57)+65)) + "" + ((char)(rnd.nextInt(57)+65)) + "-" + rnd.nextInt(10000);
		char c=(char) (rnd.nextInt(57)+65);  
		System.out.println(s);
		
	}

}
