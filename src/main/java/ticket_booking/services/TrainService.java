package ticket_booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket_booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {
    private Train train;

    private List<Train> trainList;

    // Configure ObjectMapper to map snake_case JSON to camelCase Java properties and ignore unknowns
    private ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // default filenames
    private static final String TRAINS_FILENAME = "trains.json";

    public TrainService() throws IOException{
        loadTrains();
    }

    public TrainService(Train train) throws IOException {
        this.train = train;
        loadTrains();
    }

    // Try to load trains from classpath resource first, then fallback to common file locations
    private List<Train> readTrainsFromSources() throws IOException{
        // try classpath resources
        InputStream is = TrainService.class.getClassLoader().getResourceAsStream("ticket_booking/localDb/" + TRAINS_FILENAME);
        if (is == null) is = TrainService.class.getClassLoader().getResourceAsStream("localDb/" + TRAINS_FILENAME);
        if (is != null){
            return objectMapper.readValue(is, new TypeReference<List<Train>>(){});
        }
        // try typical filesystem locations
        String[] paths = new String[]{
                System.getProperty("user.dir") + "/localDb/" + TRAINS_FILENAME,
                System.getProperty("user.dir") + "/src/main/java/ticket_booking/localDb/" + TRAINS_FILENAME,
                System.getProperty("user.dir") + "/src/main/resources/localDb/" + TRAINS_FILENAME
        };
        for (String p: paths){
            File f = new File(p);
            if (f.exists()){
                return objectMapper.readValue(f, new TypeReference<List<Train>>(){});
            }
        }
        // not found
        throw new IOException("Could not locate " + TRAINS_FILENAME + " in classpath or known locations.");
    }

    private void loadTrains() throws IOException{
        try{
            this.trainList = readTrainsFromSources();
            System.out.println("Loaded " + (this.trainList == null ? 0 : this.trainList.size()) + " trains from trains.json");
        }catch(IOException ex){
            // If not found or bad format, initialize empty list to avoid NPEs and log
            System.out.println("Warning: could not load trains.json: " + ex.getMessage());
            this.trainList = new ArrayList<>();
        }
    }

    // Persist trains to a writable localDb folder in current working directory
    private void persistTrains() throws IOException{
        File dir = new File(System.getProperty("user.dir") + "/localDb");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, TRAINS_FILENAME);
        objectMapper.writeValue(out, this.trainList);
    }

    public List<Train> searchTrains(String source, String destination){
        return trainList.stream().filter(train1 -> validTrain(train1, source, destination)).collect(Collectors.toList());
    }

    // Return a sorted list of unique stations available across all trains
    public List<String> getAllStations() {
        if (trainList == null) return new ArrayList<>();
        Set<String> stations = new HashSet<>();
        for (Train t : trainList) {
            if (t.getStations() != null) stations.addAll(t.getStations());
        }
        List<String> list = new ArrayList<>(stations);
        list.sort(String::compareToIgnoreCase);
        return list;
    }

    public void addTrain(Train newTrain) throws IOException{
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();
        if(existingTrain.isPresent()){
            updateTrain(newTrain);
        }else{
            trainList.add(newTrain);
            persistTrains();
        }
    }

    public void updateTrain(Train updatedTrain) throws IOException {
        // Find the index of the train with the same trainId
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            // If found, replace the existing train with the updated one
            trainList.set(index.getAsInt(), updatedTrain);
            persistTrains();
        } else {
            // If not found, treat it as adding a new train
            addTrain(updatedTrain);
        }
    }


    public boolean validTrain(Train train, String source, String destination){
        List<String> stations = train.getStations();

        // Case-insensitive search for station indices
        int sourceIndex = IntStream.range(0, stations.size())
                .filter(i -> stations.get(i).equalsIgnoreCase(source))
                .findFirst().orElse(-1);

        int destinationIndex = IntStream.range(0, stations.size())
                .filter(i -> stations.get(i).equalsIgnoreCase(destination))
                .findFirst().orElse(-1);

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }

    // Find a train by its trainId (case-insensitive). Returns null if not found.
    public Train findTrainById(String trainId){
        if (trainList == null || trainId == null) return null;
        for (Train t: trainList){
            if (t.getTrainId() != null && t.getTrainId().equalsIgnoreCase(trainId)) return t;
        }
        return null;
    }

    // Free a seat for a train (set to 0). Returns true if freed and persisted, false otherwise.
    public boolean freeSeat(String trainId, int row, int col){
        try{
            Train t = findTrainById(trainId);
            if (t == null) return false;
            List<List<Integer>> seats = t.getSeats();
            if (seats == null) return false;
            if (row < 0 || row >= seats.size() || col < 0 || col >= seats.get(row).size()) return false;
            seats.get(row).set(col, 0);
            t.setSeats(seats);
            // persist updated train list
            updateTrain(t);
            return true;
        }catch(IOException ex){
            return false;
        }
    }

}
