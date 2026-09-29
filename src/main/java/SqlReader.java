import java.sql.*;
import java.io.*;
public class SqlReader {
	public static void rExecute(Connection connection, String path) throws SQLException{
		execute(connection, read(new File(path)));
	}
	
	public static void rExecute(Connection connection, File file) throws SQLException{
		execute(connection, read(file));
	}
	
	public static String read(File file) {
		StringBuilder script = new StringBuilder();
		try {
			String line;
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) !=null) {
				script.append(line).append("\n");
			}
			fr.close();
			br.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("File not found exception: " + ex.getMessage());
		}
		catch(IOException ex) {
			System.out.println("IO exception: " + ex.getMessage());
		}
		return script.toString();
	}
	
	public static void execute(Connection connection, String script) throws SQLException{
		Statement statement = connection.createStatement();
		String[] commands = script.split(";");
		String cleanedcommand;
		for (String command : commands) {
			if (!(cleanedcommand = command.trim()).isEmpty()) {
				statement.addBatch(cleanedcommand);
			}
		}
		statement.executeBatch();
		statement.close();
		
	}
}
