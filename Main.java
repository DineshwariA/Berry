import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\uD83C\uDF53 Welcome to Berry!");
        boolean running = true;
        while (running) {
            System.out.println("\n===== BERRY 🍓 =====");
            System.out.println("1. Add an item");
            System.out.println("2. View all records");
            System.out.println("3. Search by item name");
            System.out.println("4. Mark an item as returned");
            System.out.println("5. Delete a record");
            System.out.println("6. View overdue items");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    try (Connection con = Database.getConnection()) {
                        System.out.print("Enter item name: ");
                        String item = sc.nextLine();
                        System.out.print("Enter person's name: ");
                        String person = sc.nextLine();
                        System.out.print("Enter borrow date (YYYY-MM-DD): ");
                        String borrowDate = sc.nextLine();
                        System.out.print("Enter due date (YYYY-MM-DD): ");
                        String dueDate = sc.nextLine();
                        String sql = "INSERT INTO records (item_name, person_name, borrow_date, due_date) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setString(1, item);
                            ps.setString(2, person);
                            ps.setString(3, borrowDate);
                            ps.setString(4, dueDate);
                            ps.executeUpdate();
                            System.out.println("Item added successfully! 🍓");
                        }
                    } catch (SQLException e) {
                        System.out.println("Couldn't add the item: " + e.getMessage());
                    }
                    break;
                case "2":
                    try (Connection con = Database.getConnection()) {
                        String sql = "SELECT * FROM records";
                        try (PreparedStatement ps = con.prepareStatement(sql);
                             ResultSet rs = ps.executeQuery()) {
                            boolean found = false;
                            System.out.println("\n===== ALL BERRY RECORDS 🍓 =====");
                            while (rs.next()) {
                                found = true;
                                System.out.println("------------------------------");
                                System.out.println("ID: " + rs.getInt("id"));
                                System.out.println("Item: " + rs.getString("item_name"));
                                System.out.println("Person: " + rs.getString("person_name"));
                                System.out.println("Borrow Date: " + rs.getDate("borrow_date"));
                                System.out.println("Due Date: " + rs.getDate("due_date"));
                                System.out.println("Status: " + rs.getString("status"));
                            }
                            if (!found) {
                                System.out.println("No records found yet!");
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("Couldn't view records: " + e.getMessage());
                    }
                    break;
                case "3":
                    try (Connection con = Database.getConnection()) {
                        System.out.print("Enter item name to search: ");
                        String searchName = sc.nextLine();
                        String sql = "SELECT * FROM records WHERE item_name LIKE ?";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setString(1, "%" + searchName + "%");
                            try (ResultSet rs = ps.executeQuery()) {
                                boolean found = false;
                                while (rs.next()) {
                                    found = true;
                                    System.out.println("------------------------------");
                                    System.out.println("ID: " + rs.getInt("id"));
                                    System.out.println("Item: " + rs.getString("item_name"));
                                    System.out.println("Person: " + rs.getString("person_name"));
                                    System.out.println("Borrow Date: " + rs.getDate("borrow_date"));
                                    System.out.println("Due Date: " + rs.getDate("due_date"));
                                    System.out.println("Status: " + rs.getString("status"));
                                }
                                if (!found) {
                                    System.out.println("No matching items found!");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("Search failed: " + e.getMessage());
                    }
                    break;
                case "4":
                    try (Connection con = Database.getConnection()) {
                        System.out.print("Enter the record ID to mark as returned: ");
                        int id = Integer.parseInt(sc.nextLine());
                        String sql = "UPDATE records SET status = 'Returned' WHERE id = ? AND status = 'Borrowed'";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setInt(1, id);
                            int updated = ps.executeUpdate();
                            if (updated > 0) {
                                System.out.println("Item marked as returned! 🍓");
                            } else {
                                System.out.println("Record not found or item is already returned.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid numeric record ID.");
                    } catch (SQLException e) {
                        System.out.println("Couldn't update the record: " + e.getMessage());
                    }
                    break;
                case "5":
                    try (Connection con = Database.getConnection()) {
                        System.out.print("Enter the record ID to delete: ");
                        int id = Integer.parseInt(sc.nextLine());
                        System.out.print("Are you sure you want to delete this record? (yes/no): ");
                        String confirm = sc.nextLine();
                        if (confirm.equalsIgnoreCase("yes")) {
                            String sql = "DELETE FROM records WHERE id = ?";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, id);
                                int deleted = ps.executeUpdate();
                                if (deleted > 0) {
                                    System.out.println("Record deleted successfully! 🍓");
                                } else {
                                    System.out.println("No record found with that ID.");
                                }
                            }
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid numeric record ID.");
                    } catch (SQLException e) {
                        System.out.println("Couldn't delete the record: " + e.getMessage());
                    }
                    break;
                case "6":
                    try (Connection con = Database.getConnection()) {
                        String sql = "SELECT * FROM records WHERE due_date < CURDATE() AND status = 'Borrowed'";
                        try (PreparedStatement ps = con.prepareStatement(sql);
                             ResultSet rs = ps.executeQuery()) {
                            boolean found = false;
                            System.out.println("\n===== OVERDUE ITEMS 🍓 =====");
                            while (rs.next()) {
                                found = true;
                                System.out.println("------------------------------");
                                System.out.println("ID: " + rs.getInt("id"));
                                System.out.println("Item: " + rs.getString("item_name"));
                                System.out.println("Person: " + rs.getString("person_name"));
                                System.out.println("Borrow Date: " + rs.getDate("borrow_date"));
                                System.out.println("Due Date: " + rs.getDate("due_date"));
                                System.out.println("Status: " + rs.getString("status"));
                            }
                            if (!found) {
                                System.out.println("No overdue items! 🎉");
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("Couldn't view overdue items: " + e.getMessage());
                    }
                    break;
                case "7":
                    running = false;
                    System.out.println("Goodbye! 🍓");
                    break;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
        sc.close();
    }
}
