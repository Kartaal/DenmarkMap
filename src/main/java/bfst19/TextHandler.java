package bfst19;

import bfst19.Route_parsing.ResizingArray;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

class TextHandler {

    void makeDatabase(ArrayList<Address> addresses, String dirPath, String delimiter){
        try{
            File countryDir = new File(dirPath);
            if(countryDir.isDirectory()){
                deleteDirectoryRecursion(new File(dirPath));
            }
            countryDir.mkdir();

            String currentCityAndPostcode = "";
            String currentStreet = "";
            //this first step looks ugly and is perhaps unnecessary
            BufferedWriter allStreetsInCountryWriter = newBufferWriter(dirPath+"streets.txt");
            BufferedWriter citiesInCountryWriter = newBufferWriter(dirPath+currentCityAndPostcode+"/"+currentStreet+".txt");
            BufferedWriter streetsInCityWriter =  newBufferWriter(dirPath+currentCityAndPostcode+"/streets.txt");
            BufferedWriter addressesInStreetWriter = newBufferWriter(dirPath+currentCityAndPostcode+"/"+currentStreet+".txt");


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
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode,dirPath);
                    addressesInStreetWriter = newAddressesInStreetWriter(currentStreet,currentCityAndPostcode,dirPath);
                    makeCityDir(currentCityAndPostcode,dirPath);

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
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode,dirPath);
                    makeCityDir(currentCityAndPostcode,dirPath);

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
    BufferedWriter newAddressesInStreetWriter(String currentStreet, String currentCityAndPostcode, String dirPath) throws FileNotFoundException, UnsupportedEncodingException {
        return newBufferWriter(dirPath + currentCityAndPostcode + "/" + currentStreet + ".txt");
    }

    BufferedWriter newStreetsInCityWriter(String currentCityAndPostcode, String dirPath) throws FileNotFoundException, UnsupportedEncodingException {
        return newBufferWriter(dirPath+currentCityAndPostcode+"/streets.txt");
    }

    BufferedWriter newBufferWriter(String dirPath) throws FileNotFoundException, UnsupportedEncodingException {
        File file = new File(dirPath);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
        return writer;
    }

    void makeCityDir(String currentCityAndPostcode,String dirPath){
        File cityDir = new File(dirPath + currentCityAndPostcode);
        cityDir.mkdir();
    }

    void deleteDirectoryRecursion(File file) throws IOException {
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

    //getting a generic textfile from a given path
    ArrayList<String> getTextFile(String filepath){
        try {
            BufferedReader reader= new BufferedReader(new InputStreamReader(
                    new FileInputStream(filepath),"UTF-8"));
            ArrayList<String> textFile = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null){
                textFile.add(line);
            }
            return textFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //<--------Helper methods for getting specific kinds of textfiles--------------->
    ArrayList<String> getCities(Model model, String country){
        return model.getTextHandler().getTextFile("data/"+country+"/cities.txt");
    }

    ArrayList<String> getStreetsInCity(String country, String city, String postcode, Model model){
        return model.getTextHandler().getTextFile("data/"+country+"/"+city+" QQQ "+postcode+"/streets.txt");
    }

    ArrayList<String> getDefault(String country) {
        return getTextFile("data/"+country+"/streets.txt");
    }
    //<----------------------------------------------------------------------------->



    void ParseWayColors(Model model){
        ArrayList<String> cases = getTextFile(model.getCurrentTypeColorTxt());
        model.clearColors();
        int m = Integer.parseInt(cases.get(0));
        for (int i = 1; i < m; i++) {
            String[] strArr = cases.get(i).split(" ");
            model.addTypeColors(strArr);
        }
        model.notifyColorObservers();
    }


    //todo rewrite this
    HashMap<WayType,ResizingArray<String[]>> parseWayTypeCases(String pathToCasesFile){
        HashMap<WayType,ResizingArray<String[]>>  wayTypeCases = new HashMap<>();
        ArrayList<String> cases = getTextFile(pathToCasesFile);
        String wayCase = "";
        WayType wayType = null;
        for(int i = 0; i < cases.size() ; i++) {
                wayCase = cases.get(i);
                if(wayCase.startsWith("$")) {
                    wayType = WayType.valueOf(cases.get(i+1));
                    i++;
                }else{
                    String[] tokens = wayCase.split(" ");
                    if(wayTypeCases.get(wayType)==null){
                        wayTypeCases.put(wayType,new ResizingArray<>());
                    }
                    wayTypeCases.get(wayType).add(new String[]{tokens[0],tokens[1]});
                }
            }
            return wayTypeCases;
    }
}