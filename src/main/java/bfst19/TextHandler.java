package bfst19;

import bfst19.Route_parsing.ResizingArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TextHandler {
    private static TextHandler textHandler;

    public static TextHandler getInstance(){
        if(textHandler == null){
            textHandler = new TextHandler();
        }
        return textHandler;
    }

    public void makeDatabase(ArrayList<Address> addresses, String dirPath, String delimiter){
        try{
            File countryDir = new File(dirPath);
//            if(countryDir.isDirectory()){
//                deleteDirectoryRecursion(new File(dirPath));
//            }
            countryDir.mkdir();

            String currentCityAndPostcode = "";
            String currentStreet = "";
            //this first step looks ugly and is perhaps unnecessary
            BufferedWriter allStreetsInCountryWriter = newBufferWriter(dirPath + "/streets.txt");
            BufferedWriter citiesInCountryWriter = newBufferWriter(dirPath+ "/cities.txt");
            BufferedWriter streetsInCityWriter =  newStreetsInCityWriter(currentCityAndPostcode,dirPath);
            BufferedWriter addressesInStreetWriter = newAddressesInStreetWriter(currentStreet,currentCityAndPostcode,dirPath);


            for(Address address:addresses) {
                //if the streetName remains the same, and the city changes we need to change the writers for streets and addresses,
                //along with writing to the appropriate files, we also change the current city and postcode, and make the directory for it
                //todo fix code dupes here
                if (address.getStreetName().equals(currentStreet) && !(address.getCity() + delimiter + address.getPostcode()).equals(currentCityAndPostcode)) {

                    //flush writers to ensure no backlog gets deleted
                    streetsInCityWriter.flush();
                    addressesInStreetWriter.flush();

                    //redefine variables and make new dir
                    currentCityAndPostcode = address.getCity() + delimiter + address.getPostcode();
                    makeCityDir(currentCityAndPostcode,dirPath);
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode,dirPath);
                    addressesInStreetWriter = newAddressesInStreetWriter(currentStreet,currentCityAndPostcode,dirPath);

                    //write to the writers respective files
                    citiesInCountryWriter.write(currentCityAndPostcode + "\n");
                    streetsInCityWriter.write(currentStreet + "\n");
                    allStreetsInCountryWriter.write(currentStreet + delimiter + currentCityAndPostcode + "\n");

                } else if (!(address.getCity() + delimiter + address.getPostcode()).equals(currentCityAndPostcode)) {
                    //if the city changes, flush the writers and change the writer for the streets in that city,
                    // write to the file with all the cities and make the cities directory, also change the current city and postcode
                    streetsInCityWriter.flush();

                    //redefine variables and make new dir
                    currentCityAndPostcode = address.getCity() + delimiter + address.getPostcode();
                    makeCityDir(currentCityAndPostcode,dirPath);
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode,dirPath);

                    //write to the writers respective files
                    citiesInCountryWriter.write(currentCityAndPostcode + "\n");
                }else if (!address.getStreetName().equals(currentStreet)) {

                    //flush writers to ensure no backlog gets deleted
                    addressesInStreetWriter.flush();

                    //redefine variables
                    currentStreet = address.getStreetName();


                    addressesInStreetWriter = newAddressesInStreetWriter(currentStreet,currentCityAndPostcode,dirPath);

                    //write to the writers respective files
                    streetsInCityWriter.write(currentStreet + "\n");
                    allStreetsInCountryWriter.write(currentStreet + delimiter + currentCityAndPostcode + "\n");
                }

                addressesInStreetWriter.write(address.getLat() + " " + address.getLon() + " " + address.getHouseNumber() + "\n");

            }

            //closes all writers
            allStreetsInCountryWriter.close();
            citiesInCountryWriter.close();
            streetsInCityWriter.close();
            addressesInStreetWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //<----------- Helper methods for makeDatabase ----------------------------------->
    private BufferedWriter newAddressesInStreetWriter(String currentStreet, String currentCityAndPostcode, String dirPath) throws FileNotFoundException {
        return newBufferWriter(dirPath + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
    }

    private BufferedWriter newStreetsInCityWriter(String currentCityAndPostcode, String dirPath) throws FileNotFoundException {
        return newBufferWriter(dirPath+"/"+currentCityAndPostcode+"/streets.txt");
    }

    private BufferedWriter newBufferWriter(String dirPath) throws FileNotFoundException {
        File file = new File(dirPath);
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8));
        return writer;
    }

    private void makeCityDir(String currentCityAndPostcode,String dirPath){
        File cityDir = new File(dirPath + "/" + currentCityAndPostcode);
        cityDir.mkdir();
    }

    private void deleteDirectoryRecursion(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }
    //<----------------------------------------------------------------------------->

    //getting a generic text file from a given path
    public ArrayList<String> getTextFile(String filepath){
        try {
            //Handling whether filepath is for a config or a database file
            BufferedReader reader;
            if(filepath.startsWith("config/")) {
                InputStream fileStream = getClass().getClassLoader().getResourceAsStream(filepath);
                reader = new BufferedReader(new InputStreamReader(
                        fileStream, StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(filepath), StandardCharsets.UTF_8));
            }

            ArrayList<String> textFile = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null){
                textFile.add(line);
            }
            return textFile;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read from file at " + filepath);
        }
        return null;
    }

    //Return ArrayList of colors
    public ArrayList<String> parseWayColors(String filepath){
        //TODO Shitty duplicate code :\
        try {
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream(filepath);
            BufferedReader reader= new BufferedReader(new InputStreamReader(
                    fileStream, StandardCharsets.UTF_8));
            ArrayList<String> textFile = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null){
                textFile.add(line);
            }
            return textFile;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read color cases from " + filepath);
        }
        return null;
    }

    //<--------Helper methods for getting specific kinds of text files--------------->
    public ArrayList<String> getCities( String dirPath){
        return getTextFile(dirPath + "/cities.txt");
    }

    public ArrayList<String> getStreetsInCity( String city, String postcode){
        return getTextFile(Model.getDirPath() + "/" +
                city + Model.getDelimeter() + postcode + "/streets.txt");
    }

    public ArrayList<String> getAddressesOnStreet(String city,String postcode,String streetName){
        return getTextFile(Model.getDirPath() + "/" +
                city + Model.getDelimeter() + postcode + "/" + streetName + ".txt");
    }

    public ArrayList<String> getDefault(String dirPath) {
        return getTextFile(dirPath + "/streets.txt");
    }
    //<----------------------------------------------------------------------------->



    public void parseWayColors(Model model){
        ArrayList<String> cases = parseWayColors(model.getCurrentTypeColorTxt());
        model.clearColors();
        int m = Integer.parseInt(cases.get(0));
        for (int i = 1; i < m; i++) {
            String[] strArr = cases.get(i).split(" ");
            model.addTypeColors(strArr);
        }
        model.notifyColorObservers();
    }

    public HashMap<WayType, HashMap<String, ResizingArray<String[]>>> parseDrivableCases() {
        ArrayList<String> cases = getTextFile("config/Drivable_cases.txt");
        HashMap<WayType, HashMap<String, ResizingArray<String[]>>> drivableCases = new HashMap<>();

        WayType wayType = WayType.valueOf(cases.get(0));
        drivableCases.put(wayType, new HashMap<>());
        String[] tokens;
        String vehicleType = "";
        String vehicleDrivable = "";

        for(int i = 1 ; i<cases.size() ; i++){
            String line = cases.get(i);
            if(line.startsWith("%")){
                tokens = cases.get(i+1).split(" ");
                i++;
                vehicleType = tokens[0];
                vehicleDrivable = tokens[1];
                drivableCases.get(wayType).put(vehicleType+" "+vehicleDrivable,new ResizingArray<>());
            }else if(line.startsWith("$")){
                wayType = WayType.valueOf(cases.get(i+1));
                drivableCases.put(wayType,new HashMap<>());
                i++;
            }else{
                String[] lineTokens = line.split(" ");
                drivableCases.get(wayType).get(vehicleType+" "+vehicleDrivable).add(lineTokens);
            }
        }
        return drivableCases;
    }

    public HashMap<String,Integer> parseSpeedDefaults(){
        ArrayList<String> cases = getTextFile("config/Speed_cases.txt");
        HashMap<String,Integer> speedDefaults = new HashMap<>();
        for(int i = 0 ; i < cases.size() ; i++){
            String line = cases.get(i);
            String[] tokens = line.split(" ");
            speedDefaults.put(tokens[0], Integer.valueOf(tokens[1]));
        }
        return speedDefaults;
    }


    public HashMap<WayType, ResizingArray<String[]>> parseWayTypeCases(){
        HashMap<WayType, ResizingArray<String[]>>  wayTypeCases = new HashMap<>();

        ArrayList<String> cases = getTextFile("config/WayTypeCases.txt");
        String wayCase;
        WayType wayType = null;
        for(int i = 0; i < cases.size() ; i++) {
            wayCase = cases.get(i);
            if(wayCase.startsWith("$")) {
                wayType = WayType.valueOf(cases.get(i+1));
                i++;
            }else{
                String[] tokens = wayCase.split(" ");
                if(wayTypeCases.get(wayType) == null){
                    wayTypeCases.put(wayType, new ResizingArray<>());
                }
                wayTypeCases.get(wayType).add(new String[]{tokens[0], tokens[1]});
            }
        }
        return wayTypeCases;
    }

    public ArrayList<String[]> parseCitiesAndPostcodes(String dirPath){
        ArrayList<String> citiesTextFile = getCities(dirPath);
        ArrayList<String[]> citiesAndPostcodes = new ArrayList<>();
        for(int i = 0 ; i<citiesTextFile.size()-1 ; i++){
            String line = citiesTextFile.get(i);
            String[] tokens = line.split(" QQQ ");
            citiesAndPostcodes.add(tokens);
        }
        return citiesAndPostcodes;
    }

       public void writePointsOfInterest(String dirPath, List<PointOfInterestItem> pointsOfInterest) {
        try {
            File pointsOfInterestFile = new File(dirPath+ "/pointsOfInterest.txt");
            if(pointsOfInterestFile.isFile()){
                pointsOfInterestFile.delete();
            }
            BufferedWriter pointsOfInterestWriter = newBufferWriter(dirPath+ "/pointsOfInterest.txt");

            for(PointOfInterestItem pointOfInterest : pointsOfInterest){
                pointsOfInterestWriter.write(pointOfInterest.toString()+"\n");
            }

            pointsOfInterestWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't create an OutputStream for points of interests or failed to write to it.");
        }
    }

    public List<PointOfInterestItem> getPointsOfInterest(String dirPath){
        File databaseDir = new File(dirPath + "/pointsOfInterest.txt");
        ArrayList<PointOfInterestItem> pointsOfInterest = new ArrayList<>();
        if(databaseDir.isFile()) {
            ArrayList<String> pointOfInterestFile = textHandler.getTextFile(dirPath + "/pointsOfInterest.txt");
            for (String pointOfInterest : pointOfInterestFile) {
                String[] pointOFInterestFields = pointOfInterest.split(Model.getDelimeter());
                String address = pointOFInterestFields[0];
                float x =(float) (Float.valueOf(pointOFInterestFields[1])*Model.getLonfactor());
                float y = Float.valueOf(pointOFInterestFields[2]);
                pointsOfInterest.add(new PointOfInterestItem(address,x,y));
            }
        }
        return pointsOfInterest;
    }


}