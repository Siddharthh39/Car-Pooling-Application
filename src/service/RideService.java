package service;

import dao.RideDAO;
import model.Ride;

public class RideService {

    private RideDAO rideDAO = new RideDAO();

    // Publish a new ride (with fare)
    public void publish(int ownerId,
                        String source,
                        String destination,
                        int seats,
                        int farePerSeat) throws Exception {

        Ride ride = new Ride(ownerId, source, destination, seats, farePerSeat);
        rideDAO.publishRide(ride);
    }

    // View all available rides
    public void viewAll() throws Exception {
        rideDAO.viewAllRides();
    }

    // Search rides by source & destination
    public void search(String source, String destination) throws Exception {
        rideDAO.searchRides(source, destination);
    }

    // Cancel a ride (owner only – validation should be done in Main)
    public void cancel(int rideId) throws Exception {
        rideDAO.cancelRide(rideId);
    }

    // View rides published by logged-in user
    public void viewMyRides(int userId) throws Exception {
        rideDAO.viewUserRides(userId);
    }
}
