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

package com.sbdc.loggerhead.primarylogger;

import com.sbdc.loggerhead.LogMode;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DataLogEntry;
import java.util.function.Consumer;

/**
 * Base class for all primary loggers. Primary loggers would be anything that has a *Publisher or
 * *LogEntry in WPILib
 */
public abstract class AbstractPrimaryLog<T, DLE extends DataLogEntry, P extends Publisher> {
  private final LogMode mode;
  private final String name;

  private DLE logEntry;
  private P ntPublish;

  private Consumer<T> updateIndirection;

  private T last;

  /**
   * @param key Full path the value will be published to
   * @param mode Where the value will be published
   * @param ntInstance NT instance
   * @param dataLog DataLog instance
   */
  protected AbstractPrimaryLog(
      String key, LogMode mode, NetworkTableInstance ntInstance, DataLog dataLog) {
    this.name = getClass() + " " + key;
    this.mode = mode;

    switch (mode) {
      case FileOnly:
        updateIndirection = newValue -> updateFile(newValue);
        break;
      case NetworkOnly:
        updateIndirection = newValue -> updateNetwork(newValue);
        break;
      case Both:
        updateIndirection =
            newValue -> {
              updateFile(newValue);
              updateNetwork(newValue);
            };
        break;
    }
  }

  /**
   * @return The name of this logger
   */
  public String getName() {
    return name;
  }

  /**
   * @return The current log mode
   */
  public LogMode getMode() {
    return mode;
  }

  /**
   * General update method. Will update based on the currently set LogMode
   *
   * @param newValue
   */
  public void update(T newValue) {
    if (last != newValue) {
      updateIndirection.accept(newValue);
    }
  }

  /**
   * @param logEntry The logger's DataLogEntry
   */
  protected void setLogEntry(DLE logEntry) {
    this.logEntry = logEntry;
  }

  /**
   * @param publisher The logger's NetworkTables publisher
   */
  protected void setPublisher(P publisher) {
    this.ntPublish = publisher;
  }

  /**
   * @return The DataLogEntry for this logger.
   */
  protected DLE getLogEntry() {
    return logEntry;
  }

  /**
   * @return The NetworkTables publisher for this logger.
   */
  protected P getPublisher() {
    return ntPublish;
  }

  /**
   * Append value to the DataLogEntry.
   *
   * @param newValue New value to be sent
   */
  protected abstract void updateFile(T newValue);

  /**
   * Publish value to NetworkTables.
   *
   * @param newValue New value to be sent
   */
  protected abstract void updateNetwork(T newValue);
}
