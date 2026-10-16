import java.io.*;
import java.time.LocalDate;
import java.util.*;

// ==================== 1. ENUMS & DATA MODELS ====================
enum RoomCategory { STANDARD, DELUXE, SUITE }

enum PaymentMethod { UPI, CREDIT_CARD, NET_BANKING, PAY_AT_HOTEL }

enum PaymentStatus { PENDING, COMPLETED, REFUNDED }

class Guest implements Serializable {
    private String guestId;
    private String name;
    private String email;
    private String phone;

    public Guest(String name, String email, String phone) {
        this.guestId = "GST-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getGuestId() { return guestId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format("Guest: %s | Email: %s | Phone: %s", name, email, phone);
    }
}

// ==================== 2. ROOM HIERARCHY (OOP) ====================
abstract class Room implements Serializable {
    private String roomId;
    private RoomCategory category;
    private double basePrice;
    private boolean isAvailable;
    private List<String> amenities;

    public Room(String roomId, RoomCategory category, double basePrice, List<String> amenities) {
        this.roomId = roomId;
        this.category = category;
        this.basePrice = basePrice;
        this.isAvailable = true;
        this.amenities = amenities;
    }

    public String getRoomId() { return roomId; }
    public RoomCategory getCategory() { return category; }
    public double getBasePrice() { return basePrice; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public List<String> getAmenities() { return amenities; }

    public abstract double calculateTotalRate(int nights);

    @Override
    public String toString() {
        return String.format("[%s] Room ID: %s | Base Rate: ₹%.2f/night | Available: %s | Amenities: %s",
                category, roomId, basePrice, isAvailable ? "Yes" : "No", String.join(", ", amenities));
    }
}

class StandardRoom extends Room {
    public StandardRoom(String roomId) {
        super(roomId, RoomCategory.STANDARD, 2500.0, Arrays.asList("Free Wi-Fi", "TV", "AC"));
    }
    @Override
    public double calculateTotalRate(int nights) {
        return getBasePrice() * nights;
    }
}

class DeluxeRoom extends Room {
    public DeluxeRoom(String roomId) {
        super(roomId, RoomCategory.DELUXE, 5000.0, Arrays.asList("Free Wi-Fi", "Smart TV", "AC", "Mini Bar", "City View"));
    }
    @Override
    public double calculateTotalRate(int nights) {
        return (getBasePrice() * nights) * 1.05; // 5% luxury tax
    }
}

class SuiteRoom extends Room {
    public SuiteRoom(String roomId) {
        super(roomId, RoomCategory.SUITE, 10500.0, Arrays.asList("King Bed", "Jacuzzi", "Living Room", "Butler Service", "Ocean View"));
    }
    @Override
    public double calculateTotalRate(int nights) {
        return (getBasePrice() * nights) * 1.12; // 12% luxury tax + service fee
    }
}

// ==================== 3. PAYMENT CLASS ====================
class Payment implements Serializable {
    private String transactionId;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;

    public Payment(double amount, PaymentMethod method) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }

    public boolean processPayment() {
        System.out.printf("Processing payment of ₹%.2f via %s...\n", amount, method);
        // Simulate gateway latency/approval
        this.status = PaymentStatus.COMPLETED;
        System.out.println("Transaction Successful! Ref ID: " + transactionId);
        return true;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
        System.out.println("Refund processed for transaction: " + transactionId);
    }

    public PaymentStatus getStatus() { return status; }
    @Override
    public String toString() {
        return String.format("TXN: %s | Amount: ₹%.2f | Method: %s | Status: %s", transactionId, amount, method, status);
    }
}

// ==================== 4. RESERVATION CLASS ====================
class Reservation implements Serializable {
    private String bookingId;
    private Guest guest;
    private String roomId;
    private int nights;
    private double totalAmount;
    private Payment payment;
    private LocalDate bookingDate;

    public Reservation(Guest guest, String roomId, int nights, double totalAmount, Payment payment) {
        this.bookingId = "BK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.guest = guest;
        this.roomId = roomId;
        this.nights = nights;
        this.totalAmount = totalAmount;
        this.payment = payment;
        this.bookingDate = LocalDate.now();
    }

