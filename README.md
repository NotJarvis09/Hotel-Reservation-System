# Hotel Management System GUI

A feature-rich Java desktop hotel management system equipped with an interactive Graphical User Interface (GUI). Designed to simulate comprehensive hotel operations, this application allows users to handle reservations, manage rooms (Standard, Deluxe, Suite), process payments, and maintain guest records.

## Features
- **Interactive GUI Dashboard:** Built using Java to provide a user-friendly management interface.
- **Reservation & Guest Management:** Manage guest profiles, track check-ins/check-outs, and handle bookings dynamically through `Reservation`, `Guest`, and `HotelSystemManager`.
- **Room Categories:** Categorize and manage different room types including `StandardRoom`, `DeluxeRoom`, and `SuiteRoom` extending base `Room` logic.
- **Payment Processing:** Securely handle transactions using dedicated classes like `Payment`, `PaymentMethod`, and `PaymentStatus`.
- **Data Persistence:** Integrated data handlers and serialized storage (`DataHandler`, `hotel_system_data.ser`) to save system state and records.

## Project Structure
- `Task 4/Main.java`: Main application entry point and GUI controller.
- `Task 4/HotelSystemManager.java`: Core logic manager for hotel operations.
- `Task 4/Reservation.java`: Manages booking schedules and reservation states.
- `Task 4/Guest.java`: Handles guest details and information.
- `Task 4/Room.java` & Subclasses (`StandardRoom.java`, `DeluxeRoom.java`, `SuiteRoom.java`): Defines room types and attributes.
- `Task 4/Payment.java` & Support Classes (`PaymentMethod.java`, `PaymentStatus.java`): Manages financial transactions.
- `Task 4/DataHandler.java`: Handles system serialization and file operations.
- `Task 4/hotel_system_data.ser`: Serialized data file for storing persistent records.

## Prerequisites
- Java Development Kit (JDK) installed on your system.

## How to Run
1. Clone the repository:
```bash
git clone [https://github.com/NotJarvis09/Hotel-Management-System-GUI.git](https://github.com/NotJarvis09/Hotel-Management-System-GUI.git)
```
2. Navigate to the project directory:
```bash
cd "Task 4"
```
4. Compile the Java source file:
```bash
javac *.java
```
5. Run the application:
```bash
java Task 4.Main
```
