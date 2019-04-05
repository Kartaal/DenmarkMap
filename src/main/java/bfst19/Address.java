package bfst19;


public class Address extends OSMNode implements Comparable{
    private String city, streetName, postcode, houseNumber, floor, side;

    public Address(long id,float lat,float lon,
                   String streetName, String houseNumber, String postcode, String city) {
        //todo call the other constructor from this one to avoid code dup
        super(id,lat,lon);
        this.streetName = streetName;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.city = city;
    }

    public Address(long id, float lat, float lon,
                   String streetName, String houseNumber, String postcode,
                   String city, String floor, String side) {
        this(id, lat, lon, streetName, houseNumber, postcode, city);
        this.side = side;
        this.floor = floor;
    }


    public String getCity() {
        return city;
    }

    public String getStreetName(){
        return streetName;
    }

    public long getId(){
        return id;
    }

    public float getLat(){
        return lat;
    }

    public float getLon(){
        return lon;
    }

    public String getPostcode(){
        return postcode;
    }

    public String getHouseNumber(){
        return houseNumber;
    }

    public String getFloor(){
        return floor;
    }

    public String getSide(){
        return side;
    }

    @Override
    public String toString(){
        return streetName+" "+postcode+" "+city;
    }

    @Override
    public int compareTo(Object o) {
        return (toString().compareToIgnoreCase(o.toString()));
    }
}
