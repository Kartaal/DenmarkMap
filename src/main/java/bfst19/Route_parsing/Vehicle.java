package bfst19.Route_parsing;

public enum Vehicle{
    CAR(130),
    WALKING(10),
    BIKE(20);

    int maxSpeed;

    Vehicle(int maxSpeed){
        this.maxSpeed = maxSpeed;
    }

}
