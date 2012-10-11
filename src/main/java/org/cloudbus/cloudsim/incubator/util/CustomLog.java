package org.cloudbus.cloudsim.incubator.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.commons.io.output.NullOutputStream;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Replaces the primitive functionality of the standard CloudSim Log. Allows
 * easily to redirect log to a file, and to customize the output.
 * 
 * <br/>
 * 
 * Before using this class remember to call the {@link CustomLog.configLogger}
 * with the desired properties, or with an empty Properties if you want to use
 * the defaults.
 * 
 * 
 * @author Nikolay Grozev
 * 
 *         Adapted from versions of:
 *         <ol>
 *         <li>Anton Beloglazov</li>
 *         <li>William Voorsluys</li>
 *         <li>Adel Nadjaran Toosi</li>
 *         </ol>
 * 
 * @since CloudSim Toolkit 2.0
 */
public class CustomLog {

    // //// Configuration properties
    /**
     * A key for the config property specifying what is the minimal logged
     * level.
     */
    public static final String LOG_LEVEL_PROP_KEY = "LogLevel";
    /**
     * A key for a boolean property, specifying if every log entry should start
     * with the current CloudSim time.
     */
    public static final String LOG_CLOUD_SIM_CLOCK_PROP_KEY = "LogCloudSimClock";
    /**
     * Specifies which methods of the {@link LogRecord} class should be used to
     * create the log entries. Calls must be seprated with a semicolon ";". For
     * example "getLevel;getMessage".
     */
    public static final String LOG_FORMAT_PROP_KEY = "LogFormat";
    /**
     * A key for a file property where the log is to be written. If not
     * specified the standard output is used.
     */
    public static final String FILE_PATH_PROP_KEY = "FilePath";
    /**
     * A key for a boolean property specifying whther the standard CloudSim
     * logger should be turned off. That will cause all the log generated by the
     * system classes of CloudSim not to be printed..
     */
    public static final String SHUT_STANDART_LOGGER_PROP_KEY = "ShutStandardLogger";

    /**
     * The default log level used by this log, if not specified.
     */
    public static Level DEFAULT_LEVEL = Level.INFO;

    private static Logger logger;
    private static Level granularityLevel;
    private static Formatter formatter;

    /**
     * Prints the message passed as a non-String object. Simply uses toString
     * implementation.
     * 
     * @param message
     *            - the message.
     * @param level
     *            - the level to use. If null the default level is used.
     */
    public static void print(Object message, Level level) {
	logger.log(
		level == null ? DEFAULT_LEVEL : level, String.valueOf(message));
    }

    /**
     * Prints the message passed as a non-String object. Simply uses toString
     * implementation. Uses the default log level.
     * 
     * @param message
     *            - the message.
     */
    public static void print(Object message) {
	print(message, DEFAULT_LEVEL);
    }

    /**
     * Prints a line with the message to the log.
     * 
     * @param level
     *            - the log level. If null, the default log level is used.
     * @param msg
     *            - the message. Must not be null.
     */
    public static void printLine(Level level, String msg) {
	logger.log(level == null ? DEFAULT_LEVEL : level, msg);
    }

    /**
     * Prints a line with the message to the log. Uses the default log level.
     * 
     * @param msg
     *            - the message. Must not be null.
     */
    public static void printLine(String msg) {
	printLine(null, msg);
    }

    /**
     * Prints the formatted string, resulting from applying the format string to
     * the arguements.
     * 
     * @param format
     *            - the format (as in String.format).
     * @param level
     *            - the level. If null the default level is used
     * @param args
     */
    public static void printf(Level level, String format, Object... args) {
	logger.log(level == null ? DEFAULT_LEVEL : level, String.format(format, args));
    }

    /**
     * Returns if this logger is disabled.
     * 
     * @return - if this logger is disabled.
     */
    public static boolean isDisabled() {
	return logger.getLevel().equals(Level.OFF);
    }

    /**
     * Sets the output of this logger. This method is to be used for redirecting
     * to "nonstandard" (e.g. database) output streams. If you simply want to
     * redirect the logger to a file, you'd better use the initialization
     * properties.
     * 
     * @param output
     *            - the new output. Must not be null.
     */
    public static void setOutput(OutputStream output) {
	logger.addHandler(new StreamHandler(output, formatter));
    }

    /**
     * Returns a nicely formatted representation of the current CloudSim time.
     * 
     * @return a nicely formatted representation of the current CloudSim time.
     */
    public static String formatClockTime() {
	return TextUtil.toString(CloudSim.clock());
    }

    /**
     * Configures the logger. Must be called before the logger is used.
     * 
     * @param props
     *            - the configuration properties. See the predefined keys in
     *            this class, to get an idea of what is required.
     * @throws SecurityException
     *             - if the specified log format contains invalid method calls.
     * @throws IOException
     *             - if something goes wrong with the I/O.
     */
    public static void configLogger(final Properties props)
	    throws SecurityException, IOException {
	if (logger == null) {
	    final boolean logInFile = props.containsKey(FILE_PATH_PROP_KEY);
	    final String fileName = logInFile ? props.getProperty(
		    FILE_PATH_PROP_KEY).toString() : null;
	    final String format = props.getProperty(LOG_FORMAT_PROP_KEY,
		    "getLevel;getMessage").toString();
	    final boolean prefixCloudSimClock = Boolean.parseBoolean(props
		    .getProperty(LOG_CLOUD_SIM_CLOCK_PROP_KEY, "false")
		    .toString());
	    final boolean shutStandardMessages = Boolean.parseBoolean(props
		    .getProperty(SHUT_STANDART_LOGGER_PROP_KEY, "false")
		    .toString());
	    granularityLevel = Level.parse(props.getProperty(
		    LOG_LEVEL_PROP_KEY, "FINE").toString());

	    if (shutStandardMessages) {
		Log.setOutput(new NullOutputStream());
	    }

	    logger = Logger.getLogger(CustomLog.class.getPackage().getName());
	    logger.setUseParentHandlers(false);

	    formatter = new CustomFormatter(prefixCloudSimClock, format);

	    if (logInFile) {
		System.err.println("Rediricting output to " + new File(fileName).getAbsolutePath());
	    }

	    StreamHandler handler = logInFile ? new FileHandler(fileName, false)
		    : new ConsoleHandler();
	    handler.setLevel(granularityLevel);
	    handler.setFormatter(formatter);
	    logger.addHandler(handler);
	    logger.setLevel(granularityLevel);
	}
    }

    private static class CustomFormatter extends Formatter {

	private boolean prefixCloudSimClock;
	private String format;

	public CustomFormatter(boolean prefixCloudSimClock, String format) {
	    super();
	    this.prefixCloudSimClock = prefixCloudSimClock;
	    this.format = format;
	}

	@Override
	public String format(LogRecord record) {
	    String[] methodCalls = format.split(";");
	    StringBuffer result = new StringBuffer();
	    if (prefixCloudSimClock) {
		result.append(formatClockTime() + "\t");
	    }

	    int i = 0;
	    for (String method : methodCalls) {
		try {
		    result.append(record.getClass().getMethod(method)
			    .invoke(record));
		} catch (Exception e) {
		    System.err.println("Error in logging:");
		    e.printStackTrace(System.err);
		    System.exit(1);
		}
		if (i++ < methodCalls.length - 1) {
		    result.append("\t");
		}
	    }
	    result.append(TextUtil.NEW_LINE);

	    return result.toString();
	}
    }
}