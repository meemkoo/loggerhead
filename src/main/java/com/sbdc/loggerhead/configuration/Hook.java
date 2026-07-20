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

package com.sbdc.loggerhead.configuration;

import java.util.function.Supplier;

/**
 * Track wether the output of a callable has changes. Update with {@link Hook#update()}. Check if
 * this hook has fired with {@link Hook#hasFired()}.
 */
public class Hook {
  public final Supplier<?> getHookValue;
  private boolean fired = false;
  private Object last;

  public Hook(Supplier<?> getHookValue) {
    this.getHookValue = getHookValue;
    last = getHookValue.get();
  }

  public void update() {
    if (getHookValue.get() != last) {
      fired = true;
    }
    last = getHookValue.get();
  }

  public boolean hasFired() {
    return fired;
  }

  public void reset() {
    fired = false;
  }
}
