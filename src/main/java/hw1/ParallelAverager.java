package hw1;

import java.util.*;

public class ParallelAverager {

	int numThreads;
	ArrayList<ParallelAveragerWorker> workers; // = new ArrayList<ParallelMaximizerWorker>(numThreads);

	public ParallelAverager(int numThreads) {
		this.numThreads = numThreads;
		workers = new ArrayList<ParallelAveragerWorker>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			workers.add(null);
		}
	}

	public static void main(String[] args) {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 10; // number of integers in the list

		ParallelAverager averager = new ParallelAverager(numThreads);
		LinkedList<Integer> list = new LinkedList<Integer>();

		// populate the list
		// TODO: change this implementation to test accordingly
		for (int i = 0; i < numElements; i++) {
			list.add(i);
		}

		try {
			System.out.println(averager.avg(list));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public double avg(LinkedList<Integer> list) throws InterruptedException {
		double avg = 0; // initialize max as lowest value

		System.out.println(workers.size());
		// run numThreads instances of ParallelMaximizerWorker
		for (int i = 0; i < workers.size(); i++) {
			workers.set(i, new ParallelAveragerWorker(list));
		}
		
		for (int i = 0; i < workers.size(); i++) {
			workers.get(i).start();
		}
		// wait for threads to finish
		for (int i = 0; i < workers.size(); i++) {
			workers.get(i).join();
		}

		// take the highest of the partial maximums
		for (int i = 0; i < workers.size(); i++) {
			double a = workers.get(i).getPartialAvg();
			System.out.println(i + " " + a);
			avg += a;
		}

		return avg;
	}

}
