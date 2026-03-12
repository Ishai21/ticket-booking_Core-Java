package ticket_booking.entities;

import java.util.Date;
import java.util.List;

public class Ticket {
    private String ticketId;

    private String userId;

    private String source;

    private String destination;

    private Date dateOfTravel;

    // JSON contains an array for "train", map it to a list here
    private List<Train> train;

    // store seat coordinates when booking so cancellation can free the seat
    private Integer seatRow;
    private Integer seatCol;


    public String getTicketInfo(){
        String trainInfo = "";
        if (train != null && !train.isEmpty() && train.get(0) != null) {
            trainInfo = train.get(0).getTrainInfo();
        }
        String seatInfo = "";
        if (seatRow != null && seatCol != null) {
            seatInfo = String.format(" Seat: row %d col %d.", seatRow, seatCol);
        }
        return String.format("Ticket ID: %s belongs to User %s from %s to %s on %s %s%s", ticketId, userId, source, destination, dateOfTravel, trainInfo, seatInfo);
    }




    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getDateOfTravel() {
        return dateOfTravel;
    }

    public void setDateOfTravel(Date dateOfTravel) {
        this.dateOfTravel = dateOfTravel;
    }

    public List<Train> getTrain() {
        return train;
    }

    public void setTrain(List<Train> train) {
        this.train = train;
    }

    public Integer getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(Integer seatRow) {
        this.seatRow = seatRow;
    }

    public Integer getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(Integer seatCol) {
        this.seatCol = seatCol;
    }
}