package hw3;

import java.util.List;
import java.util.Random;

/**
 * Customers are simulation actors that have two fields: a name, and a list of
 * Food items that constitute the Customer's order. When running, an customer
 * attempts to enter the coffee shop (only successful if the coffee shop has a
 * free table), place its order, and then leave the coffee shop when the order
 * is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private final int priority;
	private long waitTime;

	private static int runningCounter = 0;

	/**
	 * You can feel free modify this constructor. It must take at least the name and
	 * order but may take other parameters if you would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int priority) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
		this.priority = priority;
	}

	public String toString() {
		return name;
	}

	public long getWaitTime() {
		return waitTime;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * This method defines what an Customer does: The customer attempts to enter the
	 * coffee shop (only successful when the coffee shop has a free table), place
	 * its order, and then leave the coffee shop when the order is complete.
	 */
	public void run() {
		// YOUR CODE GOES HERE...
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		CoffeeShop coffeeShop = CoffeeShop.getInstance();
		coffeeShop.sit(this);
		Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
		Object lock = coffeeShop.placeOrder(orderNum, order, priority);
		Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
		long t = System.currentTimeMillis();
		synchronized (lock) {
			while (!coffeeShop.complete(orderNum)) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		waitTime = System.currentTimeMillis() - t;
		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
		try {
			Thread.sleep(new Random().nextInt(100));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		coffeeShop.leave(this);
	}
}