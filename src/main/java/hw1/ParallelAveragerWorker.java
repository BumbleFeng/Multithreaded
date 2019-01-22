package hw1;

import java.util.LinkedList;

/**
 * Given a <code>LinkedList</code>, this class will find the maximum over a
 * subset of its <code>Integers</code>.
 */
public class ParallelAveragerWorker extends Thread {

	protected LinkedList<Integer> list;
	protected double partialAvg = 0;
	protected int size;

	public ParallelAveragerWorker(LinkedList<Integer> list) {
		this.list = list;
		size = list.size();
	}

	/**
	 * Update <code>partialMax</code> until the list is exhausted.
	 */
	public void run() {
		while (true) {
			int number;
			// check if list is not empty and removes the head
			// synchronization needed to avoid atomicity violation
			synchronized (list) {
				if (list.isEmpty()) {
					return; // list is empty
				}
				number = list.remove();
				// System.out.println(Thread.currentThread().getName()+" "+number);
			}

			partialAvg += number / (size + 0.0);
		}
	}

	public double getPartialAvg() {
		return partialAvg;
	}

}
