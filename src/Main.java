import db.DBConnection;

public class Main {
    public static void main(String[] args) throws Exception {
        DBConnection.ensureSchema();
        System.out.println("Cab Booking backend initialized successfully.");
        System.out.println("Deploy Lambda handler: api.LambdaHandler");
    }
}
