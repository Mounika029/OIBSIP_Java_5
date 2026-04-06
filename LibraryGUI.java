import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryGUI extends JFrame implements ActionListener {

    CardLayout cl;
    JPanel mainPanel;

    JTextField userField;
    JPasswordField passField;

    Connection con;
    String currentUser = "";
    String role = "";

    public LibraryGUI() {

        try {
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db",
                "root",
                "2006"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Library System");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        mainPanel.add(loginPanel(), "login");
        mainPanel.add(menuPanel(), "menu");

        add(mainPanel);
        setVisible(true);
    }

    JPanel loginPanel() {
        JPanel p = new JPanel(new GridLayout(3,2));

        p.add(new JLabel("Username:"));
        userField = new JTextField();
        p.add(userField);

        p.add(new JLabel("Password:"));
        passField = new JPasswordField();
        p.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        p.add(loginBtn);

        return p;
    }

    JPanel menuPanel() {
        JPanel p = new JPanel(new GridLayout(6,1));

        String[] ops = {
            "Add Book","View Books","Search Book",
            "Issue Book","Return Book","Exit"
        };

        for(String op: ops){
            JButton b = new JButton(op);
            b.addActionListener(this);
            p.add(b);
        }
        return p;
    }

    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        // LOGIN
        if(cmd.equals("Login")){
            try {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=?"
                );
                ps.setString(1, userField.getText());
                ps.setString(2, new String(passField.getPassword()));

                ResultSet rs = ps.executeQuery();

                if(rs.next()){
                    currentUser = rs.getString("username");
                    role = rs.getString("role");
                    cl.show(mainPanel, "menu");
                } else {
                    JOptionPane.showMessageDialog(this,"Invalid Login");
                }

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // ADD BOOK (Admin)
        else if(cmd.equals("Add Book")){
            if(!role.equals("admin")){
                JOptionPane.showMessageDialog(this,"Admin only!");
                return;
            }

            try {
                String title = JOptionPane.showInputDialog("Title:");
                String author = JOptionPane.showInputDialog("Author:");

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO books(title,author,available) VALUES(?,?,true)"
                );
                ps.setString(1,title);
                ps.setString(2,author);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,"Book Added!");

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // VIEW BOOKS
        else if(cmd.equals("View Books")){
            try {
                ResultSet rs = con.createStatement().executeQuery("SELECT * FROM books");

                JTextArea area = new JTextArea(10,30);

                while(rs.next()){
                    area.append(
                        rs.getInt("id")+" - "+
                        rs.getString("title")+" - "+
                        (rs.getBoolean("available")?"Available":"Issued")+"\n"
                    );
                }

                JOptionPane.showMessageDialog(this,new JScrollPane(area));

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // SEARCH
        else if(cmd.equals("Search Book")){
            try {
                String key = JOptionPane.showInputDialog("Enter title:");

                PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM books WHERE title LIKE ?"
                );
                ps.setString(1,"%"+key+"%");

                ResultSet rs = ps.executeQuery();

                JTextArea area = new JTextArea();

                while(rs.next()){
                    area.append(rs.getString("title")+"\n");
                }

                JOptionPane.showMessageDialog(this,new JScrollPane(area));

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // ISSUE
        else if(cmd.equals("Issue Book")){
            try {
                int id = Integer.parseInt(
                    JOptionPane.showInputDialog("Book ID:")
                );

                PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE books SET available=false WHERE id=?"
                );
                ps1.setInt(1,id);
                ps1.executeUpdate();

                PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO issued_books(username,book_id) VALUES(?,?)"
                );
                ps2.setString(1,currentUser);
                ps2.setInt(2,id);
                ps2.executeUpdate();

                JOptionPane.showMessageDialog(this,"Book Issued");

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // RETURN
        else if(cmd.equals("Return Book")){
            try {
                int id = Integer.parseInt(
                    JOptionPane.showInputDialog("Book ID:")
                );

                PreparedStatement ps = con.prepareStatement(
                    "UPDATE books SET available=true WHERE id=?"
                );
                ps.setInt(1,id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,"Book Returned");

            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // EXIT
        else if(cmd.equals("Exit")){
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new LibraryGUI();
    }
}