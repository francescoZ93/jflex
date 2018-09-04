/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.7.0-SNAPSHOT                                                    *
 * Copyright (C) 1998-2015  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package jflex;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import jflex.unicode.UnicodeProperties;

/**
 * In this class all output to the java console is filtered.
 *
 * <p>Use the switches verbose, time and DUMP at compile time to determine the verbosity of JFlex
 * output. There is no switch for suppressing error messages. verbose and time can be overridden by
 * command line parameters.
 *
 * <p>Redirects output to a TextArea in GUI mode.
 *
 * <p>Counts error and warning messages.
 *
 * @author Gerwin Klein
 * @version JFlex 1.7.0-SNAPSHOT
 */
public final class Out {

  /** platform dependent newline sequence */
  public static final String NL = System.getProperty("line.separator");

  /** count total warnings */
  private int warnings;

  /** count total errors */
  private int errors;

  /** output device */
  private final StdOutWriter out;

  private final Options options;

  public Out(ByteArrayOutputStream out, Options options) {
    this(new StdOutWriter(out), options);
  }

  public Out(Options options) {
    this(new StdOutWriter(), options);
  }

  public Out(OutputStream out, Options options) {
    this(new StdOutWriter(out), options);
  }

  public Out(StdOutWriter out, Options options) {
    this.out = out;
    this.options = options;
  }

  /**
   * Report time statistic data.
   *
   * @param message the message to be printed
   * @param time elapsed time
   */
  public void time(ErrorMessages message, Timer time) {
    if (options.timing()) {
      String msg = ErrorMessages.get(message, time.toString());
      out.println(msg);
    }
  }

  /**
   * Report time statistic data.
   *
   * @param message the message to be printed
   */
  public void time(String message) {
    if (options.timing()) {
      out.println(message);
    }
  }

  /**
   * Report generation progress.
   *
   * @param message the message to be printed
   */
  public void println(String message) {
    if (options.verbose()) out.println(message);
  }

  /**
   * Report generation progress.
   *
   * @param message the message to be printed
   * @param data data to be inserted into the message
   */
  public void println(ErrorMessages message, String data) {
    if (options.verbose()) {
      out.println(ErrorMessages.get(message, data));
    }
  }

  /**
   * Report generation progress.
   *
   * @param message the message to be printed
   * @param data data to be inserted into the message
   */
  public void println(ErrorMessages message, int data) {
    if (options.verbose()) {
      out.println(ErrorMessages.get(message, data));
    }
  }

  /**
   * Report generation progress.
   *
   * @param message the message to be printed
   */
  public void print(String message) {
    if (options.verbose()) out.print(message);
  }

  /**
   * Dump debug information to System.out
   *
   * <p>Use like this <code>if (Out.DEBUG) log.debug(message)</code> to save performance during
   * normal operation (when DEBUG is turned off).
   *
   * @param message a {@link java.lang.String} object.
   */
  public static void debug(String message) {
    if (Options.DEBUG) {System.out.println(message);}
  }

  /**
   * All parts of JFlex, that want to provide dump information should use this method for their
   * output.
   *
   * @message the message to be printed
   * @param message a {@link java.lang.String} object.
   */
  public void dump(String message) {
    if (options.dump()) out.println(message);
  }

  /**
   * All parts of JFlex, that want to report error messages should use this method for their output.
   *
   * @message the message to be printed
   */
  private void err(String message) {
    out.println(message);
  }

  /** throws a GeneratorException if there are any errors recorded */
  public void checkErrors() {
    if (errors > 0) throw new GeneratorException(ErrorMessages.WRONG_SKELETON);
  }

  /** print error and warning printStatistics */
  public void printStatistics() {
    StringBuilder line = new StringBuilder(errors + " error");
    if (errors != 1) line.append("s");

    line.append(", ").append(warnings).append(" warning");
    if (warnings != 1) line.append("s");

    line.append(".");
    err(line.toString());
  }

  /** reset error and warning counters */
  public void resetCounters() {
    errors = 0;
    warnings = 0;
  }

  /**
   * print a warning without position information
   *
   * @param message the warning message
   */
  public void warning(String message) {
    warnings++;

    err(NL + "Warning : " + message);
  }

  /**
   * print a warning message without line information
   *
   * @param message code of the warning message
   * @see ErrorMessages
   */
  public void warning(ErrorMessages message) {
    warning(message, 0);
  }

  /**
   * print a warning with line information
   *
   * @param message code of the warning message
   * @param line the line information
   * @see ErrorMessages
   */
  public void warning(ErrorMessages message, int line) {
    warnings++;

    String msg = NL + "Warning";
    if (line > 0) msg = msg + " in line " + (line + 1);

    err(msg + ": " + ErrorMessages.get(message));
  }

