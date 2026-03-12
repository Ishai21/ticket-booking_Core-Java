package ticket_booking;

import ticket_booking.entities.Train;
import ticket_booking.entities.User;
import ticket_booking.entities.Ticket;
import ticket_booking.services.UserBookingService;
import ticket_booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        System.out.println("Running Train Ticket Booking System...");

        Scanner scanner = new Scanner(System.in);

        int option = 0;
        UserBookingService userBookingService;
        // Persist selected train across menu iterations
        Train trainSelectedForBooking = null;
        String lastSource = null;
        String lastDest = null;

        try{
            userBookingService = new UserBookingService();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Error while loading user data");
            return;
        }
        while(option != 7){
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            System.out.println("8. Logout");

            // Protect against EOF/no stdin (Gradle run not forwarding piped stdin)
            if (!scanner.hasNextInt()) {
                System.out.println("No input available. Exiting.");
                break;
            }
            option = scanner.nextInt();

            switch (option){
                case 1:
                    System.out.println("Enter the username to signup");
                    if(!scanner.hasNext()) { System.out.println("No username provided. Returning to menu."); break; }
                    String nameToSignUp = scanner.next();
                    System.out.println("Enter the password to signup");
                    if(!scanner.hasNext()) { System.out.println("No password provided. Returning to menu."); break; }
                    String passwordToSignUp = scanner.next();
                    User userToSignup = new User(nameToSignUp, passwordToSignUp, UserServiceUtil.hashPassword(passwordToSignUp), new ArrayList<>(), UUID.randomUUID().toString());
                    Boolean signupResult = userBookingService.signUp(userToSignup);
                    if (signupResult.equals(Boolean.TRUE)){
                        System.out.println("Sign up successful. You can now login.");
                    } else {
                        System.out.println("Sign up failed. Please try again.");
                    }
                    break;
                case 2:
                    System.out.println("Enter the username to Login");
                    if(!scanner.hasNext()) { System.out.println("No username provided. Returning to menu."); break; }
                    String nameToLogin = scanner.next();
                    System.out.println("Enter the password to login");
                    if(!scanner.hasNext()) { System.out.println("No password provided. Returning to menu."); break; }
                    String passwordToLogin = scanner.next();
                    User userToLogin = new User(nameToLogin, passwordToLogin, UserServiceUtil.hashPassword(passwordToLogin), new ArrayList<>(), UUID.randomUUID().toString());
                    try{
                        UserBookingService tmp = new UserBookingService(userToLogin);
                        if (tmp.loginUser().equals(Boolean.TRUE)){
                            userBookingService = tmp; // now logged in
                            System.out.println("Login successful.");
                        } else {
                            System.out.println("Invalid credentials. Login failed.");
                        }
                    }catch (IOException ex){
                        System.out.println("Error during login. Please try again.");
                    }
                    break;
                case 3:
                    System.out.println("Fetching your bookings");
                    userBookingService.fetchBooking();
                    break;
                case 4:
                    // New flow: show available stations to choose from
                    List<String> stations = userBookingService.getAllStations();
                    if (stations.isEmpty()){
                        System.out.println("No station data available. Please ensure trains.json has station information.");
                        break;
                    }
                    System.out.println("Available stations:");
                    for (int i = 0; i < stations.size(); i++){
                        System.out.println((i+1) + ") " + stations.get(i));
                    }
                    System.out.println("Select source station by number:");
                    if(!scanner.hasNextInt()){ System.out.println("Invalid input. Returning to menu."); break; }
                    int srcIdx = scanner.nextInt() - 1;
                    if (srcIdx < 0 || srcIdx >= stations.size()){ System.out.println("Invalid selection. Returning to menu."); break; }
                    System.out.println("Select destination station by number:");
                    if(!scanner.hasNextInt()){ System.out.println("Invalid input. Returning to menu."); break; }
                    int destIdx = scanner.nextInt() - 1;
                    if (destIdx < 0 || destIdx >= stations.size()){ System.out.println("Invalid selection. Returning to menu."); break; }
                    String source = stations.get(srcIdx);
                    String dest = stations.get(destIdx);
                    if (source.equalsIgnoreCase(dest)) { System.out.println("Source and destination cannot be the same. Returning to menu."); break; }
                    lastSource = source;
                    lastDest = dest;
                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains == null || trains.isEmpty()) {
                        System.out.println("No trains found for the given route.");
                        break;
                    }

                    int index = 1;
                    for (Train t: trains){
                        System.out.println(index + ": Train id : "+t.getTrainId());
                        for (Map.Entry<String, String> entry: t.getStationTimes().entrySet()){
                            System.out.println("  station " + entry.getKey() + " time: " + entry.getValue());
                        }
                        index++;
                    }
                    System.out.println("Select a train by typing 1,2,3...");
                    if(!scanner.hasNextInt()) { System.out.println("No selection provided. Returning to menu."); break; }
                    int chosen = scanner.nextInt();
                    int chosenIndex = chosen - 1; // convert 1-based to 0-based
                    if (chosenIndex < 0 || chosenIndex >= trains.size()) {
                        System.out.println("Invalid selection. Please try Search Trains again.");
                    } else {
                        trainSelectedForBooking = trains.get(chosenIndex);
                        System.out.println("Selected train: " + trainSelectedForBooking.getTrainId());
                    }
                    break;
                case 5:
                    if (trainSelectedForBooking == null || trainSelectedForBooking.getTrainId() == null) {
                        System.out.println("No train selected. Please search and select a train first (option 4).");
                        break;
                    }
                    System.out.println("Select a seat out of these seats (0 = available, 1 = booked)");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    // Print header columns
                    System.out.print("   ");
                    for (int c = 0; c < seats.get(0).size(); c++){
                        System.out.print("col"+c+" ");
                    }
                    System.out.println();
                    for (int r = 0; r < seats.size(); r++){
                        System.out.print("row"+r+": ");
                        for (int c = 0; c < seats.get(r).size(); c++){
                            System.out.print(seats.get(r).get(c)+"    ");
                        }
                        System.out.println();
                    }

                    System.out.println("Select the seat by typing the row and column");
                    System.out.println("Enter the row");
                    if(!scanner.hasNextInt()) { System.out.println("No row provided. Returning to menu."); break; }
                    int selRow = scanner.nextInt();
                    System.out.println("Enter the column");
                    if(!scanner.hasNextInt()) { System.out.println("No column provided. Returning to menu."); break; }
                    int selCol = scanner.nextInt();
                    System.out.println("Booking your seat....");
                    Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, selRow, selCol);
                    if(booked.equals(Boolean.TRUE)){
                        // create ticket and persist to user
                        // always create a ticket object and show its id so user has a reference
                        Ticket ticket = new Ticket();
                        ticket.setTicketId(UUID.randomUUID().toString());
                        ticket.setUserId(userBookingService.isLoggedIn() && userBookingService.getCurrentUser() != null ? userBookingService.getCurrentUser().getUserId() : null);
                        ticket.setSource(lastSource == null ? "" : lastSource);
                        ticket.setDestination(lastDest == null ? "" : lastDest);
                        ticket.setDateOfTravel(new Date());
                        List<Train> trainList = new ArrayList<>();
                        trainList.add(trainSelectedForBooking);
                        ticket.setTrain(trainList);
                        // record seat coordinates for later cancellation
                        ticket.setSeatRow(selRow);
                        ticket.setSeatCol(selCol);
                        if (userBookingService.isLoggedIn()){
                            Boolean added = userBookingService.addTicket(ticket);
                            if (added.equals(Boolean.TRUE)){
                                System.out.println("Booked! Enjoy your journey. Ticket id: " + ticket.getTicketId());
                            } else {
                                System.out.println("Seat booked but failed to save ticket to user. Ticket id: " + ticket.getTicketId());
                            }
                        } else {
                            System.out.println("Booked! (You are not logged in; ticket not saved to account.) Ticket id: " + ticket.getTicketId());
                        }
                     }else{
                         System.out.println("Can't book this seat");
                     }
                    break;
                case 6:
                    if (!userBookingService.isLoggedIn() || userBookingService.getCurrentUser() == null) {
                        System.out.println("Enter the ticket id to cancel");
                        if(!scanner.hasNext()) { System.out.println("No ticket id provided. Returning to menu."); break; }
                        String ticketId = scanner.next();
                        Boolean cancelled = userBookingService.cancelBooking(ticketId);
                        if(cancelled.equals(Boolean.TRUE)){
                            System.out.println("Cancelled! We hope to see you again");
                        }else{
                            System.out.println("Can't cancel this booking");
                        }
                    } else {
                        // show user's tickets and let them choose by number
                        List<Ticket> myTickets = userBookingService.getCurrentUser().getTicketsBooked();
                        if (myTickets == null || myTickets.isEmpty()){
                            System.out.println("You have no bookings to cancel.");
                            break;
                        }
                        System.out.println("Your bookings:");
                        for (int i = 0; i < myTickets.size(); i++){
                            System.out.println((i+1) + ") " + myTickets.get(i).getTicketInfo());
                        }
                        System.out.println("Select booking number to cancel:");
                        if (!scanner.hasNextInt()){ System.out.println("Invalid input. Returning to menu."); break; }
                        int cancelIdx = scanner.nextInt() - 1;
                        if (cancelIdx < 0 || cancelIdx >= myTickets.size()){ System.out.println("Invalid selection. Returning to menu."); break; }
                        String ticketIdToCancel = myTickets.get(cancelIdx).getTicketId();
                        Boolean cancelled2 = userBookingService.cancelBooking(ticketIdToCancel);
                        if (cancelled2.equals(Boolean.TRUE)){
                            System.out.println("Cancelled! We hope to see you again");
                        } else {
                            System.out.println("Can't cancel this booking");
                        }
                    }
                    break;
                case 8:
                    // Logout: reset the userBookingService to a no-user instance
                    if (!userBookingService.isLoggedIn()){
                        System.out.println("No user is currently logged in.");
                        break;
                    }
                    try{
                        userBookingService = new UserBookingService();
                        System.out.println("Logout successful.");
                    }catch(IOException e){
                        System.out.println("Logout failed due to an internal error.");
                    }
                    break;
                case 7:
                    System.out.println("Exit successful. Thank you for using our service.");
                    break;
                default:
                    System.out.println("Invalid option. Please choose a number between 1 and 8.");
                    break;
            }
        }
    }
}