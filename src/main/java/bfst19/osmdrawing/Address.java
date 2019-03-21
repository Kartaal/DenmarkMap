package bfst19.osmdrawing;

public class Address extends OSMNode{
    private String city;
    private String streetName;
    private String postcode;
    private String housenumber;

    public Address(long id,float lat,float lon,String streetName,String houseNumber,String postcode,String city){
        super(id,lat,lon);
        this.streetName = streetName;
        this.housenumber = houseNumber;
        this.postcode = postcode;
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public String getStreetName(){
        return streetName;
    }

    public long getId(){ return id; }

    public float getLat(){return lat;}

    public float getLon(){return lat;}

    public String getPostcode(){
        return postcode;
    }

    public String getHousenumber(){
        return housenumber;
    }

    @Override
    public String toString(){
        return id+" "+lat+" "+lon+" "+streetName+" "+housenumber+" "+postcode+" "+city;
    }
}
