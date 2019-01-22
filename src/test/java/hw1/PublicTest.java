package hw1;

import static org.junit.Assert.*;
import java.util.*;


import org.junit.Test;

import hw1.ParallelMaximizer;

public class PublicTest {

	private int	threadCount = 10; // number of threads to run
	private ParallelMaximizer maximizer = new ParallelMaximizer(threadCount);
	private ParallelAverager averager = new ParallelAverager(threadCount);
	
	@Test
	public void compareMax() {
		int size = 10000; // size of list
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		int serialMax = Integer.MIN_VALUE;
		int parallelMax = 0;
		// populate list with random elements
		for (int i=0; i<size; i++) {
			int next = rand.nextInt();
			list.add(next);
			serialMax = Math.max(serialMax, next); // compute serialMax
		}
		// try to find parallelMax
		try {
			parallelMax = maximizer.max(list);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("The test failed because the max procedure was interrupted unexpectedly.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
		}
		
		assertEquals("The serial max doesn't match the parallel max", serialMax, parallelMax);
	}
	
	@Test
	public void compareAvg() {
		int size = 10000; // size of list
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		double serialAvg = 0;
		double parallelAvg = 0;
		// populate list with random elements
		for (int i=0; i<size; i++) {
			int next = rand.nextInt();
			list.add(next);
			serialAvg += next / (size+0.0); // compute serialMax
		}
		// try to find parallelMax
		try {
			parallelAvg = averager.avg(list);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("The test failed because the max procedure was interrupted unexpectedly.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
		}
		System.out.println(parallelAvg-serialAvg);
		assertEquals("The serial max doesn't match the parallel avg", serialAvg, parallelAvg, 0.0000001);
	}
}
