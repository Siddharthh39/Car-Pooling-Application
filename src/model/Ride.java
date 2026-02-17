package model;

public class Ride {

    private int id;
    private int ownerId;
    private String source;
    private String destination;
    private int seats;
    private int farePerSeat;
    private String status;

    public Ride(int ownerId, String source, String destination,
                int seats, int farePerSeat) {
        this.ownerId = ownerId;
        this.source = source;
        this.destination = destination;
        this.seats = seats;
        this.farePerSeat = farePerSeat;
        this.status = "OPEN";
    }

    public Ride(int id, int ownerId, String source, String destination,
                int seats, int farePerSeat, String status) {
        this.id = id;
        this.ownerId = ownerId;
        this.source = source;
        this.destination = destination;
        this.seats = seats;
        this.farePerSeat = farePerSeat;
        this.status = status;
    }

    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public int getSeats() { return seats; }
    public int getFarePerSeat() { return farePerSeat; }
    public String getStatus() { return status; }
}
