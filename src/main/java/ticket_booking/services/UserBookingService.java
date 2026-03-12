package ticket_booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket_booking.entities.Ticket;
import ticket_booking.entities.Train;
import ticket_booking.entities.User;
import ticket_booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {
    private User user;

    private List<User> userList;

    // Configure ObjectMapper to map snake_case JSON to camelCase Java properties
    private ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Resolve path relative to project root so running via Gradle or IDE finds the file
    private static final String USERS_PATH = System.getProperty("user.dir") + "/src/main/java/ticket_booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserFromFile();
    }

    public UserBookingService() throws IOException{
        loadUserFromFile();
    }

    public void loadUserFromFile() throws IOException{
        File users = new File(USERS_PATH);
        userList = objectMapper.readValue(users, new TypeReference<List<User>>(){});
    }

    public Boolean loginUser(){
        if (user == null) return Boolean.FALSE;
        Optional<User> foundUser = userList.stream().filter(user1 ->
                user1.getName().equalsIgnoreCase(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashPassword())).findFirst();

        if(foundUser.isPresent()){
            // replace the user with the one from file (so it has tickets, userId, etc.)
            this.user = foundUser.get();
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    public Boolean signUp(User user){
        try{
            userList.add(user);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch(IOException e){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException{
        File userFile = new File(USERS_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    // json -> Object (User) -> Deserialize
    // Object (User) -> json -> Serialize

    public void fetchBooking(){
        if (this.user == null) {
            System.out.println("No user logged in. Please login to view bookings.");
            return;
        }
        List<Ticket> tickets = this.user.getTicketsBooked();
        if (tickets == null || tickets.isEmpty()){
            System.out.println("You have no bookings.");
            return;
        }
        System.out.println("Your bookings:");
        for (int i = 0; i < tickets.size(); i++){
            System.out.println((i+1) + ") " + tickets.get(i).getTicketInfo());
        }
    }

    public Boolean cancelBooking(String ticketId){
        if (this.user == null) return Boolean.FALSE;
        List<Ticket> ticketsBooked = user.getTicketsBooked();
        if (ticketsBooked == null) return Boolean.FALSE;
        Optional<Ticket> ticketOpt = ticketsBooked.stream().filter(ticket1 -> ticket1.getTicketId().equals(ticketId)).findFirst();
        if(ticketOpt.isPresent()){
            Ticket ticket = ticketOpt.get();
            // Free seat on the train if seat coordinates and train id available
            try{
                if (ticket.getTrain() != null && !ticket.getTrain().isEmpty()){
                    Train bookedTrain = ticket.getTrain().get(0);
                    if (bookedTrain != null && ticket.getSeatRow() != null && ticket.getSeatCol() != null){
                        TrainService trainService = new TrainService();
                        boolean freed = trainService.freeSeat(bookedTrain.getTrainId(), ticket.getSeatRow(), ticket.getSeatCol());
                        if(!freed){
                            System.out.println("Warning: unable to free seat on train " + bookedTrain.getTrainId());
                        }
                    }
                }
            }catch(IOException e){
                // log and continue with removal
                System.out.println("Warning: error while freeing seat: " + e.getMessage());
            }

            // remove ticket from user's list and persist
            ticketsBooked.remove(ticket);
            // Update the corresponding user in the userList and save the full users.json
            for (User u : userList) {
                if (u.getUserId() != null && u.getUserId().equals(this.user.getUserId())){
                    u.setTicketsBooked(ticketsBooked);
                    break;
                }
            }
            try {
                saveUserListToFile();
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    // Add a ticket to the logged-in user and persist the users.json
    public Boolean addTicket(Ticket ticket){
        if (this.user == null) return Boolean.FALSE;
        List<Ticket> tickets = this.user.getTicketsBooked();
        if (tickets == null){
            tickets = new ArrayList<>();
            this.user.setTicketsBooked(tickets);
        }
        tickets.add(ticket);
        for (User u : userList) {
            if (u.getUserId() != null && u.getUserId().equals(this.user.getUserId())){
                u.setTicketsBooked(tickets);
                break;
            }
        }
        try{
            saveUserListToFile();
            return Boolean.TRUE;
        }catch(IOException e){
            return Boolean.FALSE;
        }
    }

    public boolean isLoggedIn(){
        return this.user != null;
    }

    // Return currently logged-in user (or the user object set during login flow).
    public User getCurrentUser(){
        return this.user;
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);

        }catch(IOException e){
            return new ArrayList<>();
        }
    }

    // Return all available stations by delegating to TrainService
    public List<String> getAllStations(){
        try{
            TrainService trainService = new TrainService();
            return trainService.getAllStations();
        }catch(IOException e){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

}
