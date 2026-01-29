package dao;

import db.DBConnection;
import model.Booking;

import java.sql.*;

public class BookingDAO {

    // Book ride
    public void bookRide(Booking booking) throws Exception {

        String sql = """
            INSERT INTO bookings(ride_id, user_id, seats_booked, total_fare)
            VALUES (?, ?, ?, ?)
        """;

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, booking.getRideId());
        ps.setInt(2, booking.getUserId());
        ps.setInt(3, booking.getSeatsBooked());
        ps.setInt(4, booking.getTotalFare());

        ps.executeUpdate();
    }

    // Get booking info (for cancel)
    public ResultSet getBooking(int bookingId) throws Exception {

        String sql = "SELECT * FROM bookings WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookingId);

        return ps.executeQuery();
    }

    // Cancel booking
    public void cancelBooking(int bookingId) throws Exception {

        String sql = "DELETE FROM bookings WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookingId);

        ps.executeUpdate();
    }
    public ResultSet getBookingById(int bookingId) throws Exception {

        String sql = "SELECT * FROM bookings WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookingId);

        return ps.executeQuery();
    }

    // Delete booking
    public void deleteBooking(int bookingId) throws Exception {

        String sql = "DELETE FROM bookings WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookingId);

        ps.executeUpdate();
    }

    public ResultSet getBookingsByUser(int userId) throws Exception {

        String sql = """
        SELECT b.id, b.ride_id, b.seats_booked, b.total_fare,
               r.source, r.destination
        FROM bookings b
        JOIN rides r ON b.ride_id = r.id
        WHERE b.user_id = ?
    """;

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);

        return ps.executeQuery();
    }

}
