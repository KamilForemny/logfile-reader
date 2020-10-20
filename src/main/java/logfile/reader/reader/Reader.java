package logfile.reader.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import logfile.reader.database.DataSourceImpl;
import logfile.reader.model.Alert;
import logfile.reader.model.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class Reader {

    public static final long MAX_TIME_DIFFERENCE = 4;

    private final Logger logger = LoggerFactory.getLogger(Reader.class);
    private final ObjectMapper mapper;
    private final DataSourceImpl dataSource;
    private final ConcurrentHashMap<String, Log> logHashMap = new ConcurrentHashMap<>();

    public Reader(String fileName) {
        this.mapper = new ObjectMapper();
        this.dataSource = new DataSourceImpl();
        try {
            readLine(fileName);
        } catch (IOException | SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void readLine(String fileName) throws IOException, SQLException {
        logger.info("Starting reading file: {}", fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            for (String line; (line = reader.readLine()) != null; ) {
                Log log = mapLine(line);
                Log mapLog = logHashMap.get(log.getId());

                if (mapLog != null) {
                    long duration = Math.abs(log.getTimestamp() - mapLog.getTimestamp());
                    boolean alert = duration > MAX_TIME_DIFFERENCE;
                    saveAlert(log, duration, alert);
                    logHashMap.remove(log.getId());
                } else {
                    logHashMap.put(log.getId(), log);
                }
            }
        } catch (IOException ioException) {
            logger.error("IOException occurred");
            throw ioException;
        }
        logger.info("Reading file: {} completed.", fileName);

//        dataSource.printTable();
    }


    private void saveAlert(Log newLog, long duration, boolean alert) {
        dataSource.saveAlert(new Alert(
                newLog.getId(),
                duration,
                newLog.getType(),
                newLog.getHost(),
                alert
        ));
    }


    private Log mapLine(String line) {
        Log log = null;
        try {
            log = mapper.readValue(line, Log.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return log;
    }

}