  /**
   * print warning message with location information
   *
   * @param file the file the warning is issued for
   * @param message the code of the message to print
   * @param line the line number of the position
   * @param column the column of the position
   */
  public void warning(File file, ErrorMessages message, int line, int column) {

    String msg = NL + "Warning";
    if (file != null) msg += " in file \"" + file + "\"";
    if (line >= 0) msg = msg + " (line " + (line + 1) + ")";

    try {
      err(msg + ": " + NL + ErrorMessages.get(message));
    } catch (ArrayIndexOutOfBoundsException e) {
      err(msg);
    }

    warnings++;

    if (line >= 0) {
      if (column >= 0) showPosition(file, line, column);
      else showPosition(file, line);
    }
  }

  /**
   * print error message (string)
   *
   * @param message the message to print
   */
  public void error(String message) {
    errors++;
    err(NL + message);
  }

  /**
   * print error message (code)
   *
   * @param message the code of the error message
   * @see ErrorMessages
   */
  public void error(ErrorMessages message) {
    errors++;
    err(NL + "Error: " + ErrorMessages.get(message));
  }

  /**
   * print error message with data
   *
   * @param data data to insert into the message
   * @param message the code of the error message
   * @see ErrorMessages
   */
  public void error(ErrorMessages message, String data) {
    errors++;
    err(NL + "Error: " + ErrorMessages.get(message, data));
  }

  /**
   * IO error message for a file (displays file name in parentheses).
   *
   * @param message the code of the error message
   * @param file the file it occurred for
   */
  public void error(ErrorMessages message, File file) {
    errors++;
    err(NL + "Error: " + ErrorMessages.get(message) + " (" + file + ")");
  }

  /**
   * print error message with location information
   *
   * @param file the file the error occurred for
   * @param message the code of the error message to print
   * @param line the line number of error position
   * @param column the column of error position
   */
  public void error(File file, ErrorMessages message, int line, int column) {

    String msg = NL + "Error";
    if (file != null) msg += " in file \"" + file + "\"";
    if (line >= 0) msg = msg + " (line " + (line + 1) + ")";

    try {
      err(msg + ": " + NL + ErrorMessages.get(message));
    } catch (ArrayIndexOutOfBoundsException e) {
      err(msg);
    }

    errors++;

    if (line >= 0) {
      if (column >= 0) showPosition(file, line, column);
      else showPosition(file, line);
    }
  }

  /**
   * prints a line of a file with marked position.
   *
   * @param file the file of which to show the line
   * @param line the line to show
   * @param column the column in which to show the marker
   */
  public void showPosition(File file, int line, int column) {
    try {
      String ln = getLine(file, line);
      if (ln != null) {
        err(ln);

        if (column < 0) return;

        String t = "^";
        for (int i = 0; i < column; i++) t = " " + t;

        err(t);
      }
    } catch (IOException e) {
      /* silently ignore IO errors, don't show anything */
    }
  }

  /**
   * print a line of a file
   *
   * @param file the file to show
   * @param line the line number
   */
  public void showPosition(File file, int line) {
    try {
      String ln = getLine(file, line);
      if (ln != null) err(ln);
    } catch (IOException e) {
      /* silently ignore IO errors, don't show anything */
    }
  }

  /**
   * get one line from a file
   *
   * @param file the file to read
   * @param line the line number to get
   * @throw IOException if any error occurs
   */
  private static String getLine(File file, int line) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));

    String msg = "";

    for (int i = 0; i <= line; i++) msg = reader.readLine();

    reader.close();

    return msg;
  }

  /** Print system information (e.g. in case of unexpected exceptions) */
  public void printSystemInfo() {
    // TODO(regisd): why is systeminfo sent to stderr?
    err("Java version:     " + System.getProperty("java.version"));
    err("Runtime name:     " + System.getProperty("java.runtime.name"));
    err("Vendor:           " + System.getProperty("java.vendor"));
    err("VM version:       " + System.getProperty("java.vm.version"));
    err("VM vendor:        " + System.getProperty("java.vm.vendor"));
    err("VM name:          " + System.getProperty("java.vm.name"));
    err("VM info:          " + System.getProperty("java.vm.info"));
    err("OS name:          " + System.getProperty("os.name"));
    err("OS arch:          " + System.getProperty("os.arch"));
    err("OS version:       " + System.getProperty("os.version"));
    err("Encoding:         " + System.getProperty("file.encoding"));
    err("Unicode versions: " + UnicodeProperties.UNICODE_VERSIONS);
    err("JFlex version:    " + Main.version);
  }

  /**
   * Request a bug report for an unexpected Exception/Error.
   *
   * @param e a {@link java.lang.Error} object.
   */
  public void requestBugReport(Error e) {
    err("An unexpected error occurred. Please send a report of this to");
    err("<bugs@jflex.de> and include the following information:");
    err("");
    printSystemInfo();
    err("Exception:");
    e.printStackTrace(out);
    err("");
    err("Please also include a specification (as small as possible)");
    err("that triggers this error. You may also want to check at");
    err("http://www.jflex.de if there is a newer version available");
    err("that doesn't have this problem");
    err("");
    err("Thanks for your support.");
  }
}
