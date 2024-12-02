import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightDataHandler {
    public static void saveFlightToDatabase(Flight flight) {
        String insertSQL = """
        INSERT INTO flights (flight_name, origin, destination, departure_time, arrival_time, price, cabin, total_seats, available_seats, booked_seats)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, flight.getFlightName());
            pstmt.setString(2, flight.getOrigin());
            pstmt.setString(3, flight.getDestination());
            pstmt.setTimestamp(4, Timestamp.valueOf(flight.getDepartureTime()));
            pstmt.setTimestamp(5, Timestamp.valueOf(flight.getArrivalTime()));
            pstmt.setDouble(6, flight.getPrice());
            pstmt.setString(7, flight.getCabin());
            pstmt.setInt(8, 60);
            pstmt.setInt(9, flight.getAvailableSeats());
            pstmt.setString(10, flight.getBookedSeatsAsString());
            pstmt.executeUpdate();
            System.out.println("Flight saved successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error saving flight: " + e.getMessage());
        }
    }

    public static List<Flight> fetchAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String querySQL = "SELECT * FROM flights";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                int totalSeats = 60;
                Flight flight = new Flight(
                        rs.getString("flight_name"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getTimestamp("departure_time").toLocalDateTime(),
                        rs.getTimestamp("arrival_time").toLocalDateTime(),
                        rs.getDouble("price"),
                        rs.getString("cabin"),
                        totalSeats
                );

                flight.setBookedSeatsFromString(rs.getString("booked_seats"));
                flight.setAvailableSeats(totalSeats - flight.getBookedSeats().size());

                flights.add(flight);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching flights: " + e.getMessage());
        }

        return flights;
    }


    public static void updateSeatBooking(Flight flight) {
        String updateSQL = "UPDATE flights SET available_seats = ?, booked_seats = ? WHERE flight_name = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            int totalSeats = 60;
            int availableSeats = totalSeats - flight.getBookedSeats().size();
            pstmt.setInt(1, availableSeats);
            pstmt.setString(2, flight.getBookedSeatsAsString());
            pstmt.setString(3, flight.getFlightName());
            pstmt.executeUpdate();
            System.out.println("Flight seats updated successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating seats: " + e.getMessage());
        }
    }
}