    public String getBookingId() { return bookingId; }
    public String getRoomId() { return roomId; }
    public Payment getPayment() { return payment; }

    @Override
    public String toString() {
        return String.format(
            "--------------------------------------------------\n" +
            "Booking ID   : %s\n" +
            "Booked On    : %s\n" +
            "%s\n" +
            "Room ID      : %s | Nights: %d\n" +
            "Total Bill   : ₹%.2f\n" +
            "Payment Info : %s\n" +
            "--------------------------------------------------",
            bookingId, bookingDate, guest, roomId, nights, totalAmount, payment
        );
    }
}

// ==================== 5. PERSISTENCE LAYER (FILE I/O) ====================
class DataHandler {
    private static final String DATA_FILE = "hotel_system_data.ser";

    public static void saveData(Map<String, Room> rooms, Map<String, Reservation> reservations) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(rooms);
            oos.writeObject(reservations);
        } catch (IOException e) {
            System.out.println("Error saving system state: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Object[] loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Room> rooms = (Map<String, Room>) ois.readObject();
            Map<String, Reservation> reservations = (Map<String, Reservation>) ois.readObject();
            return new Object[]{rooms, reservations};
        } catch (Exception e) {
            System.out.println("Error loading system state: " + e.getMessage());
            return null;
        }
    }
}

// ==================== 6. CORE HOTEL SYSTEM CONTROLLER ====================
class HotelSystemManager {
    private Map<String, Room> rooms = new HashMap<>();
    private Map<String, Reservation> reservations = new HashMap<>();

    public HotelSystemManager() {
        Object[] loadedData = DataHandler.loadData();
        if (loadedData != null) {
            rooms = (Map<String, Room>) loadedData[0];
            reservations = (Map<String, Reservation>) loadedData[1];
        } else {
            seedDefaultRooms();
        }
    }

    private void seedDefaultRooms() {
        rooms.put("S101", new StandardRoom("S101"));
        rooms.put("S102", new StandardRoom("S102"));
        rooms.put("D201", new DeluxeRoom("D201"));
        rooms.put("D202", new DeluxeRoom("D202"));
        rooms.put("SU301", new SuiteRoom("SU301"));
        DataHandler.saveData(rooms, reservations);
    }

