package bfst19;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextHandler {
    private static TextHandler textHandler;
    private boolean hasInputFile;

    private TextHandler() {
    }

    public static TextHandler getInstance() {
        if (textHandler == null) {
            textHandler = new TextHandler();
        }

        return textHandler;
    }

    void setHasInputFile(boolean hasInput) {
        hasInputFile = hasInput;
    }

    void makeDatabase(ArrayList<Address> addresses, String dirPath, String delimiter) {
        try {
            File countryDir = new File(dirPath);

            if (countryDir.isDirectory()) {
                deleteDirectoryRecursion(new File(dirPath));
            }

            countryDir.mkdir();

            String currentCityAndPostcode = "";
            String currentStreet = "";

            BufferedWriter allStreetsInCountryWriter = newBufferWriter(dirPath + "/streets.txt");
            BufferedWriter citiesInCountryWriter = newBufferWriter(dirPath + "/cities.txt");
            BufferedWriter streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode, dirPath);
            BufferedWriter addressesInStreetWriter = newAddressesInStreetWriter(currentStreet, currentCityAndPostcode, dirPath);


            for (Address address : addresses) {

                if (address.getStreetName().equals(currentStreet) && !(address.getCity() + delimiter +
                        address.getPostcode()).equals(currentCityAndPostcode)) {

                    //flush writers to ensure no backlog gets deleted
                    streetsInCityWriter.flush();
                    addressesInStreetWriter.flush();

                    //redefine variables and make new dir
                    //this cannot be refactored due to multiple return values
                    currentCityAndPostcode = address.getCity() + delimiter + address.getPostcode();
                    makeCityDir(currentCityAndPostcode, dirPath);
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode, dirPath);
                    addressesInStreetWriter = newAddressesInStreetWriter(currentStreet, currentCityAndPostcode, dirPath);

                    //write to the writers respective files
                    citiesInCountryWriter.write(currentCityAndPostcode + "\n");
                    streetsInCityWriter.write(currentStreet + "\n");
                    allStreetsInCountryWriter.write(currentStreet + delimiter + currentCityAndPostcode + "\n");

                } else if (!(address.getCity() + delimiter + address.getPostcode()).equals(currentCityAndPostcode)) {

                    streetsInCityWriter.flush();

                    currentCityAndPostcode = address.getCity() + delimiter + address.getPostcode();
                    makeCityDir(currentCityAndPostcode, dirPath);
                    streetsInCityWriter = newStreetsInCityWriter(currentCityAndPostcode, dirPath);

                    citiesInCountryWriter.write(currentCityAndPostcode + "\n");

                } else if (!address.getStreetName().equals(currentStreet)) {

                    addressesInStreetWriter.flush();

                    currentStreet = address.getStreetName();

                    addressesInStreetWriter = newAddressesInStreetWriter(currentStreet, currentCityAndPostcode, dirPath);

                    streetsInCityWriter.write(currentStreet + "\n");
                    allStreetsInCountryWriter.write(currentStreet + delimiter + currentCityAndPostcode + "\n");
                }
                addressesInStreetWriter.write(address.getLat() + " " + address.getLon() + " " + address.getHouseNumber() + "\n");
            }

            allStreetsInCountryWriter.close();
            citiesInCountryWriter.close();
            streetsInCityWriter.close();
            addressesInStreetWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //<----------- Helper methods for makeDatabase ----------------------------------->

    private BufferedWriter newAddressesInStreetWriter(
            String currentStreet, String currentCityAndPostcode, String dirPath) throws FileNotFoundException {

        return newBufferWriter(dirPath + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
    }

    private BufferedWriter newStreetsInCityWriter(String currentCityAndPostcode, String dirPath) throws FileNotFoundException {

        return newBufferWriter(dirPath + "/" + currentCityAndPostcode + "/streets.txt");
    }

    private BufferedWriter newBufferWriter(String dirPath) throws FileNotFoundException {

        File file = new File(dirPath);
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
    }

    private void makeCityDir(String currentCityAndPostcode, String dirPath) {

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

    //getting a generic textfile from a given path
    private ArrayList<String> getTextFile(String filepath) {
        try {

            BufferedReader reader;

            if (!hasInputFile) {
                InputStream fileStream = getClass().getClassLoader().getResourceAsStream(filepath);
                reader = new BufferedReader(new InputStreamReader(
                        fileStream, StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(filepath), StandardCharsets.UTF_8));
            }

            ArrayList<String> textFile = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                textFile.add(line);
            }

            return textFile;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read from file at " + filepath);
        }

        return null;
    }

    //getting a config text file from a given path
    private ArrayList<String> getConfigFile(String filepath) {
        try {

            InputStream fileStream = getClass().getClassLoader().getResourceAsStream(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8));

            ArrayList<String> textFile = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                textFile.add(line);
            }

            return textFile;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read config file at " + filepath);
        }

        return null;
    }

    //<--------Helper methods for getting specific kinds of text files--------------->
    private ArrayList<String> getCities(String dirPath) {
        return getTextFile(dirPath + "/cities.txt");
    }

    ArrayList<String> getStreetsInCity(String city, String postcode) {
        return getTextFile(Model.getDirPath() + "/" + city + Model.getDelimeter() + postcode + "/streets.txt");
    }

    ArrayList<String> getAddressesOnStreet(String city, String postcode, String streetName) {
        return getTextFile(Model.getDirPath() + "/" + city + Model.getDelimeter() + postcode + "/" + streetName + ".txt");
    }

    ArrayList<String> getDefault(String dirPath) {
        return getTextFile(dirPath + "/streets.txt");
    }

    //<----------------------------------------------------------------------------->

    void parseWayColors(Model model) {
        ArrayList<String> cases = getConfigFile(model.getCurrentTypeColorTxt());
        model.clearColors();

        int m = Integer.parseInt(cases.get(0));

        for (int i = 1; i < m; i++) {
            String[] strArr = cases.get(i).split(" ");
            model.addTypeColors(strArr);
        }

        model.notifyColorObservers();
    }

    public HashMap<WayType, HashMap<String, ResizingArray<String[]>>> parseDrivableCases() {
        ArrayList<String> cases = getConfigFile("config/Drivable_cases.txt");
        HashMap<WayType, HashMap<String, ResizingArray<String[]>>> drivableCases = new HashMap<>();

        WayType wayType = WayType.valueOf(cases.get(0));
        drivableCases.put(wayType, new HashMap<>());

        String[] tokens;
        String vehicleType = "";
        String vehicleDrivable = "";

        for (int i = 1; i < cases.size(); i++) {
            String line = cases.get(i);

            if (line.startsWith("%")) {
                tokens = cases.get(i + 1).split(" ");
                i++;

                vehicleType = tokens[0];
                vehicleDrivable = tokens[1];
                drivableCases.get(wayType).put(vehicleType + " " + vehicleDrivable, new ResizingArray<>());

            } else if (line.startsWith("$")) {
                wayType = WayType.valueOf(cases.get(i + 1));
                drivableCases.put(wayType, new HashMap<>());
                i++;

            } else {
                String[] lineTokens = line.split(" ");
                drivableCases.get(wayType).get(vehicleType + " " + vehicleDrivable).add(lineTokens);
            }
        }

        return drivableCases;
    }

    public HashMap<String, Integer> parseSpeedDefaults() {
        ArrayList<String> cases = getConfigFile("config/Speed_cases.txt");
        HashMap<String, Integer> speedDefaults = new HashMap<>();

        for (String line : cases) {
            String[] tokens = line.split(" ");
            speedDefaults.put(tokens[0], Integer.valueOf(tokens[1]));
        }

        return speedDefaults;
    }


    HashMap<WayType, ResizingArray<String[]>> parseWayTypeCases() {
        HashMap<WayType, ResizingArray<String[]>> wayTypeCases = new HashMap<>();

        ArrayList<String> cases = getConfigFile("config/WayTypeCases.txt");
        String wayCase;
        WayType wayType = null;

        for (int i = 0; i < cases.size(); i++) {
            wayCase = cases.get(i);

            if (wayCase.startsWith("$")) {
                wayType = WayType.valueOf(cases.get(i + 1));
                i++;

            } else {
                String[] tokens = wayCase.split(" ");

                if (wayTypeCases.get(wayType) == null) {
                    wayTypeCases.put(wayType, new ResizingArray<>());
                }

                wayTypeCases.get(wayType).add(new String[]{tokens[0], tokens[1]});
            }
        }
        return wayTypeCases;
    }

    ArrayList<String[]> parseCitiesAndPostcodes(String dirPath) {
        ArrayList<String> citiesTextFile = getCities(dirPath);
        ArrayList<String[]> citiesAndPostcodes = new ArrayList<>();

        for (int i = 0; i < citiesTextFile.size() - 1; i++) {
            String line = citiesTextFile.get(i);
            String[] tokens = line.split(" QQQ ");
            citiesAndPostcodes.add(tokens);
        }

        return citiesAndPostcodes;
    }

    void writePointsOfInterest(String dirPath, List<PointOfInterestItem> pointsOfInterest) {
        try {
            new File("data\\" + dirPath).mkdirs();

            String poiPath;
            if (hasInputFile) {
                // this regex matches the last \ in a string
                poiPath = dirPath.replaceAll("\\\\(?!.*)$", "\\data\\") + "/pointsOfInterest.txt";
            } else {
                poiPath = "data\\" + dirPath + "/pointsOfInterest.txt";
            }

            File pointsOfInterestFile = new File(poiPath);
            if (pointsOfInterestFile.isFile()) {
                pointsOfInterestFile.delete();
            }
            BufferedWriter pointsOfInterestWriter = newBufferWriter(poiPath);

            for (PointOfInterestItem pointOfInterest : pointsOfInterest) {
                pointsOfInterestWriter.write(pointOfInterest.toString() + "\n");
            }

            pointsOfInterestWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't create an OutputStream for points of interests or failed to write to it.");
        }
    }

    List<PointOfInterestItem> getPointsOfInterest(String dirPath) {
        File databaseDir = new File(dirPath + "/pointsOfInterest.txt");
        ArrayList<PointOfInterestItem> pointsOfInterest = new ArrayList<>();

        if (databaseDir.isFile()) {
            ArrayList<String> pointOfInterestFile = textHandler.getTextFile(dirPath + "/pointsOfInterest.txt");

            for (String pointOfInterest : pointOfInterestFile) {
                String[] pointOFInterestFields = pointOfInterest.split(Model.getDelimeter());
                String address = pointOFInterestFields[0];

                float x = (float) (Float.valueOf(pointOFInterestFields[1]) * Model.getLonfactor());
                float y = Float.valueOf(pointOFInterestFields[2]);
                pointsOfInterest.add(new PointOfInterestItem(address, x, y));
            }
        }
        return pointsOfInterest;
    }
}