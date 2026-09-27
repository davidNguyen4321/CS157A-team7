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
//		String evidence = MessageFormat.format("'{'\"offending material id\":\"{0}\"", offendingmaterialid);
//		evidence += addEvidence (contenttype, offendingmaterialid);
//		if (reason.equals("Copyright")) {
//			evidence += MessageFormat.format(",\"original material id\":\"{0}\"", supportingmaterialid);
//			evidence += addEvidence (contenttype, supportingmaterialid);
//		}
//		evidence += "}";
//		
//		String reportdescriptioncleaned = this.starterpopulator.clean(reportdescription);
//		String evidencecleaned = this.starterpopulator.clean(evidence);
//		
//		String addreport = MessageFormat.format(this.starterpopulator.doubleSingleQuote("""
//        INSERT INTO REPORTS (REPORT_DESCRIPTION, CONTENT_TYPE, REASON, EVIDENCE, REPORTED_ID, REPORTER_ID)
//    	VALUES ({0}, '{1}', '{2}', {3}, {4}, {5})"""), reportdescriptioncleaned, contenttype, reason, evidencecleaned, reportedid, reporterid);
//		this.starterpopulator.execute(addreport);
	}
	
//	public String addEvidence(String contenttype, int id) throws SQLException{
//		switch (contenttype) {
//		case "Recipe":
//			return snapshot("RECIPES", "RECIPE_ID", id);
//		case "Review":
//			return snapshot("REVIEWS", "REVIEW_ID", id);
//		case "User":
//			return snapshot("USERS", "USER_ID", id);
//		default:
//			return "";
//	}
//	}
//	
//	public String snapshot(String tablename, String idname, int id) throws SQLException{
//		Statement statement = connection.createStatement();
//		String query = MessageFormat.format(starterpopulator.doubleSingleQuote("SELECT * FROM {0} WHERE {1} = ") + id, tablename, idname);
//		ResultSet resultset = statement.executeQuery(query);
//		ResultSetMetaData metadata = resultset.getMetaData();
//		int columncount = metadata.getColumnCount();
//		String evidence = "";
//		
//		if (resultset.next()) {
//			for (int i = 1; i<=columncount; i++) {
//				String columnname = metadata.getColumnName(i);
//				Object columnvalue = resultset.getObject(i);
//				evidence += ",\"" + columnname + "\":\"" + columnvalue + "\"";
//			}
//		}
//		statement.close();
//		resultset.close();
//		return evidence;
//	}
}
