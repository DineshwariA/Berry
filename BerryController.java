package com.berry.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
public class BerryController {
    public static class ItemRequest {
        public String itemName;
        public String personName;
        public String borrowDate;
        public String dueDate;
        public String direction = "BORROWED";
    }
    @PostMapping("/api/add-item")
    public String addItem(@RequestBody ItemRequest item) {

        if (item == null
                || item.itemName == null || item.itemName.trim().isEmpty()
                || item.personName == null || item.personName.trim().isEmpty()
                || item.borrowDate == null || item.borrowDate.trim().isEmpty()
                || item.dueDate == null || item.dueDate.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please fill in every field.");
        }

        String direction = item.direction == null
                ? "BORROWED" : item.direction.trim().toUpperCase();

        if (!direction.equals("LENT") && !direction.equals("BORROWED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Choose LENT or BORROWED.");
        }

        LocalDate borrowDate;
        LocalDate dueDate;

        try {
            borrowDate = LocalDate.parse(item.borrowDate.trim());
            dueDate = LocalDate.parse(item.dueDate.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use dates in YYYY-MM-DD format.");
        }

        if (dueDate.isBefore(borrowDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The due date cannot be before the borrow date.");
        }

        String sql = "INSERT INTO records "
                + "(item_name, person_name, borrow_date, due_date, status, direction) "
                + "VALUES (?, ?, ?, ?, 'Borrowed', ?)";

        try (Connection connection = BerryDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, item.itemName.trim());
            statement.setString(2, item.personName.trim());
            statement.setDate(3, Date.valueOf(borrowDate));
            statement.setDate(4, Date.valueOf(dueDate));
            statement.setString(5, direction);

            if (statement.executeUpdate() > 0) {
                return "Item added successfully! 🍓";
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Item could not be added.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error while adding item.");
        }
    }

    @GetMapping("/api/records")
    public List<Map<String, Object>> getRecords() {
        List<Map<String, Object>> records = new ArrayList<>();

        String sql = "SELECT id, item_name, person_name, "
                + "borrow_date, due_date, status, direction "
                + "FROM records ORDER BY id DESC";

        try (Connection connection = BerryDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Map<String, Object> record = new LinkedHashMap<>();

                record.put("id", result.getInt("id"));
                record.put("itemName", result.getString("item_name"));
                record.put("personName", result.getString("person_name"));
                Date borrowDate = result.getDate("borrow_date");
                Date dueDate = result.getDate("due_date");
                record.put("borrowDate", borrowDate == null ? null : borrowDate.toString());
                record.put("dueDate", dueDate == null ? null : dueDate.toString());
                record.put("status", result.getString("status"));
                record.put("direction", result.getString("direction"));

                records.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load records.");
        }

        return records;
    }
    @GetMapping("/api/dashboard")
    public Map<String, Integer> getDashboard() {
        Map<String, Integer> totals = new LinkedHashMap<>();

        String sql = "SELECT "
                + "COUNT(*) AS total_items, "
                + "COALESCE(SUM(CASE WHEN status = 'Borrowed' "
                + "AND direction = 'BORROWED' THEN 1 ELSE 0 END), 0) "
                + "AS borrowed_items, "
                + "COALESCE(SUM(CASE WHEN status = 'Borrowed' "
                + "AND direction = 'LENT' THEN 1 ELSE 0 END), 0) "
                + "AS lent_items, "
                + "COALESCE(SUM(CASE WHEN status = 'Borrowed' "
                + "AND due_date < CURRENT_DATE THEN 1 ELSE 0 END), 0) "
                + "AS overdue_items "
                + "FROM records";

        try (Connection connection = BerryDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                totals.put("totalItems", result.getInt("total_items"));
                totals.put("borrowedItems", result.getInt("borrowed_items"));
                totals.put("lentItems", result.getInt("lent_items"));
                totals.put("overdueItems", result.getInt("overdue_items"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load dashboard.");
        }

        return totals;
    }
    @PostMapping("/api/return-item")
    public String returnItem(@RequestParam int id) {
        String sql = "UPDATE records SET status = 'Returned' "
                + "WHERE id = ? AND status = 'Borrowed'";

        try (Connection connection = BerryDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            if (statement.executeUpdate() > 0) {
                return "Item marked as returned! 🍓";
            }

            return "Item not found or already returned.";

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error returning item.");
        }
    }
    @PostMapping("/api/delete-item")
    public String deleteItem(@RequestParam int id) {
        String sql = "DELETE FROM records WHERE id = ?";

        try (Connection connection = BerryDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            if (statement.executeUpdate() > 0) {
                return "Item deleted successfully.";
            }

            return "Item not found.";

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error deleting item.");
        }
    }
}
