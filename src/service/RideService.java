package service;

import dao.RideDAO;
import model.Ride;

import java.sql.SQLException;
import java.util.List;

public class RideService {

    private final RideDAO rideDAO = new RideDAO();

    public int publish(int ownerId,
                       String source,
                       String destination,
                       int seats,
                       int farePerSeat) throws SQLException {

        Ride ride = new Ride(ownerId, source, destination, seats, farePerSeat);
        return rideDAO.publishRide(ride);
    }

    public List<Ride> viewAll() throws SQLException {
        return rideDAO.viewAllRides();
    }

    public List<Ride> search(String source, String destination) throws SQLException {
        return rideDAO.searchRides(source, destination);
    }

    public boolean cancel(int rideId, int ownerId) throws SQLException {
        return rideDAO.cancelRide(rideId, ownerId);
    }

    public List<Ride> viewMyRides(int userId) throws SQLException {
        return rideDAO.viewUserRides(userId);
    }
}
