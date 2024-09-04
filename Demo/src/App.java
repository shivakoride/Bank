import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App
{
    Scanner sc = new Scanner(System.in);
    public static void main(String[] args) throws Exception
    {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(App::resetDailyLimits, 0, 1, TimeUnit.DAYS);
        App atm = new App();
        atm.performTransaction();

        scheduler.shutdown();

    }
    
    public void withdraw()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();
            sc.nextLine();
            
            System.out.println("Enter amount to be withdrawn:");
            int amount = sc.nextInt();

            String selectQuery = "SELECT balance FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);

            if (rs.next()) 
            {
                int balance = rs.getInt("balance");
                ResultSet sr = null;
                String sql = "Select * from bank WHERE AccountNumber = "+accountNumber;
                sr = st.executeQuery(sql);
                if(sr.next())
                {
                    int pin = sr.getInt("pin");
                    System.out.println("Enter PIN : ");
                    int pwd = sc.nextInt();
                    if(pin == pwd)
                    {
                        if (amount > balance) 
                        {
                            System.out.println("Insufficient Balance");
                        } 
                        else 
                        {
                            int Dailylimit = sr.getInt("dailylimit");
                        // Update the balance in the database
                            if(Dailylimit>=amount)
                            {
                                String updateQuery = "UPDATE bank SET balance = " + (balance - amount) + " WHERE AccountNumber = " + accountNumber;
                                st.executeUpdate(updateQuery);                    
                                System.out.println("Withdrawal successful. Updated balance: " + (balance - amount));
                                String query = "UPDATE bank SET dailylimit = "+ (Dailylimit-amount) +" WHERE AccountNumber = "+accountNumber;
                                st.executeUpdate(query);
                            }
                            else
                            {
                                System.out.println("Dailylimit not satisfied!!");
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Incorrect Pin!!");
                    }
                }
            }       
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private static void resetDailyLimits() {
        Connection con = null;
        PreparedStatement ps = null;
        int maximumDailyLimit = 50000;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);

            // Update the last_reset_date for all accounts to the current date
            String updateQuery = "UPDATE bank SET last_reset_date = CURRENT_DATE, dailylimit = ? WHERE last_reset_date < CURRENT_DATE";
            ps = con.prepareStatement(updateQuery);
            ps.setInt(1, maximumDailyLimit);  // Set your maximum daily limit
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void deposit() 
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try 
        {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();

            System.out.println("Enter amount to be deposited:");
            int amount = sc.nextInt();
            sc.nextLine();

            String selectQuery = "SELECT balance FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);

            if (rs.next()) 
            {
                int balance = rs.getInt("balance");

                // Update the balance in the database
                String updateQuery = "UPDATE bank SET balance = " + (balance + amount) + " WHERE AccountNumber = " + accountNumber;
                st.executeUpdate(updateQuery);
                System.out.println("Deposit successful. Updated balance: " + (balance + amount));
            } 
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void New()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();
            int min = 10000;
            System.out.println("Enter amount to be deposited:");
            int amount = sc.nextInt();
            sc.nextLine();
            if(min>amount)
            {
                System.out.println("Minimum account required is : "+min);
            }
            else{
                System.out.println("The condition for the minimum amount is satisfied!!");
                System.out.println();
                System.out.println("Enter the new pin : ");
                int Pin = sc.nextInt();
                System.out.println("Enter Name : ");
                String Name = sc.nextLine();
                System.out.println("Enter Contact : ");
                int Contact = sc.nextInt();
                String selectQuery = "INSERT into bank(AccountNumber,balance,Pin,Name,Contact,Dailylimit) values(?,?,?,?,?,50000)";
                ps = con.prepareStatement(selectQuery);
                ps.setInt(1, accountNumber);
                ps.setInt(2, amount);
                ps.setInt(3, Pin);
                ps.setString(4, Name);
                ps.setInt(5, Contact);
                int rowseffected = ps.executeUpdate();
                if (rowseffected > 0) {
                    System.out.println("Record inserted successfully!");
                } else {
                    System.out.println("Failed to insert the record.");
                }
                
            }

            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void deleteAccount()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();
            String selectQuery = "SELECT * FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);
            if(rs.next())
            {
                System.out.println("Enter pin to Delete Account : ");
                int pin = sc.nextInt();
                int Pin = rs.getInt("Pin");
                if(pin == Pin)
                {
                    String Query = "DELETE from bank WHERE AccountNumber = ?";
                    ps = con.prepareStatement(Query);
                    ps.setInt(1, accountNumber);
                    int rowseffected = ps.executeUpdate();
                    if (rowseffected > 0)
                    {
                        System.out.println("Record deleted successfully!");
                    } 
                    else 
                    {
                        System.out.println("Failed to delete the record.");
                    }
                    System.out.println();
                }
                else
                {
                    System.out.println("Incorrect Pin!!");
                }
                
            }
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void Changepin()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();
            String selectQuery = "SELECT * FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);
            if(rs.next())
            {
                System.out.println("Enter current pin : ");
                int pin = sc.nextInt();
                int Pin = rs.getInt("Pin");
                if(pin == Pin)
                {
                    System.out.println("Enter new Pin: ");
                    int newpin1 = sc.nextInt();
                    System.out.println("Enter the new Pin again : ");
                    int newpin = sc.nextInt();
                    if(newpin1==newpin)
                    {
                        String Query = "UPDATE bank SET Pin = ? WHERE AccountNumber = ?";
                        ps = con.prepareStatement(Query);
                        ps.setInt(1, newpin);
                        ps.setInt(2, accountNumber);
                        int rowseffected = ps.executeUpdate();
                        if (rowseffected > 0)
                        {
                            System.out.println("Pin changed successfully!");
                        } 
                        else 
                        {
                            System.out.println("Failed to change the Pin!");
                        }
                        System.out.println();
                    }
                    else
                    {
                        System.out.println("New Pin is not same!!");
                    }
                }
                else
                {
                    System.out.println("Incorrect Pin!!");
                }
                
            }
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void performTransaction()
    {
        try {
            while(true)
            {
            System.out.println("Choose transaction type:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. AccountDetails");
            System.out.println("4. New Account");
            System.out.println("5. Delete Account");
            System.out.println("6. Change Pin");
            System.out.println("7. Change Contact");
            System.out.println("8. Exit");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    deposit();
                    break;
                case 2:
                    withdraw();
                    break;
                case 3:
                    AccountDetails();
                    break;
                case 4:
                    New();
                    break;
                case 5:
                    deleteAccount();
                    break;
                case 6:
                    Changepin();
                    break;
                case 7:
                    Changecontact();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }}catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void AccountDetails()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            st = con.createStatement();
            String use = "use atm";
            st.execute(use);
            System.out.println("Enter Account Number : ");
            int accountNumber = sc.nextInt();
            sc.nextLine();
            String selectQuery = "SELECT * FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);
            if(rs.next())
            {
                int pin = rs.getInt("Pin");
                System.out.println("Enter PIN : ");
                int pwd = sc.nextInt();
                if(pin == pwd)
                {
                    java.sql.ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    System.out.println("Account Details of Account Number "+accountNumber+" are : ");
                    System.out.println();
                    for (int j = 1; j <= columnCount; j++) {
                        System.out.print(String.format("%-20s", metaData.getColumnName(j)));
                    }
                    System.out.println();

                    // Print table data
                    do {
                        for (int j = 1; j <= columnCount; j++) {
                            System.out.print(String.format("%-20s", rs.getString(j)));
                        }
                        System.out.println();
                    } while (rs.next());
                    System.out.println();
                }
                else
                {
                    System.out.println("Incorrect Pin!!");
                }
                
            }
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } 
        catch (Exception e) 
        {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    public void Changecontact()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();
            String selectQuery = "SELECT * FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);
            if(rs.next())
            {
                sc.nextLine();
                System.out.println("Enter current Contact : ");
                
                try 
                {
                    long contact = sc.nextLong();
                    long Contact = rs.getLong("Contact");
                    // Now you have the integer value in 'currentContact'
                    if(Contact == contact)
                    {
                        System.out.println("Enter new Contact: ");
                        long newcontact1 = sc.nextLong();
                        
                        System.out.println("Enter the new Contact again : ");
                        long newcontact = sc.nextLong();
                        if(newcontact1==newcontact)
                        {
                            String Query = "UPDATE bank SET Contact = ? WHERE AccountNumber = ?";
                            ps = con.prepareStatement(Query);
                            ps.setLong(1, newcontact);
                            ps.setInt(2, accountNumber);
                            int rowseffected = ps.executeUpdate();
                            if (rowseffected > 0)
                            {
                                System.out.println("Contact changed successfully!");
                            } 
                            else 
                            {
                                System.out.println("Failed to change the Contact!");
                            }
                            System.out.println();
                        }
                        else
                        {
                            System.out.println("New Contact entries donot match!!");
                        }
                    }
                    else
                    {
                        System.out.println("Incorrect Contact!!");
                    }
                } 
                catch (NumberFormatException e) 
                {
                    System.out.println("Invalid input for contact. Please enter a valid integer.");
                    // Handle the exception or prompt the user to enter a valid integer
                }
                
                
            }
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void FixedDeposit()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try 
        {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "f.r.i.e.n.d.s");
            con.setAutoCommit(true);
            st = con.createStatement();
            String use = "USE atm";
            st.execute(use);
            System.out.println("Enter account number:");
            int accountNumber = sc.nextInt();

            System.out.println("Enter amount to be deposited:");
            int amount = sc.nextInt();
            sc.nextLine();

            String selectQuery = "SELECT balance FROM bank WHERE AccountNumber = " + accountNumber;
            rs = st.executeQuery(selectQuery);

            if (rs.next()) 
            {
                int balance = rs.getInt("balance");

                // Update the balance in the database
                String updateQuery = "UPDATE bank SET balance = " + (balance + amount) + " WHERE AccountNumber = " + accountNumber;
                st.executeUpdate(updateQuery);
                System.out.println("Deposit successful. Updated balance: " + (balance + amount));
            } 
            else
            {
                System.out.println("There is no Account Number with : "+accountNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in a finally block to ensure they are always closed
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

