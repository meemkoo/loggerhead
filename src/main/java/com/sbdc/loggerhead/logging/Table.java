/*
 Copyright (C) 2026  Evan Hansen

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published
 by the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.sbdc.loggerhead.logging;

import com.sbdc.loggerhead.logging.compoundlogger.CompoundLogger;
import com.sbdc.loggerhead.logging.exceptions.LoggingTableRootDefinedError;
import edu.wpi.first.util.struct.Struct;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Way of structuring loggers other than manually building paths for loggers. Each table provides
 * methods to get subtables and add loggers to them. It should be noted that no Table actually
 * maintains references to any logger. Calls to add*Logger methods simply reference methods on
 * {@link Loggerhead}
 */
public class Table {
  private static Table ROOT;

  /**
   * Sets the global root table instance
   *
   * @param table New root table
   */
  private static void setRoot(Table table) {
    if (table != null) {
      ROOT = table;
    }
  }

  /**
   * @return True if the root table has been set
   */
  public static boolean rootTableHasBeenSet() {
    return ROOT != null;
  }

  /** Table name */
  public final String name;

  /** Full path of table */
  public final String path;

  /** This tables parent */
  private final Table parent;

  /** Reference to loggerhead instance */
  private final Loggerhead loggerhead;

  /** All subtables of this table */
  private final HashMap<String, Table> subTables = new HashMap<>();

  /**
   * @param name name of this table
   * @param loggerhead Loggerhead reference
   * @param parent parent table
   */
  public Table(String name, Loggerhead loggerhead, Table parent) {
    this.parent = parent;
    this.loggerhead = loggerhead;
    this.name = name;
    this.path = parent.path + name + "/";
  }

  /**
   * Constructor for the root table
   *
   * @param loggerhead loggerhead instance
   */
  protected Table(Loggerhead loggerhead) {
    if (rootTableHasBeenSet()) {
      throw new LoggingTableRootDefinedError();
    }
    this.parent = this;
    setRoot(this);
    this.loggerhead = loggerhead;
    this.name = "";
    this.path = "";
  }

  /**
   * Creates subtable table or gets an existing subtable, then applies a callable to it
   *
   * @param newTableName Subtable name
   * @param applyToTable Callable accepting the Subtable
   * @return The parent table for chaining
   */
  public Table applyToSubTable(String newTableName, Consumer<Table> applyToTable) {
    Table table = getSubTable(newTableName);
    applyToTable.accept(table);
    return this;
  }

  /**
   * Creates subtable with name, or gets the existing subtable with that name
   *
   * @param newTableName Subtable's name
   * @return Subtable
   */
  public Table getSubTable(String newTableName) {
    if (subTables.containsKey(newTableName)) {
      return subTables.get(newTableName);
    }

    Table newTable = new Table(newTableName, loggerhead, this);
    subTables.put(newTableName, newTable);
    return newTable;
  }

  /**
   * @return HashMap mapping all subtable names to thier subtables
   */
  public HashMap<String, Table> getSubTables() {
    return subTables;
  }

  /**
   * @return This tables parent. On a root table this will loop back to itself
   */
  public Table getParent() {
    return parent;
  }

  /** Clears all subtables from this tables */
  public void clearSubtables() {
    subTables.clear();
  }

  // --- Logger adder methods ---

  // LGH BEGIN GENERATED 596a96cc7bf9108cd896f33c44aedc8a

  /**
   * * Add a Boolean logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param booleanGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addBooleanLogger(String key, LogMode mode, Supplier<Boolean> booleanGetter) {

    loggerhead.addBooleanLogger(path + key, mode, booleanGetter);
    return this;
  }

  /**
   * * Add a String logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param stringGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addStringLogger(String key, LogMode mode, Supplier<String> stringGetter) {

    loggerhead.addStringLogger(path + key, mode, stringGetter);
    return this;
  }

  /**
   * * Add a Integer logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param integerGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addIntegerLogger(String key, LogMode mode, Supplier<Integer> integerGetter) {

    loggerhead.addIntegerLogger(path + key, mode, integerGetter);
    return this;
  }

  /**
   * * Add a Double logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param doubleGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addDoubleLogger(String key, LogMode mode, Supplier<Double> doubleGetter) {

    loggerhead.addDoubleLogger(path + key, mode, doubleGetter);
    return this;
  }

  /**
   * * Add a BooleanArray logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param booleanArrayGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addBooleanArrayLogger(
      String key, LogMode mode, Supplier<boolean[]> booleanArrayGetter) {

    loggerhead.addBooleanArrayLogger(path + key, mode, booleanArrayGetter);
    return this;
  }

  /**
   * * Add a StringArray logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param stringArrayGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addStringArrayLogger(
      String key, LogMode mode, Supplier<String[]> stringArrayGetter) {

    loggerhead.addStringArrayLogger(path + key, mode, stringArrayGetter);
    return this;
  }

  /**
   * * Add a IntegerArray logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param integerArrayGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addIntegerArrayLogger(
      String key, LogMode mode, Supplier<long[]> integerArrayGetter) {

    loggerhead.addIntegerArrayLogger(path + key, mode, integerArrayGetter);
    return this;
  }

  /**
   * * Add a DoubleArray logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the string logger without slashes
   * @param mode Logging mode for the string logger
   * @param doubleArrayGetter Callable providing the string
   * @return This table for chaining
   */
  public Table addDoubleArrayLogger(
      String key, LogMode mode, Supplier<double[]> doubleArrayGetter) {

    loggerhead.addDoubleArrayLogger(path + key, mode, doubleArrayGetter);
    return this;
  }

  public <T> Table addStructLogger(
      String key, LogMode mode, Supplier<T> structGetter, Struct<T> struct) {
    loggerhead.addStructLogger(path + key, mode, structGetter, struct);
    return this;
  }

  public <T> Table addStructLoggerArray(
      String key, LogMode mode, Supplier<T[]> structGetter, Struct<T> struct) {
    loggerhead.addStructArrayLogger(path + key, mode, structGetter, struct);
    return this;
  }

  // LGH END GENERATED 596a96cc7bf9108cd896f33c44aedc8a

  /**
   * Add a compound logger to the Loggerhead instance. The compound logger is responsible for
   * placing child loggers. See
   *
   * @param compoundLogger The compound logger
   * @return This table for chaining
   */
  public Table addCompoundLogger(CompoundLogger compoundLogger) {
    loggerhead.addCompoundLogger(compoundLogger);
    compoundLogger.initialize(this);
    return this;
  }

  public Table addLoggable(Loggable loggable, LogMode logMode) {
    loggable.setupLogging(this, logMode, loggerhead);
    return this;
  }

  public Table addLoggableUnder(String name, Loggable loggable, LogMode logMode) {
    loggable.setupLogging(new Table(name, loggerhead, this), logMode, loggerhead);
    return this;
  }
}
