package hw3;

/**
 * A Machine is used to make a particular Food. Each Machine makes just one kind
 * of Food. Each machine has a capacity: it can make that many food items in
 * parallel; if the machine is asked to produce a food item beyond its capacity,
 * the requester blocks. Each food item takes at least item.cookTimeMS
 * milliseconds to produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;

	// YOUR CODE GOES HERE...
	public final int machineCapacity;
	public int cooking;

	/**
	 * The constructor takes at least the name of the machine, the Food item it
	 * makes, and its capacity. You may extend it with other arguments, if you wish.
	 * Notice that the constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever initialization etc.
	 * you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;

		// YOUR CODE GOES HERE...
		this.machineCapacity = capacityIn;
		
		Simulation.logEvent(SimulationEvent.machineStarting(this, foodIn, machineCapacity));
	}

	/**
	 * This method is called by a Cook in order to make the Machine's food item. You
	 * can extend this method however you like, e.g., you can have it take extra
	 * parameters or return something other than Object. It should block if the
	 * machine is currently at full capacity. If not, the method should return, so
	 * the Cook making the call can proceed. You will need to implement some means
	 * to notify the calling Cook when the food item is finished.
	 */
	public Thread makeFood() throws InterruptedException {
		// YOUR CODE GOES HERE...
		Thread thread = new Thread(new CookAnItem(this));
		thread.start();
		return thread;
	}

	public boolean available() {
		synchronized (this) {
			return cooking < machineCapacity;
		}
	}

	// THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		Machine machine;

		public CookAnItem(Machine machine) {
			this.machine = machine;
		}

		public void run() {
			Food food = machine.machineFoodType;
			synchronized (machine) {
				while (!available()) {
					try {
						// YOUR CODE GOES HERE...
						machine.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Simulation.logEvent(SimulationEvent.machineCookingFood(machine, food));
				cooking++;
			}
			try {
				Thread.sleep(food.cookTimeMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (machine) {
				cooking--;
				machine.notifyAll();
				Simulation.logEvent(SimulationEvent.machineDoneFood(machine, food));
			}
		}
	}

	public String toString() {
		return machineName;
	}
}