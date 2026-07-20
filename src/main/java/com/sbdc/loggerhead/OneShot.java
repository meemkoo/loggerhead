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

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.IntegerLogEntry;
import edu.wpi.first.util.datalog.StringLogEntry;

/**
 * Untility class to publish values to NetworkTables and write values to logs one time. Useful for
 * setting a NetworkTables {@code ".type"} string or anything that gets broadcast one time or
 * infrequently.
 */
public class OneShot {
  /**
   * Publishes a string value to NetworkTables and the log. Do not call repeatedly.
   *
   * @param key NetworkTables key
   * @param value The string
   */
  public static void setString(String key, String value) {
    StringPublisher publish = NetworkTableInstance.getDefault().getStringTopic(key).publish();
    publish.set(value);
    StringLogEntry log = new StringLogEntry(Loggerhead.getInstance().getDataLog(), key);
    log.append(value);
  }

  /**
   * Publishes a double value to NetworkTables and the log. Do not call repeatedly.
   *
   * @param key NetworkTables key
   * @param value The double
   */
  public static void setDouble(String key, Double value) {
    var publish = NetworkTableInstance.getDefault().getDoubleTopic(key).publish();
    publish.set(value);
    DoubleLogEntry log = new DoubleLogEntry(Loggerhead.getInstance().getDataLog(), key);
    log.append(value);
  }

  /**
   * Publishes an integer value to NetworkTables and the log. Do not call repeatedly.
   *
   * @param key NetworkTables key
   * @param value The integer
   */
  public static void setInteger(String key, Integer value) {
    var publish = NetworkTableInstance.getDefault().getIntegerTopic(key).publish();
    publish.set(value);
    IntegerLogEntry log = new IntegerLogEntry(Loggerhead.getInstance().getDataLog(), key);
    log.append(value);
  }

  /**
   * Publishes a boolean value to NetworkTables and the log. Do not call repeatedly.
   *
   * @param key NetworkTables key
   * @param value The boolean
   */
  public static void setBoolean(String key, Boolean value) {
    var publish = NetworkTableInstance.getDefault().getBooleanTopic(key).publish();
    publish.set(value);
    BooleanLogEntry log = new BooleanLogEntry(Loggerhead.getInstance().getDataLog(), key);
    log.append(value);
  }
}
