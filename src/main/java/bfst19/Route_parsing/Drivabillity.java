package bfst19.Route_parsing;

/**
 * Represents the abillity to traverse an edge with a given vehicle
 * hence the word "Drivabillity" to describe this property.
 * This property can be in 4 different states and has names equivalent
 * to the direction(s) of traversal.
 * Also includes a value representing each state, and method for getting
 * a Drivabillity from one of these 4 values.
 * The values are ints for ease of comparison and compactness in config files.
 */
public enum Drivabillity {
    NOWAY(-1),
    FORWARD(0),
    BACKWARD(1),
    BOTHWAYS(2);
    int value;

    Drivabillity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Drivabillity intToDrivabillity(int value) {

        if (NOWAY.getValue() == value) {
            return NOWAY;
        } else if (FORWARD.getValue() == value) {
            return FORWARD;
        } else if (BACKWARD.getValue() == value) {
            return BACKWARD;
        } else if (BOTHWAYS.getValue() == value) {
            return BOTHWAYS;
        }
        return NOWAY;
    }
}
