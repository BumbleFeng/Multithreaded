package hw4;

import java.util.LinkedList;
import java.util.List;

/**
 * A single-threaded breadth-first solver.
 */
public class STMazeSolverBFS extends SkippingMazeSolver {
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

	public STMazeSolverBFS(Maze maze) {
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

	/**
	 * Expands a node in the search tree, returning the list of child nodes.
	 * 
	 * @throws SolutionFound
	 */
	public List<SolutionNode> expand(SolutionNode node) throws SolutionFound {
		LinkedList<SolutionNode> result = new LinkedList<SolutionNode>();
		if (maze.display != null)
			maze.setColor(node.choice.at, 0);
		for (Direction dir : node.choice.choices) {
			Choice newChoice = follow(node.choice.at, dir);
			if (maze.display != null)
				maze.setColor(newChoice.at, 2);
			result.add(new SolutionNode(node, newChoice, dir));
		}
		return result;
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
		SolutionNode curr = null;
		LinkedList<SolutionNode> frontier = new LinkedList<SolutionNode>();

		try {
			frontier.push(new SolutionNode(null, firstChoice(maze.getStart()), null));
			while (!frontier.isEmpty()) {
				LinkedList<SolutionNode> new_frontier = new LinkedList<SolutionNode>();

				for (SolutionNode node : frontier) {
					if (!node.choice.isDeadend()) {
						curr = node;
						new_frontier.addAll(expand(node));
					} else if (maze.display != null) {
						maze.setColor(node.choice.at, 0);
					}
				}
				
				frontier = new_frontier;
				if (maze.display != null) {
					maze.display.updateDisplay();
					// try
					// {
					// Thread.sleep(50);
					// }
					// catch (InterruptedException e)
					// {
					// }
					// Could use: maze.display.waitForMouse();
					// if we wanted to pause until a mouse button was pressed.
				}
			}
			// No solution found.
			return null;
		} catch (SolutionFound e) {
			if (curr == null) {
				// this only happens if there was a direct path from the start
				// to the end
				return pathToFullPath(maze.getMoves(maze.getStart()));
			} else {
				LinkedList<Direction> soln = new LinkedList<Direction>();
				// First save the direction we were going in when the exit was
				// discovered.
				soln.addFirst(e.from);
				while (curr.parent != null) {
					soln.addFirst(curr.from);
					curr = curr.parent;
				}
				markPath(soln, 1);
				return pathToFullPath(soln);
			}
		}
	}
}
