package hw5;

import java.io.File;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Main class for your estimation actor system.
 *
 * @author akashnagesh
 *
 */
public class User extends UntypedActor {

	static Props props = Props.create(User.class);

	public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create("EstimationSystem");

		/*
		 * Create the Estimator Actor and send it the StartProcessingFolder message.
		 * Once you get back the response, use it to print the result. Remember, there
		 * is only one actor directly under the ActorSystem. Also, do not forget to
		 * shutdown the actorsystem
		 */

		ActorRef user = system.actorOf(User.props, "User");
		String StartProcessingFolder = "Akka_Text/";
		File folder = new File(StartProcessingFolder);
		if (folder.isDirectory()) {
			user.tell(folder, null);
		}

		try {
			Thread.sleep(1000);  // Sleep long enough to ensure delivery of
								// messages before shutting down
		} catch (InterruptedException e) {
		}
		system.terminate();
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof File) {
			File folder = (File) msg;
			File[] texts = folder.listFiles();
			ActorContext context = getContext();
			ActorRef counter = context.actorOf(Counter.props, "Counter");
			ActorRef estimator1 = context.actorOf(Estimator.props(0.5, 0.8), "Estimator1");
			ActorRef estimator2 = context.actorOf(Estimator.props(0.5, 0.01), "Estimator2");
			for (File text : texts) {
				if (text.getName().endsWith(".txt")) {
					estimator1.tell(text, counter);
					estimator2.tell(text, counter);
				}
			}
		} else if (msg instanceof String) {
			String string = (String) msg;
			System.out.println(string);
		} 
	}

}
