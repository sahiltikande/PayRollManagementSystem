import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

abstract class Employee {
    private String name;
    private int id;

    public Employee(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public abstract double calculateSalary();
}

class FullTimeEmployee extends Employee {
    private double monthlySalary;

    public FullTimeEmployee(String name, int id, double monthlySalary) {
        super(name, id);
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double calculateSalary() {
        return monthlySalary;
    }
}
class PartTimeEmployee extends Employee {
    private int hoursWorked;
    private double hourlyRate;

    public PartTimeEmployee(String name, int id, int hoursWorked, double hourlyRate) {
        super(name, id);
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
    }

    // Getter for hoursWorked
    public int getHoursWorked() {
        return hoursWorked;
    }

    // Getter for hourlyRate
    public double getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public double calculateSalary() {
        return hoursWorked * hourlyRate;
    }
}
class PayrollSystem {

    private Connection connection;


    public PayrollSystem() {
        connection = DatabaseConnection.getConnection();
    }

    // Check if employee ID already exists
    public boolean employeeIdExists(int id) {
        String sql = "SELECT COUNT(*) FROM employees WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Add employee to the database

    public void addEmployee(Employee employee) {

        if (employeeIdExists(employee.getId())) {
            System.out.println("Error: Employee ID " + employee.getId() + " already exists. Please enter a unique ID.");
            return;
        }

        String sql = "INSERT INTO employees (id,name, type, monthly_salary, hours_worked, hourly_rate) VALUES (?,?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, employee.getId());
            stmt.setString(2, employee.getName());
            if (employee instanceof FullTimeEmployee) {
                stmt.setString(3, "fulltime");
                stmt.setDouble(4, ((FullTimeEmployee) employee).calculateSalary());
                stmt.setNull(5, java.sql.Types.INTEGER); // part-time fields are null
                stmt.setNull(6, java.sql.Types.DOUBLE);
            } else if (employee instanceof PartTimeEmployee) {
                stmt.setString(3, "parttime");
                stmt.setNull(4, java.sql.Types.DOUBLE); // full-time fields are null
                stmt.setInt(5, ((PartTimeEmployee) employee).getHoursWorked()); // Use getter method
                stmt.setDouble(6, ((PartTimeEmployee) employee).getHourlyRate()); // Use getter method
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove employee from the database
    public void removeEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Display all employees from the database
    
    public void displayEmployees() {
        String sql = "SELECT * FROM employees";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
        
        // Check if there are employees in the database
            if (!rs.next()) {
                System.out.println("NO EMPLOYEES PRESENT!!!");
                return; // Exit method if no employees
            }

        // Print table header
            System.out.println();
            System.out.printf("%-10s %-20s %-15s %-15s %-12s %-12s %-15s%n", 
                          "ID", "Name", "Type", "Monthly Salary", "Hours Worked", "Hourly Rate", "Final Salary");
            System.out.println("------------------------------------------------------------------------------------------------------------");

        // Loop through result set and print each employee in tabular format
            do {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
            
                if (type.equalsIgnoreCase("fulltime")) {
                    double salary = rs.getDouble("monthly_salary");
                    System.out.printf("%-10d %-20s %-15s %-15.2f %-12s %-12s %-15.2f%n", 
                                  id, name, "Full-Time", salary, "N/A", "N/A", salary);
                } else if (type.equalsIgnoreCase("parttime")) {
                    int hoursWorked = rs.getInt("hours_worked");
                    double hourlyRate = rs.getDouble("hourly_rate");
                    double finalSalary = hoursWorked * hourlyRate;
                    System.out.printf("%-10d %-20s %-15s %-15s %-12d %-12.2f %-15.2f%n", 
                                  id, name, "Part-Time", "N/A", hoursWorked, hourlyRate, finalSalary);
                }
            } while (rs.next());
        
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PayrollSystem payrollSystem = new PayrollSystem();

        while (true) {
            System.out.println();
            System.out.println("======================================================= MENU ============================================================");
            System.out.println();
            System.out.println("1. Add Employee");
            System.out.println("2. Remove Employee");
            System.out.println("3. Display Employees");
            System.out.println("4. Exit");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            System.out.print("Choose an option:");
            int choice = scanner.nextInt();
            scanner.nextLine(); 
            System.out.println("=========================================================================================================================");
            switch (choice) {
                case 1: // Add Employee
                boolean validId = false;
                Employee employee = null;

                while (!validId) {
                    System.out.println("Enter employee type (fulltime/parttime):");
                    String type = scanner.nextLine();

                    System.out.println("Enter employee name: ");
                    String name = scanner.nextLine();


                    System.out.println("Enter employee ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine(); 
                    
                    if (payrollSystem.employeeIdExists(id)) {
                        System.out.println("Error: Employee ID " + id + " already exists. Please enter a unique ID.");
                        continue;
                    }

                    if (type.equalsIgnoreCase("fulltime")) {
                        System.out.println("Enter monthly salary: ");
                        double monthlySalary = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline
                        employee = new FullTimeEmployee(name, id, monthlySalary);
                    } else if (type.equalsIgnoreCase("parttime")) {
                        System.out.println("Enter hours worked: ");
                        int hoursWorked = scanner.nextInt();
                        System.out.println("Enter hourly rate: ");
                        double hourlyRate = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline
                        employee = new PartTimeEmployee(name, id, hoursWorked, hourlyRate);
                    } else {
                        System.out.println("Invalid employee type.");
                        continue;
                    }

                    validId = true;
                    payrollSystem.addEmployee(employee);
                    System.out.println("Employee Added Successfully !!!");
                }
                break;


                case 2: // Remove Employee
                    System.out.println("Enter employee ID to remove: ");
                    int removeId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    payrollSystem.removeEmployee(removeId);
                    System.out.println("Employee Removed SuccessFully !!!");
                    break;

                case 3: // Display Employees
                    System.out.println("Employee Details: ");
                    payrollSystem.displayEmployees();
                    break;

                case 4: // Exit
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
