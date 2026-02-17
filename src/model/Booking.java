package model;

public class Booking {

    private int id;
    private int rideId;
    private int userId;
    private int seatsBooked;
    private int totalFare;

    public Booking(int rideId, int userId, int seatsBooked, int totalFare) {
        this.rideId = rideId;
        this.userId = userId;
        this.seatsBooked = seatsBooked;
        this.totalFare = totalFare;
    }

    public Booking(int id, int rideId, int userId, int seatsBooked, int totalFare) {
        this.id = id;
        this.rideId = rideId;
        this.userId = userId;
        this.seatsBooked = seatsBooked;
        this.totalFare = totalFare;
    }

    public int getId() { return id; }
    public int getRideId() { return rideId; }
    public int getUserId() { return userId; }
    public int getSeatsBooked() { return seatsBooked; }
    public int getTotalFare() { return totalFare; }
}
