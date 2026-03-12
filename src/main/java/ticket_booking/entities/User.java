package ticket_booking.entities;

import java.util.List;

public class User {

    private String name;

    private String userId;

    private String password;

    private String HashPassword;

    private List<Ticket> ticketsBooked;

    public User(){}

    public User(String name, String password, String hashPassword, List<Ticket> ticketsBooked, String userId) {
        this.name = name;
        this.userId = userId;
        this.password = password;
        HashPassword = hashPassword;
        this.ticketsBooked = ticketsBooked;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHashPassword() {
        return HashPassword;
    }

    public void setHashPassword(String hashPassword) {
        HashPassword = hashPassword;
    }

    public List<Ticket> getTicketsBooked() {
        return ticketsBooked;
    }

    public void printTicketsBooked(){
        for(int i = 0; i < ticketsBooked.size(); i++){
            System.out.println(ticketsBooked.get(i).getTicketInfo());
        }
    }

    public void setTicketsBooked(List<Ticket> ticketsBooked) {
        this.ticketsBooked = ticketsBooked;
    }

}