    public void searchRooms(RoomCategory category, Double maxPrice) {
        System.out.println("\n--- Search Results ---");
        boolean found = false;
        for (Room room : rooms.values()) {
            if (room.isAvailable()) {
                boolean matchCategory = (category == null || room.getCategory() == category);
                boolean matchPrice = (maxPrice == null || room.getBasePrice() <= maxPrice);
                if (matchCategory && matchPrice) {
                    System.out.println(room);
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("No available rooms match your criteria.");
        }
    }

    public void createBooking(Guest guest, String roomId, int nights, PaymentMethod method) {
        Room room = rooms.get(roomId);
        if (room == null || !room.isAvailable()) {
            System.out.println("Error: Room is either invalid or currently unavailable.");
            return;
        }

        double totalAmount = room.calculateTotalRate(nights);
        Payment payment = new Payment(totalAmount, method);
        
        if (payment.processPayment()) {
            room.setAvailable(false);
            Reservation reservation = new Reservation(guest, roomId, nights, totalAmount, payment);
            reservations.put(reservation.getBookingId(), reservation);
            DataHandler.saveData(rooms, reservations);
            
            System.out.println("\nReservation Completed Successfully!");
            System.out.println(reservation);
        } else {
            System.out.println("Payment failed. Reservation cancelled.");
        }
    }

    public void cancelReservation(String bookingId) {
        Reservation res = reservations.get(bookingId);
        if (res == null) {
            System.out.println("Error: Booking ID not found.");
            return;
        }

        Room room = rooms.get(res.getRoomId());
        if (room != null) {
            room.setAvailable(true);
        }

        res.getPayment().refund();
        reservations.remove(bookingId);
        DataHandler.saveData(rooms, reservations);
        System.out.println("Reservation " + bookingId + " has been successfully cancelled.");
    }

    public void viewBookingDetails(String bookingId) {
        Reservation res = reservations.get(bookingId);
        if (res != null) {
            System.out.println(res);
        } else {
            System.out.println("No booking found with ID: " + bookingId);
        }
    }

    public void addRoomToInventory(Room room) {
        rooms.put(room.getRoomId(), room);
        DataHandler.saveData(rooms, reservations);
        System.out.println("Room " + room.getRoomId() + " added successfully.");
    }

    public void viewAllBookings() {
        if (reservations.isEmpty()) {
            System.out.println("No active bookings in the system.");
            return;
        }
        System.out.println("\n--- All System Reservations ---");
        for (Reservation res : reservations.values()) {
            System.out.println(res);
        }
    }
}

// ==================== 7. MAIN INTERFACE / CLI ====================
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HotelSystemManager manager = new HotelSystemManager();

        while (true) {
            System.out.println("\n=== Grand Palace Hotel Reservation System ===");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. View Booking Details");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. Admin Portal (Manage Inventory & Bookings)");
            System.out.println("6. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.println("\nFilter options:");
                    System.out.println("1. All Rooms");
                    System.out.println("2. Filter by Category (Standard/Deluxe/Suite)");
                    System.out.print("Choose filter type: ");
                    int fType = scanner.nextInt();
                    scanner.nextLine();

                    RoomCategory cat = null;
                    if (fType == 2) {
                        System.out.print("Enter Category (STANDARD, DELUXE, SUITE): ");
                        try {
                            cat = RoomCategory.valueOf(scanner.nextLine().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid category. Showing all rooms.");
                        }
                    }
                    manager.searchRooms(cat, null);
                    break;

                case 2:
                    System.out.print("Enter Guest Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Email ID: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter Phone Number: ");
                    String phone = scanner.nextLine();
                    Guest guest = new Guest(name, email, phone);

                    manager.searchRooms(null, null);
                    System.out.print("Enter Room ID you wish to book: ");
                    String roomId = scanner.nextLine();
                    System.out.print("Enter Number of Nights: ");
                    int nights = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Select Payment Method: 1. UPI  2. Credit Card  3. Net Banking  4. Pay at Hotel");
                    int pChoice = scanner.nextInt();
                    scanner.nextLine();
                    PaymentMethod method = switch (pChoice) {
                        case 1 -> PaymentMethod.UPI;
                        case 2 -> PaymentMethod.CREDIT_CARD;
                        case 3 -> PaymentMethod.NET_BANKING;
                        default -> PaymentMethod.PAY_AT_HOTEL;
                    };

                    manager.createBooking(guest, roomId, nights, method);
                    break;

                case 3:
                    System.out.print("Enter Booking ID: ");
                    String bId = scanner.nextLine();
                    manager.viewBookingDetails(bId);
                    break;

                case 4:
                    System.out.print("Enter Booking ID to cancel: ");
                    String cId = scanner.nextLine();
                    manager.cancelReservation(cId);
                    break;

                case 5:
                    System.out.println("\n--- Admin Portal ---");
                    System.out.println("1. View All Bookings");
                    System.out.println("2. Add New Room to Inventory");
                    System.out.print("Choose option: ");
                    int adminChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (adminChoice == 1) {
                        manager.viewAllBookings();
                    } else if (adminChoice == 2) {
                        System.out.print("Enter New Room ID (e.g., D203): ");
                        String newId = scanner.nextLine();
                        System.out.print("Enter Category (STANDARD, DELUXE, SUITE): ");
                        try {
                            RoomCategory newCat = RoomCategory.valueOf(scanner.nextLine().toUpperCase());
                            Room newRoom = switch (newCat) {
                                case STANDARD -> new StandardRoom(newId);
                                case DELUXE -> new DeluxeRoom(newId);
                                case SUITE -> new SuiteRoom(newId);
                            };
                            manager.addRoomToInventory(newRoom);
                        } catch (Exception e) {
                            System.out.println("Invalid category selection.");
                        }
                    }
                    break;

                case 6:
                    System.out.println("Thank you for using the Hotel Reservation System. Goodbye!");
                    scanner.close();
                    System.exit(0);

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}