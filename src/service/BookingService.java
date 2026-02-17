package service;

import dao.BookingDAO;
import dao.RideDAO;
import db.DBConnection;
import model.Booking;
import model.Ride;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RideDAO rideDAO = new RideDAO();

    public Booking bookRide(int rideId, int userId, int seatsRequested) throws SQLException {
        if (seatsRequested <= 0) {
            throw new IllegalArgumentException("Seats requested must be greater than zero");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Ride ride = rideDAO.getRideForBooking(connection, rideId);
                if (ride == null) {
                    throw new IllegalArgumentException("Ride not available");
                }

                if (seatsRequested > ride.getSeats()) {
                    throw new IllegalArgumentException("Not enough seats available");
                }

                int totalFare = seatsRequested * ride.getFarePerSeat();
                int bookingId = bookingDAO.bookRide(connection,
                        new Booking(rideId, userId, seatsRequested, totalFare));
                rideDAO.reduceSeats(connection, rideId, seatsRequested);

                connection.commit();
                return new Booking(bookingId, rideId, userId, seatsRequested, totalFare);
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void cancelBooking(int bookingId, int loggedInUserId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(connection, bookingId);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking not found");
                }
                if (booking.getUserId() != loggedInUserId) {
                    throw new IllegalArgumentException("You can cancel only your own booking");
                }

                bookingDAO.deleteBooking(connection, bookingId);
                rideDAO.rollbackSeats(connection, booking.getRideId(), booking.getSeatsBooked());

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Booking> showMyBookings(int userId) throws SQLException {
        return bookingDAO.getBookingsByUser(userId);
    }

}
