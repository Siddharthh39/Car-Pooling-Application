package dao;

import db.DBConnection;
import model.Ride;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RideDAO {

    public int publishRide(Ride ride) throws SQLException {

        String sql = "INSERT INTO rides(owner_id, source, destination, seats, fare_per_seat, status) VALUES (?, ?, ?, ?, ?, 'OPEN')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ride.getOwnerId());
            ps.setString(2, ride.getSource());
            ps.setString(3, ride.getDestination());
            ps.setInt(4, ride.getSeats());
            ps.setInt(5, ride.getFarePerSeat());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to publish ride");
    }

    public List<Ride> viewAllRides() throws SQLException {
        String sql = "SELECT * FROM rides WHERE status='OPEN' AND seats > 0 ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Ride> rides = new ArrayList<>();
            while (rs.next()) {
                rides.add(mapRide(rs));
            }
            return rides;
        }
    }

    public List<Ride> searchRides(String source, String destination) throws SQLException {
        String sql = "SELECT * FROM rides WHERE source=? AND destination=? AND status='OPEN' AND seats > 0 ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, destination);

            try (ResultSet rs = ps.executeQuery()) {
                List<Ride> rides = new ArrayList<>();
                while (rs.next()) {
                    rides.add(mapRide(rs));
                }
                return rides;
            }
        }
    }

    public Ride getRideForBooking(Connection con, int rideId) throws SQLException {
        String sql = "SELECT * FROM rides WHERE id=? AND status='OPEN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rideId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRide(rs);
                }
                return null;
            }
        }
    }

    public void reduceSeats(Connection con, int rideId, int seats) throws SQLException {
        String sql = "UPDATE rides SET seats = seats - ? WHERE id=? AND seats >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seats);
            ps.setInt(2, rideId);
            ps.setInt(3, seats);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Not enough seats available for booking");
            }
        }
    }

    public void rollbackSeats(Connection con, int rideId, int seats) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE rides SET seats = seats + ? WHERE id=?")) {
            ps.setInt(1, seats);
            ps.setInt(2, rideId);
            ps.executeUpdate();
        }
    }

    public boolean cancelRide(int rideId, int ownerId) throws SQLException {
        String sql = "UPDATE rides SET status='CANCELLED' WHERE id=? AND owner_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rideId);
            ps.setInt(2, ownerId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Ride> viewUserRides(int userId) throws SQLException {
        String sql = "SELECT * FROM rides WHERE owner_id=? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Ride> rides = new ArrayList<>();
                while (rs.next()) {
                    rides.add(mapRide(rs));
                }
                return rides;
            }
        }
    }

    private Ride mapRide(ResultSet rs) throws SQLException {
        return new Ride(
                rs.getInt("id"),
                rs.getInt("owner_id"),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getInt("seats"),
                rs.getInt("fare_per_seat"),
                rs.getString("status")
        );
    }
}
