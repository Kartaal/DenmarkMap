package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import javafx.scene.layout.VBox;

import java.util.Iterator;

public class InstructionContainer extends VBox {
	private static Controller controller;

	public InstructionContainer() {
		super();
	}//boop

	public void init(Controller controller) {
		this.setStyle("-fx-min-width: 280; -fx-min-height: 400;");
		InstructionContainer.controller = controller;

		controller.addPathObserver(this);
	}

	void showInstructions() {
		addInstructions();
	}

	private void addInstructions() {
		Iterator<Edge> pathIterator = controller.getPathIterator();

		if (pathIterator != null) {
			Edge edge = pathIterator.next();
			Edge currentEdge = pathIterator.next();

			//this section is to establish which ends of the two starting edges are the "head" and "base"
			// being which direction we are going
			OSMNode previousV = edge.either();
			OSMNode previousW = edge.other();
			OSMNode newV = currentEdge.either();
			OSMNode newW = currentEdge.other();

			if (edge != null && currentEdge != null) {
				OSMNode previousBase;
				OSMNode previousHead;
				OSMNode currentBase;
				OSMNode currentHead;

				// 4 distinct cases
				if (previousV.getId() == newV.getId()) {
					previousHead = previousV;
					previousBase = previousW;
					currentBase = newV;
					currentHead = newW;

				} else if (previousW.getId() == newV.getId()) {
					previousHead = previousW;
					previousBase = previousV;
					currentBase = newV;
					currentHead = newW;

				} else if (previousV.getId() == newW.getId()) {
					previousHead = previousV;
					previousBase = previousW;
					currentBase = newW;
					currentHead = newV;

				} else {
					previousHead = previousW;
					previousBase = previousV;
					currentBase = newW;
					currentHead = newV;
				}

				double length = edge.getLength();
				String currentEdgeName = currentEdge.getName();
				double angle = Calculator.angleBetween2Lines(previousBase, previousHead, currentBase, currentHead);

				String direction = getTurnAngleText(angle);
				addNewInstruction(length, direction, currentEdgeName);

				length = currentEdge.getLength();
				String name = currentEdgeName;

				previousBase = currentBase;
				previousHead = currentHead;

				while (pathIterator.hasNext()) {

					currentEdge = pathIterator.next();
					currentHead = currentEdge.getOtherEndNode(previousHead);
					currentBase = currentEdge.getOtherEndNode(currentHead);
					double currentLength = currentEdge.getLength();

					angle = Calculator.angleBetween2Lines(previousBase, previousHead, currentBase, currentHead);
					direction = getTurnAngleText(angle);

					if (currentLength > 1.0) {
						String currentName = currentEdge.getName();

						if (direction.equals("u-turn") || direction.equals("left") ||
								direction.equals("right") || !name.equals(currentName)) {
							addNewInstruction(length, direction, currentName);
							length = 0.0;
							name = currentName;
						}
					}

					length += currentLength;
					previousBase = currentBase;
					previousHead = currentHead;
				}
			}
		}
	}

	private String getTurnAngleText(double angle) {
		String direction = "";
		if (15 < angle && 45 > angle) {
			direction = "keep left";
		} else if (45 < angle && angle < 120) {
			direction = "left";
		} else if (345 > angle && angle > 315) {
			direction = "keep right";
		} else if (240 < angle && angle < 315) {
			direction = "right";
		} else if (120 < angle && angle < 240) {
			direction = "u-turn";
		}
		return direction;
	}

	private void addNewInstruction(double speed, String direction, String road) {
		Instruction instruction = new Instruction(speed, direction, road);
		getChildren().add(instruction);
	}

	void removeAllChildren() {
		getChildren().clear();
	}

}
