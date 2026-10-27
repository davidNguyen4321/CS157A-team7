package Auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
    
    @Override
    public void contextDestroyed(ServletContextEvent e) {
        Connection connection = DatabaseAccess.getConnection();
        if(connection!=null) {
        	try {
        		connection.close();
        	}
        	catch(Exception ex) {
        		ex.printStackTrace();
        	}
        }
    }
}