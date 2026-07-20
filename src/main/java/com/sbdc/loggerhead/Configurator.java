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

import com.sbdc.loggerhead.configuration.Hook;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.event.EventLoop;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Handles configuration of robot logging. Do not instantiate */
public final class Configurator {
  private Runnable preConfig =
      () ->
          DriverStation.reportError(
              "preConfig has not been set. This will probably leak memory over time in your robot program. This is likely not a problem with your code, but is likely a problem with the Loggerhead",
              false);
  private Runnable postConfig = () -> {};
  private Runnable configureCallback =
      () ->
          DriverStation.reportWarning(
              "Unset logging configurator. Please call call setConfigureCallback()", false);
  private final EventLoop eventLoop = new EventLoop();
  private final List<Hook> hooks = new ArrayList<>();

  protected Configurator(Runnable preConfig, Runnable postConfig) {
    this.preConfig = preConfig;
    this.postConfig = postConfig;
  }

  /**
   * Adds a hook to the configurator using hookValuegetter as the input.
   *
   * @param hookValueGetter supplier of a value
   * @return Configurator instance for chaining
   */
  public Configurator addHook(Supplier<?> hookValueGetter) {
    Hook newHook = new Hook(hookValueGetter);
    eventLoop.bind(newHook::update);
    hooks.add(newHook);
    return this;
  }

  /**
   * Set the user configuration function
   *
   * @param configureCallback
   * @return Configurator for method chaining
   */
  public Configurator setConfigureCallback(Runnable configureCallback) {
    this.configureCallback =
        () -> {
          System.out.println("Configuring RootLogger");
          preConfig.run();
          configureCallback.run();
          postConfig.run();
        };
    return this;
  }

  /**
   * @return currently set configure callback
   */
  public Runnable getConfiguratorCallback() {
    return configureCallback;
  }

  /**
   * Checks all added hooks and if they have fired. If any hooks fire, the configure callback will
   * be called, along with preConfig and postConfig.
   */
  public void checkAllHooks() {
    eventLoop.poll();
    hooks.forEach(
        hook -> {
          if (hook.hasFired()) {
            configureCallback.run();
            hook.reset();
          }
        });
  }
}
