import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private final String flightName;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final double price;
    private final String cabin;
    private int availableSeats;
    private final List<Integer> bookedSeats;

    public Flight(String flightName, String origin, String destination, LocalDateTime departureTime,
                  LocalDateTime arrivalTime, double price, String cabin, int totalSeats) {
        this.flightName = flightName;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.cabin = cabin;
        this.availableSeats = totalSeats;
        this.bookedSeats = new ArrayList<>();
    }

    public void setBookedSeatsFromString(String seats) {
        if (seats != null && !seats.isEmpty()) {
            bookedSeats.clear();
            for (String seat : seats.split(",")) {
                bookedSeats.add(Integer.parseInt(seat.trim()));
            }
        }
    }

    public String getBookedSeatsAsString() {
        return String.join(",", bookedSeats.stream().map(String::valueOf).toList());
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<Integer> getBookedSeats() {
        return bookedSeats;
    }

    public void bookSeats(List<Integer> seats) {
        if (seats.size() > availableSeats) {
            throw new IllegalArgumentException("Not enough available seats.");
        }

        for (int seat : seats) {
            if (bookedSeats.contains(seat)) {
                throw new IllegalArgumentException("Seat " + seat + " is already booked.");
            }
        }

        bookedSeats.addAll(seats);
        availableSeats -= seats.size();
    }

    public String getFlightName() {
        return flightName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public String getCabin() {
        return cabin;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

}