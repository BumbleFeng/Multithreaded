package hw5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * this actor reads the file, counts the vowels and sends the result to
 * Estimator.
 *
 * @author akashnagesh
 *
 */
public class Counter extends UntypedActor {

	static Props props = Props.create(Counter.class);

	private Map<File, Double> testList;

	public Counter() {
		testList = new ConcurrentHashMap<>();
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof Messages) {
			Messages message = (Messages) msg;
			File text = message.getText();
			Double p = testList.get(text);
			if (p == null) {
				p = vowelsPercentage(text);
				testList.put(text, p);
				System.out.println(text.getName() + ":" + p);
			}
			Double u = p - message.getC();
			getSender().tell(u, null);
		} else {
			unhandled(msg);
		}
	}

	public double vowelsPercentage(File text) {
		int v = 0, l = 0;
		try {
			FileReader fr = new FileReader(text);
			BufferedReader in = new BufferedReader(fr);
			String string;
			while ((string = in.readLine()) != null) {
				string = string.toUpperCase();
				for (int i = 0; i < string.length(); i++) {
					if (Character.isLetter(string.charAt(i))) {
						l++;
						switch (string.charAt(i)) {
						case 'A':
							v++;
							break;
						case 'E':
							v++;
							break;
						case 'I':
							v++;
							break;
						case 'O':
							v++;
							break;
						case 'U':
							v++;
							break;
						case 'Y':
							v++;
							break;
						}
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (v + 0.0) / l;
	}
}
