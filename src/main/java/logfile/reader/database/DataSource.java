package logfile.reader.database;

import logfile.reader.model.Alert;

import java.sql.SQLException;

public interface DataSource {
    void createTable() throws SQLException;
    void printTable() throws SQLException;
    void saveAlert(Alert alert);
}
