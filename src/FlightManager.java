import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlightManager {
    private List<Flight> flights;

    public FlightManager() {
        this.flights = new ArrayList<>();
    }

    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public void checkAndRemoveFullyBookedFlights() {
        Iterator<Flight> iterator = flights.iterator();
        while (iterator.hasNext()) {
            Flight flight = iterator.next();
            if (flight.isFullyBooked()) {
                System.out.println("Flight " + flight.getFlightName() + " is fully booked and removed.");
                iterator.remove();
                generateNewFlight();
            }
        }
    }

    public void generateNewFlight() {
        Flight newFlight = new Flight(
                "NewFlight" + (flights.size() + 1),
                "Origin", "Destination",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1000.0, "Economy", 60
        );
        flights.add(newFlight);
        System.out.println("New flight generated: " + newFlight.getFlightName());
    }

    public List<Flight> getFlights() {
        return flights;
    }
}
