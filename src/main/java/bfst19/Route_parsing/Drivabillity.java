package bfst19.Route_parsing;

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

    public static Drivabillity valueToDrivabillity(int value) {

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