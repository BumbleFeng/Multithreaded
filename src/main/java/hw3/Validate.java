package hw3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hw3.SimulationEvent;

/**
 * Validates a simulation
 */
public class Validate {
	private static class InvalidSimulationException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidSimulationException() {
		}
	};

	// Helper method for validating the simulation
	private static boolean check(boolean check, String message) throws InvalidSimulationException {
		if (!check) {
			System.err.println("SIMULATION INVALID : " + message);
			throw new Validate.InvalidSimulationException();
		}
		return check;
	}

	/**
	 * Validates the given list of events is a valid simulation. Returns true if the
	 * simulation is valid, false otherwise.
	 *
	 * @param events
	 *            - a list of events generated by the simulation in the order they
	 *            were generated.
	 *
	 * @returns res - whether the simulation was valid or not
	 */
	public static boolean validateSimulation(List<SimulationEvent> events) {
		try {
			check(events.get(0).event == SimulationEvent.EventType.SimulationStarting,
					"Simulation didn't start with initiation event");
			int[] params = events.get(0).simParams;
			int numCustomers = params[0];
			int numCooks = params[1];
			int numTables = params[2];
			int capacity = params[3];
			check(events.get(events.size() - 1).event == SimulationEvent.EventType.SimulationEnded,
					"Simulation didn't end with termination event");
			check(checkCustomerNum(events, numCustomers), "More eaters than specified");
			check(checkCookNum(events, numCooks), "More cooks than specified");
			check(checkTables(events, numTables), "Exceed coffee shop capacity");
			check(checkMachines(events, capacity), "Exceed capacity of machines");
			check(checkOrderNum(events, numCustomers), "Eater place more than one order");
			check(checkSequence(events, numCustomers), "Sequence Error");
			check(checkFoodNum(events, numCustomers), "Food Number Error");
			// In hw3 you will write validation code for things such as:
			// Should not have more eaters than specified
			// Should not have more cooks than specified
			// The coffee shop capacity should not be exceeded
			// The capacity of each machine should not be exceeded
			// Eater should not receive order until cook completes it
			// Eater should not leave coffee shop until order is received
			// Eater should not place more than one order
			// Cook should not work on order before it is placed

			return true;
		} catch (InvalidSimulationException e) {
			return false;
		}
	}

	public static boolean checkCustomerNum(List<SimulationEvent> events, int numCustomers) {
		Set<Customer> customers = new HashSet<>();
		for (SimulationEvent event : events) {
			if (event.customer != null)
				customers.add(event.customer);
		}
		return customers.size() == numCustomers;
	}

	public static boolean checkCookNum(List<SimulationEvent> events, int numCooks) {
		Set<Cook> cooks = new HashSet<>();
		for (SimulationEvent event : events) {
			if (event.cook != null)
				cooks.add(event.cook);
		}
		return cooks.size() == numCooks;
	}

	public static boolean checkTables(List<SimulationEvent> events, int numTables) {
		int sit = 0;
		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CustomerEnteredCoffeeShop)
				sit++;
			if (event.event == SimulationEvent.EventType.CustomerLeavingCoffeeShop)
				sit--;
			if (sit > numTables) {
				return false;
			}
		}
		return sit == 0;
	}

	public static boolean checkMachines(List<SimulationEvent> events, int capacity) {
		int[] count = new int[3];
		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.MachineStartingFood) {
				switch (event.food.name) {
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
			if (event.event == SimulationEvent.EventType.MachineDoneFood) {
				switch (event.food.name) {
				case "burger":
					count[0]--;
					break;
				case "fries":
					count[1]--;
					break;
				case "coffee":
					count[2]--;
					break;
				}
			}
			if (count[0] > capacity || count[1] > capacity || count[2] > capacity)
				return false;
		}
		return count[0] == 0 && count[1] == 0 && count[2] == 0;
	}

	public static boolean checkOrderNum(List<SimulationEvent> events, int numCustomers) {
		Set<Customer> order = new HashSet<>();
		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CustomerPlacedOrder) {
				if (event.orderNumber - customerId(event) != 1)
					return false;
				if (order.contains(event.customer)) {
					return false;
				} else {
					order.add(event.customer);
				}
			}
		}
		return order.size() == numCustomers;
	}

	public static boolean checkSequence(List<SimulationEvent> events, int numCustomers) {
		int[][] sequence = new int[numCustomers][7];
		for (int i = 0; i < events.size(); i++) {
			SimulationEvent event = events.get(i);
			if (event.event == SimulationEvent.EventType.CustomerStarting)
				sequence[customerId(event)][0] = i;
			if (event.event == SimulationEvent.EventType.CustomerEnteredCoffeeShop)
				sequence[customerId(event)][1] = i;
			if (event.event == SimulationEvent.EventType.CustomerPlacedOrder)
				sequence[customerId(event)][2] = i;
			if (event.event == SimulationEvent.EventType.CookReceivedOrder)
				sequence[event.orderNumber - 1][3] = i;
			if (event.event == SimulationEvent.EventType.CookCompletedOrder)
				sequence[event.orderNumber - 1][4] = i;
			if (event.event == SimulationEvent.EventType.CustomerReceivedOrder)
				sequence[customerId(event)][5] = i;
			if (event.event == SimulationEvent.EventType.CustomerLeavingCoffeeShop)
				sequence[customerId(event)][6] = i;
		}

		for (int i = 0; i < numCustomers; i++) {
			for (int j = 0; j < 6; j++) {
				if (sequence[i][j] == 0 || sequence[i][j + 1] == 0 || sequence[i][j] >= sequence[i][j + 1])
					return false;
			}
		}
		System.out.println(Arrays.deepToString(sequence));
		return true;
	}

	public static boolean checkFoodNum(List<SimulationEvent> events, int numCustomers) {
		int[][] order = new int[numCustomers][3];
		int[][] cook = new int[numCustomers][3];
		int[] machine = new int[3];
		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CookReceivedOrder) {
				int orderNum = event.orderNumber - 1;
				for (Food food : event.orderFood) {
					switch (food.name) {
					case "burger":
						order[orderNum][0]++;
						break;
					case "fries":
						order[orderNum][1]++;
						break;
					case "coffee":
						order[orderNum][2]++;
						break;
					}
				}
			}
			if (event.event == SimulationEvent.EventType.CookStartedFood) {
				int orderNum = event.orderNumber - 1;
				switch (event.food.name) {
				case "burger":
					cook[orderNum][0] += 100;
					break;
				case "fries":
					cook[orderNum][1] += 100;
					break;
				case "coffee":
					cook[orderNum][2] += 100;
					break;
				}
			}
			if (event.event == SimulationEvent.EventType.CookFinishedFood) {
				int orderNum = event.orderNumber - 1;
				switch (event.food.name) {
				case "burger":
					cook[orderNum][0] -= 99;
					break;
				case "fries":
					cook[orderNum][1] -= 99;
					break;
				case "coffee":
					cook[orderNum][2] -= 99;
					break;
				}
			}
			if (event.event == SimulationEvent.EventType.MachineStartingFood) {
				switch (event.food.name) {
				case "burger":
					machine[0] += 100;
					break;
				case "fries":
					machine[1] += 100;
					break;
				case "coffee":
					machine[2] += 100;
					break;
				}
			}
			if (event.event == SimulationEvent.EventType.MachineDoneFood) {
				switch (event.food.name) {
				case "burger":
					machine[0] -= 99;
					break;
				case "fries":
					machine[1] -= 99;
					break;
				case "coffee":
					machine[2] -= 99;
					break;
				}
			}
		}
		System.out.println(Arrays.deepToString(order));
		System.out.println(Arrays.toString(machine));
		int[] food = new int[3];
		for (int i = 0; i < numCustomers; i++) {
			for (int j = 0; j < 3; j++) {
				if (order[i][j] != cook[i][j])
					return false;
				else
					food[j] += order[i][j];
			}
		}
		return food[0] == machine[0] && food[1] == machine[1] && food[2] == machine[2];
	}

	public static int customerId(SimulationEvent event) {
		return Integer.valueOf(event.customer.toString().substring(9)) - 1;
	}
}
