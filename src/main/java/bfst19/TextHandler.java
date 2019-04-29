package bfst19;

import java.io.*;
import java.util.ArrayList;

class TextHandler {

    void makeDatabase(Model model, ArrayList<Address> addresses, String datasetName){
        File countryDir = new File("data/"+datasetName);
        countryDir.mkdir();
        String currentCityAndPostcode = "";
        String currentStreet = "";
        try {
            //this first step looks ugly and is perhaps unnecessary
            BufferedWriter allStreetsInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetName+"/streets.txt")),"UTF-8"));
            BufferedWriter streetsInCityWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetName+"/"+currentCityAndPostcode+"/streets.txt")),"UTF-8"));
            BufferedWriter citiesInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetName+"/cities.txt")),"UTF-8"));
            File streetFile = new File("data/"+datasetName+"/"+currentCityAndPostcode+"/"+currentStreet+".txt");
            BufferedWriter addressesInStreetWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
            for(Address address:addresses) {
                //if the streetName remains the same, and the city changes we need to change the writers for streets and addresses,
                //along with writing to the appropriate files, we also change the current city and postcode, and make the directory for it
                //todo fix code dupes here
                if (address.getStreetName().equals(currentStreet) && !(address.getCity() + model.getDelimeter() + address.getPostcode()).equals(currentCityAndPostcode)) {
                    currentCityAndPostcode = address.getCity() + " QQQ " + address.getPostcode();
                    File cityDir = new File("data/" + datasetName + "/" + currentCityAndPostcode);
                    cityDir.mkdir();
                    File streetsInCityFile = new File("data/" + datasetName + "/" + currentCityAndPostcode + "/streets.txt");
                    streetFile = new File("data/" + datasetName + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
                    streetsInCityWriter.flush();
                    addressesInStreetWriter.flush();
                    //because the addresses are sorted by their streetnames first, we need to accommodate changing cities many times.
                    streetsInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetsInCityFile, true), "UTF-8"));
                    addressesInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
                    citiesInCountryWriter.write(currentCityAndPostcode + "\n");
                    streetsInCityWriter.write(currentStreet + "\n");
                    allStreetsInCountryWriter.write(currentStreet + model.getDelimeter() + currentCityAndPostcode + "\n");
                } else {
                    //if the city changes, flush the writers and change the writer for the streets in that city,
                    // write to the file with all the cities and make the cities directory, also change the current city and postcode
                    if (!(address.getCity() + model.getDelimeter() + address.getPostcode()).equals(currentCityAndPostcode)) {
                        currentCityAndPostcode = address.getCity() + " QQQ " + address.getPostcode();
                        File cityDir = new File("data/" + datasetName + "/" + currentCityAndPostcode);
                        cityDir.mkdir();
                        File streetsInCityFile = new File("data/" + datasetName + "/" + currentCityAndPostcode + "/streets.txt");
                        streetsInCityWriter.flush();
                        //because the addresses are sorted by their streetnames first, we need to accommodate changing cities many times.
                        streetsInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetsInCityFile, true), "UTF-8"));
                        citiesInCountryWriter.write(currentCityAndPostcode + "\n");
                    }
                    //if the addresses street is different, make a new street file, write to that city's streets.txt file and change the current street.
                    if (!address.getStreetName().equals(currentStreet)) {
                        currentStreet = address.getStreetName();
                        streetFile = new File("data/" + datasetName + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
                        addressesInStreetWriter.flush();
                        addressesInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
                        streetsInCityWriter.write(currentStreet + "\n");
                        allStreetsInCountryWriter.write(currentStreet + model.getDelimeter() + currentCityAndPostcode + "\n");
                    }
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

    ArrayList<String> getCities(Model model, String country){
        return model.textHandler.getTextFile("data/"+country+"/cities.txt");
    }

    ArrayList<String> getStreetsInCity(String country, String city, String postcode, Model model){
        return model.textHandler.getTextFile("data/"+country+"/"+city+" QQQ "+postcode+"/streets.txt");
    }

    //generalized getCities and getStreets to getTextFile, might not be final.
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

    ArrayList<String> getDefault(String country) {
        return getTextFile("data/"+country+"/streets.txt");
    }


    void ParseWayColors(Model model){
        ArrayList<String> cases = getTextFile(model.CurrentTypeColorTxt);
        model.typeColors.clear();
        int m = Integer.parseInt(cases.get(0));
        for (int i = 1; i < m; i++) {
            String[] strArr = cases.get(i).split(" ");
            model.typeColors.add(strArr[0]);
            model.typeColors.add(strArr[1]);
        }
        model.notifyColorObservers();
    }

    void parseWayTypeCases(String pathToCasesFile, Model model){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToCasesFile),"UTF-8"));
            int n = Integer.parseInt(in.readLine().trim());
            for(int i = 0; i < n ; i++) {
                String wayType = in.readLine();
                String wayCase = in.readLine();

                while((wayCase != null) && !(wayCase.startsWith("$"))){
                    String[] tokens = wayCase.split(" ");
                    if(model.wayTypeCases.get(wayType)==null){
                        model.wayTypeCases.put(wayType,new ArrayList<>());
                    }
                    model.wayTypeCases.get(wayType).add(new String[]{tokens[0],tokens[1]});
                    wayCase = in.readLine();
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}