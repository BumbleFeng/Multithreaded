package hw4;

import java.util.LinkedList;
import java.util.List;

public class StudentSTMazeSolverRec extends SkippingMazeSolver {
	
	@Override
	public Choice follow(Position at, Direction dir) throws SolutionFound{
		try {
			return super.follow(at, dir);
		} catch (SolutionFound e) {
			throw new SolutionFound(at, dir);
		}
	}

	public StudentSTMazeSolverRec(Maze maze) {
		super(maze);
	}

	@Override
	public List<Direction> solve() {
		List<Direction> solutionPath = null;
		Choice ch;
		try {
			ch = firstChoice(maze.getStart());
			solutionPath = solve(ch);
		} catch (SolutionFound e) {
			System.out.println("Solution found.");
		}
		if (solutionPath == null)
			return null;
		return pathToFullPath(solutionPath);
	}

	public LinkedList<Direction> solve(Choice ch) {
		try {
			while (!ch.choices.isEmpty()) {
				Direction direction = ch.choices.pop();
				LinkedList<Direction> sol = solve(follow(ch.at, direction));
				if (sol != null) {
					sol.addFirst(direction);
					return sol;
				}
			}
		} catch (SolutionFound e) {
			LinkedList<Direction> soln = new LinkedList<Direction>();
			soln.addFirst(e.from);
			return soln;
		}
		return null;
	}

}
