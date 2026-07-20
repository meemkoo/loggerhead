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

package com.sbdc.loggerhead;

import com.sbdc.loggerhead.compoundlogger.CompoundLogger;
import com.sbdc.loggerhead.exceptions.LoggingTableRootDefinedError;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
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
  public static boolean hasRoot() {
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
    if (hasRoot()) {
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
  // TODO: Generate these someday?

  /**
   * Add a string logger to the Loggerhead instance, prefixed with this tables full path
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
   * Add a double logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the double logger without slashes
   * @param mode Logging mode for the string logger
   * @param doubleGetter Callable providing the double
   * @return This table for chaining
   */
  public Table addDoubleLogger(String key, LogMode mode, Supplier<Double> doubleGetter) {
    loggerhead.addDoubleLogger(path + key, mode, doubleGetter);
    return this;
  }

  /**
   * Add a integer logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the integer logger without slashes
   * @param mode Logging mode for the integer logger
   * @param integerGetter Callable providing the integer
   * @return This table for chaining
   */
  public Table addIntegerLogger(String key, LogMode mode, Supplier<Integer> integerGetter) {
    loggerhead.addIntegerLogger(path + key, mode, integerGetter);
    return this;
  }

  /**
   * Add a boolean logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the boolean logger without slashes
   * @param mode Logging mode for the boolean logger
   * @param boolGetter Callable providing the boolean
   * @return This table for chaining
   */
  public Table addBooleanLogger(String key, LogMode mode, Supplier<Boolean> boolGetter) {
    loggerhead.addBooleanLogger(path + key, mode, boolGetter);
    return this;
  }

  /**
   * Add a pose logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the pose logger without slashes
   * @param mode Logging mode for the pose logger
   * @param poseGetter Callable providing the pose
   * @return This table for chaining
   */
  public Table addPoseLogger(String key, LogMode mode, Supplier<Pose2d> poseGetter) {
    loggerhead.addPoseLogger(path + key, mode, poseGetter);
    return this;
  }

  /**
   * Add a swerve state logger to the Loggerhead instance, prefixed with this tables full path
   *
   * @param key Name of the swerve state logger without slashes
   * @param mode Logging mode for the swerve state logger
   * @param moduleStateGetter Callable providing the swerve state
   * @return This table for chaining
   */
  public Table addSwerveStateLogger(
      String key, LogMode mode, Supplier<SwerveModuleState[]> moduleStateGetter) {
    loggerhead.addSwerveStateLogger(path + key, mode, moduleStateGetter);
    return this;
  }

  /**
   * Add a compound logger to the Loggerhead instance. The compound logger is responsible for
   * placing child loggers. See {@link CompoundLogger#initialize(String)}
   *
   * @param compoundLogger The compound logger
   * @return This table for chaining
   */
  public Table addCompoundLogger(CompoundLogger compoundLogger) {
    loggerhead.addCompoundLogger(compoundLogger);
    compoundLogger.initialize(path);
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
