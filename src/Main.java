import service.UserService;
import service.RideService;
import service.BookingService;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        UserService userService = new UserService();
        RideService rideService = new RideService();
        BookingService bookingService = new BookingService();

        System.out.println("====== CAB BOOKING APPLICATION ======");

        int userId;

        // ---------- LOGIN / REGISTER ----------
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Register (New User)");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            if (choice == 1) {
                System.out.print("Enter your User ID: ");
                userId = sc.nextInt();

                if (!userService.login(userId)) {
                    System.out.println("❌ Invalid User ID. Try again.");
                    continue;
                }

                System.out.println("✅ Login successful!");
                break;

            } else if (choice == 2) {
                System.out.print("Enter name: ");
                String name = sc.nextLine();

                System.out.print("Enter email: ");
                String email = sc.nextLine();

                userId = userService.register(name, email);
                System.out.println("✅ Registration successful!");
                System.out.println("👉 Your User ID is: " + userId);
                break;

            } else if (choice == 0) {
                System.out.println("👋 Goodbye!");
                return;

            } else {
                System.out.println("❌ Invalid option");
            }
        }

        // ---------- MAIN MENU ----------
        while (true) {
            System.out.println("\n----- MENU -----");
            System.out.println("1. View all available rides");
            System.out.println("2. Publish a ride");
            System.out.println("3. Book a ride");
            System.out.println("4. Cancel my ride (Owner)");
            System.out.println("5. View my rides");
            System.out.println("6. Cancel my booking");
            System.out.println("0. Logout");

            System.out.print("Choose option: ");
            int option = sc.nextInt();

            switch (option) {

                // View all rides
                case 1:
                    rideService.viewAll();
                    break;

                // Publish ride
                case 2:
                    System.out.print("Enter Source: ");
                    String src = sc.next();

                    System.out.print("Enter Destination: ");
                    String dest = sc.next();

                    System.out.print("Enter Total Seats: ");
                    int seats = sc.nextInt();

                    System.out.print("Enter Fare per Seat: ");
                    int fare = sc.nextInt();

                    rideService.publish(userId, src, dest, seats, fare);
                    System.out.println("✅ Ride published successfully");
                    break;

                // Book ride
                case 3:
                    System.out.print("Enter Source: ");
                    String s = sc.next();

                    System.out.print("Enter Destination: ");
                    String d = sc.next();

                    rideService.search(s, d);

                    System.out.print("\nEnter Ride ID to book (0 to cancel): ");
                    int rideId = sc.nextInt();

                    if (rideId == 0) {
                        System.out.println("Booking cancelled");
                        break;
                    }

                    System.out.print("Enter number of seats to book: ");
                    int seatsToBook = sc.nextInt();

                    bookingService.bookRide(rideId, userId, seatsToBook);
                    break;

                // Cancel ride (owner)
                case 4:
                    System.out.print("Enter Ride ID to cancel: ");
                    int cancelRideId = sc.nextInt();

                    rideService.cancel(cancelRideId);
                    System.out.println("❌ Ride cancelled");
                    break;

                // View my rides
                case 5:
                    rideService.viewMyRides(userId);
                    break;

                case 6:
                    // Show user's bookings first
                    bookingService.showMyBookings(userId);

                    System.out.print("\nEnter Booking ID to cancel (0 to go back): ");
                    int bookingId = sc.nextInt();

                    if (bookingId == 0) {
                        System.out.println("Cancelled operation");
                        break;
                    }

                    bookingService.cancelBooking(bookingId, userId);
                    break;


                // Logout
                case 0:
                    System.out.println("👋 Logged out");
                    return;

                default:
                    System.out.println("❌ Invalid option");
            }
        }
    }
}
