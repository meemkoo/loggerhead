#!python3

LICENCE = """

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

"""


import enum
import importlib

class RunMode(enum.Enum):
  RunByRunCodeGen = 0
  RunBySelfAsLib = 1

if __name__ == "__main__":
  import sys
  sys.stdout = sys.stderr
  with sys.stderr as f:
    print("Test", file=f)

  import os

  def get_run_mode():
    return RunMode.RunByRunCodeGen

  for path, files, dirs in os.walk('.\\src'):
    for file in files:
      if file.endswith('.py'):
        importlib.import_module(os.path.join(path, file))
        # with open(os.path.join(path, file), 'rt') as f:
        #   exec(f.read(), globals())
else:
  def get_run_mode():
    return RunMode.RunBySelfAsLib