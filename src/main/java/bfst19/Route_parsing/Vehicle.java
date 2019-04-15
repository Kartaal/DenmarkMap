package bfst19.Route_parsing;

public enum Vehicle{
    CAR(130.0),
    BIKE(20.0);

    double maxSpeed;

    Vehicle(double maxSpeed){
        this.maxSpeed = maxSpeed;
    }

}
