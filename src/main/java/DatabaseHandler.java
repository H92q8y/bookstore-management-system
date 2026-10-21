import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DatabaseHandler
{

    private static final String URL = System.getenv().getOrDefault(
            "BOOKSTORE_DB_URL",
            "jdbc:mysql://localhost:3306/bookstore?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );

    private static final String USER =
            System.getenv().getOrDefault("BOOKSTORE_DB_USER", "root");

    private static final String PASSWORD =
            System.getenv("BOOKSTORE_DB_PASSWORD");

    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // reg
    public static String getUserRole(String username, String password)
    {
        String sql = "SELECT role FROM users WHERE username = ? AND `password` = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql))
        {

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("role");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    // Error of exist users.
    public static boolean registerUser(String username, String password)
    {
        String sql = "INSERT INTO users (username, `password`, role) VALUES (?, ?, 'User')";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql))
        {

            pst.setString(1, username);
            pst.setString(2, password);
            pst.executeUpdate();
            return true;

        }
        catch (SQLException e)
        {
            // Duplicate username in MySQL usually = error code 1062
            if (e.getErrorCode() == 1062)
            {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // BOOKs
    public static ObservableList<Book> getAllBooks()
    {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM books ORDER BY id";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query))
        {

            while (rs.next())
            {
                list.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                ));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;
    }


    public static void addBook(String title, String author, double price, int qty)
    {
        String query = "INSERT INTO books (title, author, price, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(query))
        {

            pst.setString(1, title);
            pst.setString(2, author);
            pst.setDouble(3, price);
            pst.setInt(4, qty);
            pst.executeUpdate();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void updateBook(int id, String title, String author, double price, int qty)
    {
        String query = "UPDATE books SET title=?, author=?, price=?, quantity=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(query))
        {

            pst.setString(1, title);
            pst.setString(2, author);
            pst.setDouble(3, price);
            pst.setInt(4, qty);
            pst.setInt(5, id);
            pst.executeUpdate();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void deleteBook(int id)
    {
        String query = "DELETE FROM books WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(query))
        {

            pst.setInt(1, id);
            pst.executeUpdate();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    // Buy
    public static boolean buyBook(int id, int qtyToBuy)
    {
        String selectSql = "SELECT quantity FROM books WHERE id = ?";
        String updateSql = "UPDATE books SET quantity = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst1 = conn.prepareStatement(selectSql))
        {

            pst1.setInt(1, id);
            ResultSet rs = pst1.executeQuery();

            if (!rs.next()) return false;

            int currentQty = rs.getInt("quantity");
            if (currentQty < qtyToBuy) return false;

            int newQty = currentQty - qtyToBuy;

            try (PreparedStatement pst2 = conn.prepareStatement(updateSql))
            {
                pst2.setInt(1, newQty);
                pst2.setInt(2, id);
                pst2.executeUpdate();
                return true;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }


    //USERS (from Admin)
    public static ObservableList<AppUser> getAllUsers()
    {
        ObservableList<AppUser> list = FXCollections.observableArrayList();
        String query = "SELECT username, role FROM users ORDER BY username";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query))
        {

            while (rs.next())
            {
                list.add(new AppUser(
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;
    }

    public static void deleteUser(String username)
    {
        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(query))
        {

            pst.setString(1, username);
            pst.executeUpdate();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}