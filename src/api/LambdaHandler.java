package api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import db.DBConnection;
import model.Booking;
import model.Ride;
import model.User;
import service.BookingService;
import service.RideService;
import service.UserService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson GSON = new Gson();

    private final UserService userService = new UserService();
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String method = request.getHttpMethod() == null ? "" : request.getHttpMethod().toUpperCase();
            String path = normalizePath(request);

            if ("OPTIONS".equals(method)) {
                return jsonResponse(200, Map.of("ok", true));
            }
            if ("GET".equals(method) && "/health".equals(path)) {
                return jsonResponse(200, Map.of("status", "UP"));
            }

            DBConnection.ensureSchema();

            if ("POST".equals(method) && "/users/register".equals(path)) {
                return registerUser(request);
            }
            if ("GET".equals(method) && "/users/login".equals(path)) {
                return loginUser(request);
            }
            if ("POST".equals(method) && "/rides".equals(path)) {
                return publishRide(request);
            }
            if ("GET".equals(method) && "/rides".equals(path)) {
                return listRides(request);
            }
            if ("DELETE".equals(method) && path.startsWith("/rides/")) {
                return cancelRide(request, path);
            }
            if ("POST".equals(method) && "/bookings".equals(path)) {
                return createBooking(request);
            }
            if ("GET".equals(method) && path.startsWith("/bookings/")) {
                return userBookings(path);
            }
            if ("DELETE".equals(method) && path.startsWith("/bookings/")) {
                return cancelBooking(request, path);
            }

            return errorResponse(404, "Route not found");
        } catch (IllegalArgumentException e) {
            return errorResponse(400, e.getMessage());
        } catch (SQLException e) {
            return errorResponse(500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse(500, "Unexpected error: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent registerUser(APIGatewayProxyRequestEvent request) throws SQLException {
        RegisterRequest body = parseBody(request, RegisterRequest.class);
        if (body.name == null || body.name.isBlank() || body.email == null || body.email.isBlank()) {
            throw new IllegalArgumentException("name and email are required");
        }

        User existing = userService.getByEmail(body.email.trim());
        if (existing != null) {
            return jsonResponse(200, existing);
        }

        int userId = userService.register(body.name.trim(), body.email.trim());
        return jsonResponse(201, Map.of("id", userId, "name", body.name.trim(), "email", body.email.trim()));
    }

    private APIGatewayProxyResponseEvent loginUser(APIGatewayProxyRequestEvent request) throws SQLException {
        Map<String, String> query = safeQueryParams(request);
        String email = query.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email query parameter is required");
        }

        User user = userService.getByEmail(email.trim());
        if (user == null) {
            return errorResponse(404, "User not found");
        }
        return jsonResponse(200, user);
    }

    private APIGatewayProxyResponseEvent publishRide(APIGatewayProxyRequestEvent request) throws SQLException {
        PublishRideRequest body = parseBody(request, PublishRideRequest.class);
        if (body.ownerId <= 0 || body.source == null || body.destination == null || body.seats <= 0 || body.farePerSeat <= 0) {
            throw new IllegalArgumentException("ownerId, source, destination, seats, farePerSeat are required");
        }

        int rideId = rideService.publish(
                body.ownerId,
                body.source.trim(),
                body.destination.trim(),
                body.seats,
                body.farePerSeat
        );

        return jsonResponse(201, Map.of("id", rideId));
    }

    private APIGatewayProxyResponseEvent listRides(APIGatewayProxyRequestEvent request) throws SQLException {
        Map<String, String> query = safeQueryParams(request);
        String source = query.get("source");
        String destination = query.get("destination");

        List<Ride> rides;
        if (source != null && !source.isBlank() && destination != null && !destination.isBlank()) {
            rides = rideService.search(source.trim(), destination.trim());
        } else {
            rides = rideService.viewAll();
        }

        return jsonResponse(200, rides);
    }

    private APIGatewayProxyResponseEvent cancelRide(APIGatewayProxyRequestEvent request, String path) throws SQLException {
        String rideIdToken = path.substring("/rides/".length());
        int rideId = Integer.parseInt(rideIdToken);

        String ownerIdToken = safeQueryParams(request).get("ownerId");
        if (ownerIdToken == null) {
            throw new IllegalArgumentException("ownerId query parameter is required");
        }

        int ownerId = Integer.parseInt(ownerIdToken);
        boolean cancelled = rideService.cancel(rideId, ownerId);
        if (!cancelled) {
            return errorResponse(404, "Ride not found or not owned by user");
        }

        return jsonResponse(200, Map.of("cancelled", true));
    }

    private APIGatewayProxyResponseEvent createBooking(APIGatewayProxyRequestEvent request) throws SQLException {
        CreateBookingRequest body = parseBody(request, CreateBookingRequest.class);
        if (body.userId <= 0 || body.rideId <= 0 || body.seats <= 0) {
            throw new IllegalArgumentException("userId, rideId and seats are required");
        }

        Booking booking = bookingService.bookRide(body.rideId, body.userId, body.seats);
        return jsonResponse(201, booking);
    }

    private APIGatewayProxyResponseEvent userBookings(String path) throws SQLException {
        String token = path.substring("/bookings/".length());
        int userId = Integer.parseInt(token);
        List<Booking> bookings = bookingService.showMyBookings(userId);
        return jsonResponse(200, bookings);
    }

    private APIGatewayProxyResponseEvent cancelBooking(APIGatewayProxyRequestEvent request, String path) throws SQLException {
        String token = path.substring("/bookings/".length());
        int bookingId = Integer.parseInt(token);

        String userIdToken = safeQueryParams(request).get("userId");
        if (userIdToken == null) {
            throw new IllegalArgumentException("userId query parameter is required");
        }

        int userId = Integer.parseInt(userIdToken);
        bookingService.cancelBooking(bookingId, userId);
        return jsonResponse(200, Map.of("cancelled", true));
    }

    private String normalizePath(APIGatewayProxyRequestEvent request) {
        String path = request.getPath() == null ? "/" : request.getPath();
        String stage = request.getRequestContext() != null ? request.getRequestContext().getStage() : null;

        if (stage != null && !stage.isBlank() && path.startsWith("/" + stage + "/")) {
            return path.substring(stage.length() + 1);
        }
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }

    private <T> T parseBody(APIGatewayProxyRequestEvent request, Class<T> type) {
        String body = request.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is required");
        }

        try {
            return GSON.fromJson(body, type);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private APIGatewayProxyResponseEvent jsonResponse(int status, Object body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(defaultHeaders())
                .withBody(GSON.toJson(body));
    }

    private APIGatewayProxyResponseEvent errorResponse(int status, String message) {
        return jsonResponse(status, Map.of("error", message));
    }

    private Map<String, String> safeQueryParams(APIGatewayProxyRequestEvent request) {
        Map<String, String> query = request.getQueryStringParameters();
        return query == null ? new HashMap<>() : query;
    }

    private Map<String, String> defaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        return headers;
    }

    private static class RegisterRequest {
        String name;
        String email;
    }

    private static class PublishRideRequest {
        int ownerId;
        String source;
        String destination;
        int seats;
        int farePerSeat;
    }

    private static class CreateBookingRequest {
        int rideId;
        int userId;
        int seats;
    }
}
