package logfile.reader.database;

import logfile.reader.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DataSourceImpl implements DataSource {
    private final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS log_events ( id VARCHAR(64) NOT NULL, duration INT NOT NULL, type VARCHAR (32), host VARCHAR(64),alert BIT)";

    private final String user = "SA";
    private final String pass = "";
    private final String db = "jdbc:hsqldb:file:db/testdb;ifexists=false";
    private final Logger logger = LoggerFactory.getLogger(DataSourceImpl.class.getName());

    public DataSourceImpl() {
        try {
            createTable();
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }


    @Override
    public void createTable() throws SQLException {
        logger.info("Creating table...");
        try (Connection connection = DriverManager.getConnection(db, user, pass)) {

            logger.debug("Creating schema: {} if not exist", "public");
            connection.prepareStatement("CREATE SCHEMA  IF NOT EXISTS public").execute();
            logger.debug("Creating table log_events");
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS log_events ( id VARCHAR(64) NOT NULL, duration INT NOT NULL, type VARCHAR (32), host VARCHAR(64),alert BIT)").execute();

        } catch (SQLException sqlException) {
            logger.error("SQLException occurred!");
            throw sqlException;
        }
        logger.info("Table created.");
    }

    public void printTable() throws SQLException {
        logger.debug("Print log_events table content");
        try (Connection connection = DriverManager.getConnection(db, user, pass)) {
            logger.debug("Select from log_events");
            ResultSet resultSet = connection.prepareStatement("select * from log_events").executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                int duration = resultSet.getInt("duration");
                String type = resultSet.getString("type");
                String host = resultSet.getString("host");
                boolean alert = resultSet.getBoolean("alert");
                logger.debug("res; {}, {}, {}, {}, {}", id, duration, type, host, alert);
            }
        } catch (SQLException exception) {
            logger.error("exception", exception);
            throw exception;
        }
    }

    @Override
    public void saveAlert(Alert alert) {
        try (Connection connection = DriverManager.getConnection(db, user, pass)) {
            PreparedStatement pst = connection.prepareStatement("INSERT INTO log_events VALUES (?,?,?,?,?)");
            pst.setString(1, alert.getId());
            pst.setLong(2, alert.getDuration());
            if (alert.getType() != null)
                pst.setString(3, alert.getType().name());
            else pst.setString(3, null);
            if (alert.getHost() != null)
                pst.setString(4, alert.getHost());
            else pst.setString(4, null);
            pst.setBoolean(5, alert.getAlert());
            pst.execute();
        } catch (SQLException exception) {
            logger.error("Exception occurred", exception);
        }
    }
}
