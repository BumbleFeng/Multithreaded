package hw3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Cooks are simulation actors that have at least one field, a name. When
 * running, a cook attempts to retrieve outstanding orders placed by Eaters and
 * process them.
 */
public class Cook implements Runnable {
	private final String name;

	/**
	 * You can feel free modify this constructor. It must take at least the name,
	 * but may take other parameters if you would find adding them useful.
	 *
	 * @param: the
	 *             name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows. The cook tries to retrieve orders placed by
	 * Customers. For each order, a List<Food>, the cook submits each Food item in
	 * the List to an appropriate Machine, by calling makeFood(). Once all machines
	 * have produced the desired Food, the order is complete, and the Customer is
	 * notified. The cook can then go to process the next order. If during its
	 * execution the cook is interrupted (i.e., some other thread calls the
	 * interrupt() method on it, which could raise InterruptedException if the cook
	 * is blocking), then it terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while (true) {
				// YOUR CODE GOES HERE...
				CoffeeShop coffeeShop = CoffeeShop.getInstance();
				int orderNum;
				synchronized (coffeeShop.getNewOrders()) {
					while (!coffeeShop.newOrder()) {
						coffeeShop.getNewOrders().wait();
					}
					orderNum = coffeeShop.next();
				}
				cook(orderNum);
			}
		} catch (InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}

	public void cook(int orderNum) throws InterruptedException {
		CoffeeShop coffeeShop = CoffeeShop.getInstance();
		List<Food> order = coffeeShop.getOrder(orderNum);
		Object lock = coffeeShop.getOrderLock(orderNum);
		synchronized (lock) {
			Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNum));

			int[] count = new int[3];
			for (Food food : order) {
				switch (food.name) {
				case "burger":
					count[0]++;
					break;
				case "fries":
					count[1]++;
					break;
				case "coffee":
					count[2]++;
					break;
				}
			}
			HashMap<Integer, List<Thread>> cookThreads = new HashMap<>();

			for (int i = 0; i < 3; i++) {
				Machine machine = coffeeShop.getMachines().get(i);
				List<Thread> machineThreads = new LinkedList<>();
				cookThreads.put(i, machineThreads);
				for (int j = 0; j < count[i]; j++) {
					Simulation.logEvent(SimulationEvent.cookStartedFood(this, machine.machineFoodType, orderNum));
					Thread thread = machine.makeFood();
					machineThreads.add(thread);
				}
			}

			for (int i : cookThreads.keySet()) {
				Machine machine = coffeeShop.getMachines().get(i);
				List<Thread> machineThreads = cookThreads.get(i);
				for (Thread thread : machineThreads) {
					thread.join();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(this, machine.machineFoodType, orderNum));
				}
			}

			coffeeShop.completed(orderNum);
			Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, orderNum));
		}
	}
}