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

package com.sbdc.loggerhead.compoundlogger;

import com.sbdc.loggerhead.LogMode;
import com.sbdc.loggerhead.Table;

/**
 * Interface for all CompoundLoggers. Compound loggers serve as a kind of logging wrapper for
 * existing objects. See {@link LogCTREDrivetrain}, {@link LogNetworkXboxController}, or anything
 * under the {@code com.sbdc.loggerhead.compoundlogger} package.
 */
public interface CompoundLogger {
  /**
   * @return The name of this CompoundLogger
   */
  public default String getName() {
    return getClass().getName();
  }

  /** Update this CompoundLogger */
  public default void update() {}

  /**
   * @return The log mode of this CompoundLogger
   */
  public abstract LogMode getLogMode();

  /**
   * Initalize this CompoundLogger by registering any child loggers with the (now available) path
   *
   * @param parentTable
   */
  public abstract void initialize(Table parentTable);
}
