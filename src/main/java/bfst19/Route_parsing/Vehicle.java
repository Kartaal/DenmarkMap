package bfst19.Route_parsing;

public enum Vehicle{
    CAR(110.0),
    BIKE(30.0);

    double maxSpeed;

    Vehicle(double maxSpeed){
        this.maxSpeed = maxSpeed;
    }

}
