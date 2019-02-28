package bfst19.linedrawing;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

public class FancyView extends View {
	public FancyView(Model model, Stage stage) {
		super(model, stage);
	}

	@Override
	public void repaint() {
		gc.save();
		gc.setTransform(new Affine());
		gc.setFill(Color.LAWNGREEN);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setTransform(transform);
		gc.setLineWidth(12);
		gc.setLineCap(StrokeLineCap.ROUND);
		gc.setStroke(Color.BROWN);
		for (Line line : model) {
			gc.beginPath();
			gc.moveTo(line.x1, line.y1);
			gc.lineTo(line.x2, line.y2);
			gc.stroke();
		}
		gc.setLineWidth(10);
		gc.setLineCap(StrokeLineCap.ROUND);
		gc.setStroke(Color.BLACK);
		for (Line line : model) {
			gc.beginPath();
			gc.moveTo(line.x1, line.y1);
			gc.lineTo(line.x2, line.y2);
			gc.stroke();
		}
		gc.setLineWidth(1);
		gc.setLineDashes(2,5);
		gc.setLineCap(StrokeLineCap.SQUARE);
		gc.setStroke(Color.WHITE);
		for (Line line : model) {
			gc.beginPath();
			gc.moveTo(line.x1, line.y1);
			gc.lineTo(line.x2, line.y2);
			gc.stroke();
		}
		gc.restore();
	}
}
