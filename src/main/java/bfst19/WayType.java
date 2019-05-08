package bfst19;

public enum WayType {
	RECREATION(false, 10),
	PARKING(false, 7),
	FARMLAND(false, 10),
	FARMYARD(false, 10),
	MILITARY(false, 12),
	AMENITY(false, 8),
	RESIDENTIAL(false, 5),
	INDUSTRIAL(false, 6),
	AIRPORT_APRON(false, 14),
	PIER(false, 5),
	TREE(false, 8),
	PARK(false, 6),
	FOREST(false, Integer.MIN_VALUE),
	BEACH(false, 14),
	GRASS(false, 2),
	WATER(false, 2),
	PITCH(false, 5),
	STADIUM(false, 13),
	TRACK(true, 500),
	BROWNFIELD(false, Integer.MIN_VALUE),
	CONSTRUCTION(false, 6),
	ALLOTMENTS(false, 8),
	CEMETERY(false, 6),
	SQUARE(false, 5),
	SCRUB(false, 10),
	UNDERBRIDGE(false, 12),

	FOOTWAY(true, 500),
	MOTORWAY(true, Integer.MIN_VALUE),
	PRIMARY(true, Integer.MIN_VALUE),
	SECONDARY(true, 5),
	TERTIARY(true, 5),
	SERVICE(true, 200),
	ROAD_RESIDENTIAL(true, 100),
	CYCLEWAY(true, 500),

	SUBWAY(true, 5),

	BRIDGE(false, Integer.MIN_VALUE),

	RAILCONSTRUCTION(true, 9),

	DISUSED(true, 30),

	COASTLINE(true, 12),
	BOAT(true, Integer.MIN_VALUE),
	COMMERCIAL(false, 14),
	BUILDING(false, 7),
	RACEWAY(true, 17),
	QUARRY(false, 12),

	DITCH(true, 17),
	BOUNDARY_ADMINISTRATIVE(true, 1),
	RAILWAY(true, Integer.MIN_VALUE),
	MILLITARY(false, 14),
	PEDESTRIAN(true, 500),
	RAILWAY_PLATFORM(false, 12),
	AIRPORT_RUNWAY(true, 10),
	AIRPORT_TAXIWAY(true, 10),
	BREAKWATER(false, 12);


	private boolean isRoadOrSimilar;
	//Originally it was shouldFill, because that is functionally the case,
	// however the true commonality is that they are meant to be strokes, lines or similar
	// and I could not come up with a better name.

	private int levelOfDetail;
	//This value is intended to implement primitive or naive level of detail, and needs polish currently.

	WayType(boolean isRoadOrSimilar, int levelOfDetail) {
		this.isRoadOrSimilar = isRoadOrSimilar;
		this.levelOfDetail = levelOfDetail;
	}

	public boolean isRoadOrSimilar() {
		return isRoadOrSimilar;
	}

	public double levelOfDetail() {
		return levelOfDetail;
	}
}
