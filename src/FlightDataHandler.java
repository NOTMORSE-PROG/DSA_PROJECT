import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightDataHandler {
    private static final String FILE_NAME = "flights.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static List<Flight> readFlightsFromFile() {
        List<Flight> flights = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Flight flight = getFlight(line);
                if (flight != null) {
                    flights.add(flight);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return flights;
    }
    private static Flight getFlight(String line) {
        try {
            String[] parts = line.split(",");

            if (parts.length < 8) {
                System.out.println("Incomplete flight data: " + line);
                return null;
            }

            String availableSeatsStr = parts[7].split(";")[0].trim();

            String flightName = parts[0];
            String origin = parts[1];
            String destination = parts[2];
            LocalDateTime departure = LocalDateTime.parse(parts[3], formatter);
            LocalDateTime arrival = LocalDateTime.parse(parts[4], formatter);
            double price = Double.parseDouble(parts[5]);
            String cabin = parts[6];
            int availableSeats = Integer.parseInt(availableSeatsStr);

            return new Flight(flightName, origin, destination, departure, arrival, price, cabin, availableSeats);
        } catch (Exception e) {
            System.out.println("Error parsing flight data: " + line + " - " + e.getMessage());
            return null;
        }
    }

    public static void saveFlightsToFile(List<Flight> flights) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (Flight flight : flights) {
                writer.write(formatFlightForSaving(flight));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void appendFlightToFile(Flight flight) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(formatFlightForSaving(flight));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error appending to file: " + e.getMessage());
        }
    }

    private static String formatFlightForSaving(Flight flight) {
        return String.join(",",
                flight.getFlightName(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime().format(formatter),
                flight.getArrivalTime().format(formatter),
                String.valueOf(flight.getPrice()),
                flight.getCabin(),
                String.valueOf(flight.getAvailableSeats()));
    }
}
