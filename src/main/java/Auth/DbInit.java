package Auth;

import java.sql.DriverManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import Search.DatabaseAccess;

@WebListener
public class DbInit implements ServletContextListener {
	// opens the DB connection at server startup
    @Override
    public void contextInitialized(ServletContextEvent e) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String pw = System.getenv("RG_DB_PASSWORD");
            DatabaseAccess.setConnection(DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC", "root", pw));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}