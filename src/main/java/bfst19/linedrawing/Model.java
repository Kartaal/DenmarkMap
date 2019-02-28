package bfst19.linedrawing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Model implements Iterable<Line> {
	List<Line> lines = new ArrayList<>();
	List<Runnable> observers = new ArrayList<>();
	String savefilename;

	public void addObserver(Runnable observer) {
		observers.add(observer);
	}

	public void notifyObservers() {
		for (Runnable observer : observers) observer.run();
	}

	public Model(List<String> args) {
		String filename = args.get(0);
		savefilename = args.get(1);
		try {
			Files.lines(Path.of(filename)).map(Line::new).forEach(lines::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<Line> iterator() {
		return lines.iterator();
	}

	public void add(Line line) {
		lines.add(line);
		notifyObservers();
	}

	public void save() {
		try {
			PrintStream out = new PrintStream(savefilename);
			for (Line line : lines) {
				line.print(out);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
