package hw5;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * This is the main actor and the only actor that is created directly under the
 * {@code ActorSystem} This actor creates more child actors
 * {@code WordCountInAFileActor} depending upon the number of files in the given
 * directory structure
 * 
 * @author akashnagesh
 *
 */
public class Estimator extends UntypedActor {

	public static Props props(double cStrat, double g) {
		return Props.create(Estimator.class, cStrat, g);
	}

	private List<Double> c;
	private List<Double> u;
	private double g;

	public Estimator(double cStrat, double g) {
		c = new ArrayList<>();
		u = new ArrayList<>();
		c.add(cStrat);
		this.g = g;
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof File) {
			int t = c.size() - 1;
			File text = (File) msg;
			Messages req = new Messages(text, c.get(t));
			Future<Object> fmsg = Patterns.ask(getSender(), req, 1000);
			try {
				Double res = (Double) Await.result(fmsg, Duration.Inf());
				u.add(res);
			} catch (Exception e) {
				System.out.println("Error in get()");
			}
			Double p = c.get(t) + u.get(t);
			Double newC = g * c.get(t) + (1 - g) * p;
			c.add(newC);
			ActorRef user = getContext().parent();
			user.tell(result(), null);
		}
	}
	
	public String result() {
		int t = u.size() - 1;
		int step = u.size();
		StringBuffer result = new StringBuffer();
		result.append(getSelf().path().name() + ":");
		result.append("C(" + step + ")=" + c.get(t) + " ");
		result.append("U(" + step + ")=" + u.get(t) + " ");
		result.append("C(" + (step + 1) + ")=" + c.get(t + 1) + " ");
		Double s = 0.0;
		for (Double d : u) {
			s += d;
		}
		s /= u.size();
		result.append("S=" + s + " ");
		return result.toString();
	}
}
