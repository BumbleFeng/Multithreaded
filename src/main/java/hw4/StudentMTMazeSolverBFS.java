package hw4;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class StudentMTMazeSolverBFS extends SkippingMazeSolver {

	public class SolutionNode {
		public SolutionNode parent;
		public Choice choice;
		public Direction from;

		public SolutionNode(SolutionNode parent, Choice choice, Direction from) {
			this.parent = parent;
			this.choice = choice;
			this.from = from;
		}
	}

	public StudentMTMazeSolverBFS(Maze maze) {
		super(maze);
	}

	@Override
	public Choice follow(Position at, Direction dir) throws SolutionFound {
		try {
			return super.follow(at, dir);
		} catch (SolutionFound e) {
			throw new SolutionFound(at, dir);
		}
	}

	public List<Direction> pathToFullPath(List<Direction> path, boolean reverse) {
		if (reverse) {
			Iterator<Direction> pathIter = path.iterator();
			LinkedList<Direction> fullPath = new LinkedList<Direction>();

			Position curr = maze.getEnd();
			Direction go_to = null, came_from = null;
			while (!curr.equals(maze.getStart())) {
				LinkedList<Direction> moves = maze.getMoves(curr);
				moves.remove(came_from);
				if (moves.size() == 1)
					go_to = moves.getFirst();
				else if (moves.size() > 1)
					go_to = pathIter.next();
				else if (moves.size() == 0) {
					System.out.println("Error in solution--leads to deadend.");
					throw (new Error());
					// System.exit(-1);
				}
				fullPath.add(go_to);
				curr = curr.move(go_to);
				came_from = go_to.reverse();
			}

			LinkedList<Direction> reversePath = new LinkedList<Direction>();
			for (int i = fullPath.size() - 1; i >= 0; i--) {
				reversePath.add(fullPath.get(i).reverse());
			}
			return reversePath;
		} else
			return super.pathToFullPath(path);
	}

	/**
	 * Expands a node in the search tree, returning the list of child nodes.
	 * 
	 * @throws SolutionFound
	 */

	private class Count {
		private int count;
		private Object lock;

		public Count() {
			count = 1;
			lock = new Object();
		}

		public void add(int a) {
			if (a != 0) {
				synchronized (lock) {
					count += a;
				}
			}
		}

		public boolean end() {
			synchronized (lock) {
				return count == 0;
			}
		}
	}

	private class BFS implements Callable<SolutionNode> {
		private BlockingQueue<SolutionNode> solutionNodes;
		private Count count;
		private BlockingQueue<List<Direction>> result;
		private boolean reverse;

		public BFS(BlockingQueue<SolutionNode> solutionNodes, Count count, BlockingQueue<List<Direction>> result,
				boolean reverse) {
			this.solutionNodes = solutionNodes;
			this.count = count;
			this.result = result;
			this.reverse = reverse;
		}

		@Override
		public SolutionNode call() throws InterruptedException {
			SolutionNode node = null;
			SolutionNode curr = null;
			try {
				while (!count.end() && !Thread.interrupted()) {
					node = solutionNodes.take();
					if (node.choice == null) 
						break;
					count.add(1);
					int c = 0;
					for (Direction dir : node.choice.choices) {
						Choice newChoice = follow(node.choice.at, dir);
						if (!newChoice.isDeadend()) {
							solutionNodes.put(new SolutionNode(node, newChoice, dir));
							c++;
						}
					}
					count.add(c - 2);
				}
				solutionNodes.put(new SolutionNode(null, null, null));
				result.put(new LinkedList<>());
				return null;
			} catch (SolutionFound e) {
				curr = new SolutionNode(node, null, e.from);
				solutionNodes.put(curr);
				LinkedList<Direction> solution = new LinkedList<Direction>();
				solution.addFirst(curr.from);
				while (curr.parent != null) {
					solution.addFirst(curr.from);
					curr = curr.parent;
				}
				result.put(pathToFullPath(solution, reverse));
				return curr;
			}
		}
	}

	private class Done implements Callable<List<Direction>> {
		private BlockingQueue<List<Direction>> result;
		private Count count1;
		private Count count2;

		public Done(BlockingQueue<List<Direction>> result, Count count1, Count count2) {
			this.result = result;
			this.count1 = count1;
			this.count2 = count2;
		}

		@Override
		public List<Direction> call() throws InterruptedException {
			while (!count1.end() && !count2.end() && !Thread.interrupted()) {
				List<Direction> solutionPath = result.take();
				if (!solutionPath.isEmpty())
					return solutionPath;
			}
			return null;
		}

	}

	/**
	 * Performs a breadth-first search of the maze. The algorithm builds a tree
	 * rooted at the start position. Parent pointers are used to point the way back
	 * to the entrance. The algorithm stores the list of leaves in the variables
	 * "frontier". During each iteration, these leaves are each expanded and the
	 * children the result become the new frontier. If a node represents a dead-end,
	 * it is discarded. Execution stops when the exit is discovered, as indicated by
	 * the SolutionFound exception.
	 */
	public List<Direction> solve() {
		// int numProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		BlockingQueue<SolutionNode> frontier1 = new LinkedBlockingQueue<SolutionNode>();
		BlockingQueue<SolutionNode> frontier2 = new LinkedBlockingQueue<SolutionNode>();
		Count count1 = new Count();
		Count count2 = new Count();
		BlockingQueue<List<Direction>> result = new LinkedBlockingQueue<List<Direction>>();

		try {
			frontier1.put(new SolutionNode(null, firstChoice(maze.getStart()), null));
			frontier2.put(new SolutionNode(null, firstChoice(maze.getEnd()), null));
		} catch (SolutionFound e) {
			return new STMazeSolverDFS(maze).solve();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 2; i++) {
			threadPool.submit(new BFS(frontier1, count1, result, false));
			threadPool.submit(new BFS(frontier2, count2, result, true));
		}

		List<Direction> solutionPath = null;
		try {
			solutionPath = threadPool.submit(new Done(result, count1, count2)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		// No solution found.
		threadPool.shutdownNow();
		return solutionPath;
	}
}
