package dao;

import db.DBConnection;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public int bookRide(Connection con, Booking booking) throws SQLException {

        String sql = "INSERT INTO bookings(ride_id, user_id, seats_booked, total_fare) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getRideId());
            ps.setInt(2, booking.getUserId());
            ps.setInt(3, booking.getSeatsBooked());
            ps.setInt(4, booking.getTotalFare());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create booking");
    }

    public Booking getBookingById(Connection con, int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
                return null;
            }
        }
    }

    public void deleteBooking(Connection con, int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    public List<Booking> getBookingsByUser(int userId) throws SQLException {
        String sql = "SELECT id, ride_id, user_id, seats_booked, total_fare FROM bookings WHERE user_id=? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(mapBooking(rs));
                }
                return bookings;
            }
        }
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("ride_id"),
                rs.getInt("user_id"),
                rs.getInt("seats_booked"),
                rs.getInt("total_fare")
        );
    }
}
