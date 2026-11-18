-- 1. Create Database
CREATE DATABASE IF NOT EXISTS berry;

-- 2. Select Database
USE berry;

-- 3. Create Records Table
CREATE TABLE IF NOT EXISTS records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100),
    person_name VARCHAR(100),
    borrow_date DATE,
    due_date DATE,
    status VARCHAR(20)
);

-- 4. Display Table Structure
DESCRIBE records;

-- 5. View All Records
SELECT * FROM records;

-- 6. Insert Sample Records (Optional)
INSERT INTO records (item_name, person_name, borrow_date, due_date, status)
VALUES
('Laptop', 'Rahul', '2026-11-01', '2026-11-10', 'Not Returned'),
('Book', 'Priya', '2026-11-03', '2026-11-12', 'Returned'),
('Calculator', 'Arun', '2026-11-05', '2026-11-15', 'Not Returned');

-- 7. View All Records
SELECT * FROM records;

-- 8. Search Item by Name
SELECT * FROM records
WHERE item_name = 'Laptop';

-- 9. Mark Item as Returned
UPDATE records
SET status = 'Returned'
WHERE id = 1;

-- 10. Delete a Record
DELETE FROM records
WHERE id = 1;

-- 11. Display Overdue Items
SELECT * FROM records
WHERE due_date < CURDATE()
AND status = 'Not Returned';
