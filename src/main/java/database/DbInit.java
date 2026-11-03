package database;

import java.sql.Connection;
import java.sql.DriverManager;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DbInit implements ServletContextListener {
	
    @Override
    public void contextInitialized(ServletContextEvent e) {
    	try {
    		Class.forName("com.mysql.cj.jdbc.Driver");
    		
    	    String dbUrl = "jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC";
    	    String dbUser = "root";
    	    String dbPassword = System.getenv("RG_DB_PASSWORD");
    	            
    	    DbAccess.setConnection(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
    	} catch (Exception ex) {
    	    ex.printStackTrace();
    	}
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent e) {
        Connection connection = DbAccess.getConnection();
        if(connection != null) {
        	try {
        		connection.close();
        	}
        	catch(Exception ex) {
        		ex.printStackTrace();
        	}
        }
    }
}