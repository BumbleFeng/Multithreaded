package hw3;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Simulation is the main class used to run the simulation. You may add any
 * fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;

	/**
	 * Used by other classes in the simulation to log events
	 * 
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * Function responsible for performing the simulation. Returns a List of
	 * SimulationEvent objects, constructed any way you see fit. This List will be
	 * validated by a call to Validate.validateSimulation. This method is called
	 * from Simulation.main(). We should be able to test your code by only calling
	 * runSimulation.
	 * 
	 * Parameters:
	 * 
	 * @param numCustomers
	 *            the number of customers wanting to enter the coffee shop
	 * @param numCooks
	 *            the number of cooks in the simulation
	 * @param numTables
	 *            the number of tables in the coffe shop (i.e. coffee shop capacity)
	 * @param machineCapacity
	 *            the capacity of all machines in the coffee shop
	 * @param randomOrders
	 *            a flag say whether or not to give each customer a random order
	 *
	 */
	public static List<SimulationEvent> runSimulation(int numCustomers, int numCooks, int numTables,
			int machineCapacity, boolean randomOrders) {
		if (numCustomers < 1 || numCooks < 1 || numTables < 1 || machineCapacity < 1) {
			System.out.println("Error Parameter");
			return null;
		}
		// This method's signature MUST NOT CHANGE.

		// We are providing this events list object for you.
		// It is the ONLY PLACE where a concurrent collection object is
		// allowed to be used.
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());

		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers, numCooks, numTables, machineCapacity));

		// Set things up you might need
		CoffeeShop coffeeShop = new CoffeeShop(numTables);

		// Start up machines
		coffeeShop.getMachines().put(0, new Machine("Grill", FoodType.burger, machineCapacity));
		coffeeShop.getMachines().put(1, new Machine("Frier", FoodType.fries, machineCapacity));
		coffeeShop.getMachines().put(2, new Machine("Star", FoodType.coffee, machineCapacity));

		// Let cooks in
		Thread[] cooks = new Thread[numCooks];
		for (int i = 0; i < numCooks; i++) {
			cooks[i] = new Thread(new Cook("Cook " + (i + 1)));
		}
		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			order = new LinkedList<Food>();
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			for (int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(new Customer("Customer " + (i + 1), order, i % 3 + 1));
			}
		} else {
			for (int i = 0; i < customers.length; i++) {
				Random rnd = new Random();
				int burgerCount = rnd.nextInt(3);
				int friesCount = rnd.nextInt(3);
				int coffeeCount = rnd.nextInt(3);
				int priority = rnd.nextInt(3) + 1;
				order = new LinkedList<Food>();
				for (int b = 0; b < burgerCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int c = 0; c < coffeeCount; c++) {
					order.add(FoodType.coffee);
				}
				customers[i] = new Thread(new Customer("Customer " + (i + 1), order, priority));
			}
		}
		
		for (int i = 0; i < cooks.length; i++) {
			cooks[i].start();
		}
		
		// Now "let the customers know the shop is open" by
		// starting them running in their own thread.
		for (int i = 0; i < customers.length; i++) {
			customers[i].start();
			// NOTE: Starting the customer does NOT mean they get to go
			// right into the shop. There has to be a table for
			// them. The Customer class' run method has many jobs
			// to do - one of these is waiting for an available
			// table...
		}

		try {
			// Wait for customers to finish
			// -- you need to add some code here...
			for (int i = 0; i < customers.length; i++) {
				customers[i].join();
			}
			// Then send cooks home...
			// The easiest way to do this might be the following, where
			// we interrupt their threads. There are other approaches
			// though, so you can change this if you want to.
			for (int i = 0; i < cooks.length; i++)
				cooks[i].interrupt();
			for (int i = 0; i < cooks.length; i++)
				cooks[i].join();

		} catch (InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		Machine machine;
		machine = coffeeShop.getMachines().remove(0);
		Simulation.logEvent(SimulationEvent.machineEnding(machine));
		machine = coffeeShop.getMachines().remove(1);
		Simulation.logEvent(SimulationEvent.machineEnding(machine));
		machine = coffeeShop.getMachines().remove(2);
		Simulation.logEvent(SimulationEvent.machineEnding(machine));
		// Done with simulation
		logEvent(SimulationEvent.endSimulation());
		
		int[] count = new int[3];
		long[] ms = new long[3];
		for(Customer customer:coffeeShop.getTimes()) {
			int i = customer.getPriority()-1;
			count[i]++;
			ms[i]+=customer.getWaitTime();
		}
		
		for (int i = 0; i < 3; i++) {
			double avg = (ms[i]+0.0)/count[i];
			System.out.println("The average wait time for priority "+(i+1)+": "+avg +"ms");
		}
		
		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args
	 *            the command-line arguments for the simulation. There should be
	 *            exactly four arguments: the first is the number of customers, the
	 *            second is the number of cooks, the third is the number of tables
	 *            in the coffee shop, and the fourth is the number of items each
	 *            cooking machine can make at the same time.
	 */
	public static void main(String args[]) throws InterruptedException {
		// Parameters to the simulation
		/*
		 * if (args.length != 4) { System.err.
		 * println("usage: java Simulation <#customers> <#cooks> <#tables> <capacity> <randomorders"
		 * ); System.exit(1); } int numCustomers = new Integer(args[0]).intValue(); int
		 * numCooks = new Integer(args[1]).intValue(); int numTables = new
		 * Integer(args[2]).intValue(); int machineCapacity = new
		 * Integer(args[3]).intValue(); boolean randomOrders = new Boolean(args[4]);
		 */
		int numCustomers = 1000;
		int numCooks = 10;
		int numTables = 50;
		int machineCapacity = 5;
		boolean randomOrders = true;

		// Run the simulation and then
		// feed the result into the method to validate simulation.
		System.out.println("Did it work? " + Validate
				.validateSimulation(runSimulation(numCustomers, numCooks, numTables, machineCapacity, randomOrders)));
	}

}
