package bfst19.linedrawing;

import java.io.PrintStream;
import java.util.Scanner;

public class Line {
	double x1, y1, x2, y2;

	public Line(String line) {
		String[] words = line.split(" ");
		x1 = Double.parseDouble(words[1]);
		y1 = Double.parseDouble(words[2]);
		x2 = Double.parseDouble(words[3]);
		y2 = Double.parseDouble(words[4]);
	}

	public Line(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void print(PrintStream out) {
		out.printf("LINE %f %f %f %f\n", x1, y1, x2, y2);
	}
}
