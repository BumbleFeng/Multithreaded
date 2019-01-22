package hw4;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This file needs to hold your solver to be tested. You can alter the class to
 * extend any class that extends MazeSolver. It must have a constructor that
 * takes in a Maze. It must have a solve() method that returns the datatype
 * List<Direction> which will either be a reference to a list of steps to take
 * or will be null if the maze cannot be solved.
 */
public class StudentMTMazeSolverDFS extends SkippingMazeSolver {

	public StudentMTMazeSolverDFS(Maze maze) {
		super(maze);
	}

	public List<Direction> solve() {
		// TODO: Implement your code here
		ExecutorService threadPool = Executors.newFixedThreadPool(20);
		int count1 = 0;
		int count2 = 0;
		BlockingQueue<Result> result1 = new LinkedBlockingQueue<Result>();
		BlockingQueue<Result> result2 = new LinkedBlockingQueue<Result>();

		try {
			Choice startPts = firstChoice(maze.getStart());
			while (!startPts.choices.isEmpty()) {
				Choice second = follow(startPts.at, startPts.choices.peek());
				Direction firstDir = startPts.choices.pop();
				while (!second.choices.isEmpty()) {
					threadPool.submit(new DFS(follow(second.at, second.choices.peek()), firstDir, second.choices.pop(),
							false, result1));
					count1++;
				}
			}

			Choice endPts = firstChoice(maze.getEnd());
			while (!endPts.choices.isEmpty()) {
				Choice second = follow(endPts.at, endPts.choices.peek());
				Direction firstDir = endPts.choices.pop();
				while (!second.choices.isEmpty()) {
					threadPool.submit(new DFS(follow(second.at, second.choices.peek()), firstDir, second.choices.pop(),
							true, result2));
					count2++;
				}
			}

		} catch (SolutionFound e) {
			return new STMazeSolverDFS(maze).solve();
		}

		Future<List<Direction>> future1 = threadPool.submit(new Done(count1, result1, result2));
		Future<List<Direction>> future2 = threadPool.submit(new Done(count2, result2, result1));

		try {
			List<Direction> solution1 = future1.get();
			if (solution1 != null) {
				threadPool.shutdownNow();
				return solution1;
			}
			List<Direction> solution2 = future2.get();
			if (solution2 != null) {
				threadPool.shutdownNow();
				return solution2;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		threadPool.shutdownNow();
		return null;
	}

	private class Done implements Callable<List<Direction>> {
		int count;
		BlockingQueue<Result> result1;
		BlockingQueue<Result> result2;

		public Done(int count, BlockingQueue<Result> result1, BlockingQueue<Result> result2) {
			this.count = count;
			this.result1 = result1;
			this.result2 = result2;
		}

		@Override
		public List<Direction> call() throws InterruptedException {
			int c = 0;
			while (c < count && !Thread.interrupted()) {
				Result r = result1.take();
				if (r.getCode() == 2)
					break;
				if (r.getCode() == 1) {
					result2.put(new Result(2));
					return r.getSolution();
				}
				c++;
			}
			result2.put(new Result(2));
			return null;
		}
	}

	private class Result {
		private int code;
		// 0 means one dead end;1 means find solution;2 means solved by another way
		private List<Direction> solution;

		public Result(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public List<Direction> getSolution() {
			return solution;
		}

		public void setSolution(List<Direction> solution) {
			this.solution = solution;
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

	private class DFS implements Callable<List<Direction>> {
		Choice startPt;
		Direction firstDir;
		Direction secondDir;
		boolean reverse;
		BlockingQueue<Result> result;

		public DFS(Choice startPt, Direction firstDir, Direction secondDir, boolean reverse,
				BlockingQueue<Result> result) {
			this.startPt = startPt;
			this.firstDir = firstDir;
			this.secondDir = secondDir;
			this.reverse = reverse;
			this.result = result;
		}

		@Override
		public List<Direction> call() throws InterruptedException {
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice ch;

			try {
				choiceStack.push(this.startPt);
				while (!choiceStack.isEmpty() && !Thread.interrupted()) {
					ch = choiceStack.peek();
					if (ch.isDeadend()) {
						// backtrack.
						choiceStack.pop();
						if (!choiceStack.isEmpty())
							choiceStack.peek().choices.pop();
						continue;
					}
					choiceStack.push(follow(ch.at, ch.choices.peek()));
				}
				// No solution found.
				// System.out.println(Thread.currentThread().getName());
				result.put(new Result(0));
				return null;
			} catch (SolutionFound e) {
				Iterator<Choice> iter = choiceStack.iterator();
				LinkedList<Direction> solutionPath = new LinkedList<Direction>();
				while (iter.hasNext()) {
					ch = iter.next();
					solutionPath.push(ch.choices.peek());
				}
				solutionPath.push(this.secondDir);
				solutionPath.push(this.firstDir);
				// System.out.println(Thread.currentThread().getName());
				Result r = new Result(1);
				r.setSolution(pathToFullPath(solutionPath, reverse));
				result.put(r);
				return solutionPath;
			}
		}
	}
}
