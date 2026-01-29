package dao;

import db.DBConnection;
import model.Ride;

import java.sql.*;

public class RideDAO {

    // Publish ride
    public void publishRide(Ride ride) throws Exception {

        String sql = """
            INSERT INTO rides(owner_id, source, destination, seats, fare_per_seat, status)
            VALUES (?, ?, ?, ?, ?, 'OPEN')
        """;

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, ride.getOwnerId());
        ps.setString(2, ride.getSource());
        ps.setString(3, ride.getDestination());
        ps.setInt(4, ride.getSeats());
        ps.setInt(5, ride.getFarePerSeat());

        ps.executeUpdate();
    }

    // View all available rides
    public void viewAllRides() throws Exception {

        String sql = "SELECT * FROM rides WHERE status='OPEN' AND seats > 0";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();

        System.out.println("\nID | FROM → TO | SEATS | FARE");
        System.out.println("--------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("id") + " | " +
                            rs.getString("source") + " → " +
                            rs.getString("destination") + " | " +
                            rs.getInt("seats") + " | ₹" +
                            rs.getInt("fare_per_seat")
            );
        }
    }

    // Search rides by source & destination
    public void searchRides(String source, String destination) throws Exception {

        String sql = """
            SELECT * FROM rides
            WHERE source=? AND destination=?
            AND status='OPEN' AND seats > 0
        """;

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, source);
        ps.setString(2, destination);

        ResultSet rs = ps.executeQuery();

        System.out.println("\nAVAILABLE RIDES");
        System.out.println("ID | FROM → TO | SEATS | FARE");
        System.out.println("--------------------------------");

        boolean found = false;

        while (rs.next()) {
            found = true;
            System.out.println(
                    rs.getInt("id") + " | " +
                            rs.getString("source") + " → " +
                            rs.getString("destination") + " | " +
                            rs.getInt("seats") + " | ₹" +
                            rs.getInt("fare_per_seat")
            );
        }

        if (!found) {
            System.out.println("❌ No matching rides found");
        }
    }

    // Get ride info for booking
    public ResultSet getRideForBooking(int rideId) throws Exception {

        String sql = "SELECT seats, fare_per_seat FROM rides WHERE id=? AND status='OPEN'";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, rideId);

        return ps.executeQuery();
    }

    // Reduce seats after booking
    public void reduceSeats(int rideId, int seats) throws Exception {

        String sql = "UPDATE rides SET seats = seats - ? WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, seats);
        ps.setInt(2, rideId);
        ps.executeUpdate();
    }

    // Rollback seats (on cancel)
    public void rollbackSeats(int rideId, int seats) throws Exception {

        String sql = "UPDATE rides SET seats = seats + ? WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, seats);
        ps.setInt(2, rideId);
        ps.executeUpdate();
    }

    // Cancel ride (owner)
    public void cancelRide(int rideId) throws Exception {

        String sql = "UPDATE rides SET status='CANCELLED' WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, rideId);

        ps.executeUpdate();
    }

    // View rides published by user
    public void viewUserRides(int userId) throws Exception {

        String sql = "SELECT * FROM rides WHERE owner_id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        System.out.println("\nMY RIDES");
        System.out.println("ID | FROM → TO | SEATS | STATUS | FARE");
        System.out.println("--------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("id") + " | " +
                            rs.getString("source") + " → " +
                            rs.getString("destination") + " | " +
                            rs.getInt("seats") + " | " +
                            rs.getString("status") + " | ₹" +
                            rs.getInt("fare_per_seat")
            );
        }
    }
}
