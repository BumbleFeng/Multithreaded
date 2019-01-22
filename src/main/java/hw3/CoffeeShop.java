package hw3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoffeeShop {
	private static CoffeeShop instance;
	private final int numTables;

	private List<Customer> tables;
	private HashMap<Integer, List<Food>> orderList;
	private HashMap<Integer,Object> orderLocks;
	private HashMap<Integer, Integer> newOrders;
	private List<Integer> completedOrders;
	private HashMap<Integer, Machine> machines;
	private List<Customer> times;

	public CoffeeShop(int numTables) {
		this.numTables = numTables;
		tables = new ArrayList<>();
		orderList = new HashMap<>();
		orderLocks = new HashMap<>();
		newOrders = new HashMap<>();
		completedOrders = new ArrayList<>();
		machines = new HashMap<>();
		times = new ArrayList<>();
		instance = this;
	}

	public static CoffeeShop getInstance() {
		return instance;
	}

	public void sit(Customer customer) {
		synchronized (tables) {
			while (tables.size() >= numTables) {
				try {
					tables.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			tables.add(customer);
		}
	}

	public void leave(Customer customer) {
		synchronized (tables) {
			times.add(customer);
			tables.remove(customer);
			Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(customer));
			tables.notifyAll();
		}
	}

	public Object placeOrder(int orderNum, List<Food> order, int priority) {
		synchronized (orderList) {
			orderList.put(orderNum, order);
		}
		synchronized (orderLocks) {
			orderLocks.put(orderNum, new Object());
		}
		synchronized (newOrders) {
			newOrders.put(orderNum, priority);
			newOrders.notifyAll();
		}
		Object lock = new Object();
		synchronized (orderLocks) {
			orderLocks.put(orderNum, lock);
		}
		return lock;
	}

	public boolean complete(int orderNum) {
		System.out.println("check order "+orderNum);
		synchronized (orderLocks.get(orderNum)) {
			synchronized (completedOrders) {
				return completedOrders.contains(orderNum);
			}
		}
	}

	public HashMap<Integer, Integer> getNewOrders() {
		synchronized (newOrders) {
			return newOrders;
		}
	}

	public boolean newOrder() {
		synchronized (newOrders) {
			return !newOrders.isEmpty();
		}
	}

	public int next() {
		synchronized (newOrders) {
			int orderNum = -1;
			int priority = 4;
			for (int key : newOrders.keySet()) {
				if (newOrders.get(key) == 1) {
					newOrders.remove(key);
					System.out.println("order " + key + " priority: 1");
					return key;
				}
				if (newOrders.get(key) < priority) {
					priority = newOrders.get(key);
					orderNum = key;
				}
			}
			System.out.println("order " + orderNum + " priority: " + priority);
			newOrders.remove(orderNum);
			return orderNum;
		}
	}

	public List<Food> getOrder(int orderNum) {
		synchronized (orderList) {
			return orderList.get(orderNum);
		}
	}

	public void completed(int orderNum) {
		synchronized (orderLocks.get(orderNum)) {
			synchronized (completedOrders) {
				completedOrders.add(orderNum);
				orderLocks.get(orderNum).notifyAll();
			}
		}
	}

	public Object getOrderLock(int orderNum) {
		return orderLocks.get(orderNum);
	}

	public HashMap<Integer, Machine> getMachines() {
		return machines;
	}

	public List<Customer> getTimes() {
		return times;
	}

}
