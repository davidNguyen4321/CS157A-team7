import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;

public class AddReport {
	Connection connection;
	Starter starter;
	StarterPopulator starterpopulator;
	public AddReport(Connection connection, StarterPopulator starterpopulator) {
		this.connection = connection;
		this.starter = starterpopulator.starter;
		this.starterpopulator = starterpopulator;
	}
	
	public void addReport(String reportdescription, String contenttype, String reason, int offendingmaterialid, int supportingmaterialid, int reportedid, int reporterid) throws SQLException{
	}
}
