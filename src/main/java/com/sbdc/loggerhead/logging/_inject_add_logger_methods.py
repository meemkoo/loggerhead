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

from pathlib import Path
import sys, os

FOUND_IT = False
last = Path(__file__).resolve().parent
while not FOUND_IT:
  if 'generators' in os.listdir(last):
    FOUND_IT = True
  else:
    last = last.parent
sys.path.append(str(last))
from generators.run_code_gen import *
print(get_run_mode())

from dataclasses import dataclass, field
import enum
from copy import deepcopy

PUB_CLASS_TEMP = "{fqcls}Publisher"
LOG_ENTRY_CLASS_TEMP = "{fqcls}LogEntry"
FULL_QUALIFIED_TEMP = "Primary{fqcls}Log"

IMPORT_TEMP_ALL = [
"com.sbdc.loggerhead.LogMode",
"edu.wpi.first.networktables.NetworkTableInstance",
"edu.wpi.first.util.datalog.DataLog",
]



@dataclass
class ClassDescriptor:
  base_type: str = ""
  wrapper_type: str = ""
  pub: str = ""
  log: str = ""
  full_class_name: str = ""
  is_array: bool = False



def get_big_8_dt():
  all_cds = [ClassDescriptor() for _ in range(8)]
  for i, wrapper in enumerate(["Boolean", "String", "Integer", "Double"]):
    if wrapper in ("Double", "Boolean"):
      base = wrapper.lower()
    elif wrapper == "Integer":
      base = "long"
    else:
      base = wrapper

    all_cds[i].wrapper_type = wrapper
    all_cds[i].base_type = base
    all_cds[i].pub = PUB_CLASS_TEMP.format(fqcls=wrapper)
    all_cds[i].log = LOG_ENTRY_CLASS_TEMP.format(fqcls=wrapper)
    all_cds[i].full_class_name = wrapper

    array_fq = wrapper + "Array"
    # array_base = base + "[]"

    all_cds[i+4].wrapper_type = wrapper+"[]"
    all_cds[i+4].base_type = base+"[]"
    all_cds[i+4].pub = PUB_CLASS_TEMP.format(fqcls=wrapper+"Array")
    all_cds[i+4].log = LOG_ENTRY_CLASS_TEMP.format(fqcls=wrapper+"Array")
    all_cds[i+4].full_class_name = wrapper+"Array"
    all_cds[i+4].is_array = True

  return all_cds


def generate_table_java_inject():
  cds = get_big_8_dt()
  methods: list[JavaMethod] = []

  for cd in cds:
    getterCamel = f"{cd.full_class_name}"
    getterCamel = getterCamel[0].lower() + getterCamel[1:]

    javadoc = f"""Add a {cd.full_class_name} logger to the Loggerhead instance, prefixed with this tables full path

@param key Name of the string logger without slashes
@param mode Logging mode for the string logger
@param {getterCamel}Getter Callable providing the string
@return This table for chaining
"""
    
    body = f"""
loggerhead.add{cd.full_class_name}Logger(path + key, mode, {getterCamel}Getter);
return this;
"""

    method = JavaMethod(name=f"add{cd.full_class_name}Logger", body=body, return_="Table", 
                        annotations=[], mods=[JavaModifiers.PUBLIC], is_constructor=False, 
                        params=[
                          JavaDeclaration("key", "String"),
                          JavaDeclaration("mode", "LogMode"),
                          JavaDeclaration(f"{getterCamel}Getter", f"Supplier<{cd.base_type if cd.is_array else cd.wrapper_type}>"), 
                        ], jdoc=javadoc)
    methods.append(method)

  struct_stuff = """
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
  """

  return '\n'.join(map(lambda x: x.generate(), methods)) + struct_stuff


def generate_loggerhead_java_inject():
  cds = get_big_8_dt()
  methods: list[JavaMethod] = []

  for cd in cds:
    getterCamel = f"{cd.full_class_name}"
    getterCamel = getterCamel[0].lower() + getterCamel[1:]

    javadoc = f"""Add a {cd.full_class_name} logger to the Loggerhead instance

@param key Name of the string logger without slashes
@param mode Logging mode for the string logger
@param {getterCamel}Getter Callable providing the string
"""
    
    body = f"""
Primary{cd.full_class_name}Log logPub = new Primary{cd.full_class_name}Log(key, mode, ntInst, log);
SourceUpdateMap<Primary{cd.full_class_name}Log, {cd.base_type if cd.is_array else cd.wrapper_type}> compundLogger =
    new SourceUpdateMap<>(this, logPub, {getterCamel}Getter);
primaryLogs.put(key, compundLogger);
"""

    method = JavaMethod(name=f"add{cd.full_class_name}Logger", body=body, return_="void", 
                        annotations=[], mods=[JavaModifiers.PUBLIC], is_constructor=False, 
                        params=[
                          JavaDeclaration("key", "String"),
                          JavaDeclaration("mode", "LogMode"),
                          JavaDeclaration(getterCamel + "Getter", f"Supplier<{cd.base_type if cd.is_array else cd.wrapper_type}>"), 
                        ], jdoc=javadoc)
    methods.append(method)

  struct_stuff = """public <T, S extends Struct<T>> Loggerhead addStructLogger(
String key, LogMode mode, Supplier<T> moduleStateGetter, Struct<T> struct) {
PrimaryStructLog<T, S> logPub = new PrimaryStructLog<>(key, mode, ntInst, log, struct);
SourceUpdateMap<PrimaryStructLog<T, S>, T> compundLogger =
    new SourceUpdateMap<>(this, logPub, moduleStateGetter);
primaryLogs.put(key, compundLogger);

return this;
}

public <T, S extends Struct<T>> Loggerhead addStructArrayLogger(
  String key, LogMode mode, Supplier<T[]> valueGetter, Struct<T> struct) {
PrimaryStructArrayLog<T, S> logPub =
    new PrimaryStructArrayLog<>(key, mode, ntInst, log, struct);
SourceUpdateMap<PrimaryStructArrayLog<T, S>, T[]> mapping =
    new SourceUpdateMap<>(this, logPub, valueGetter);
primaryLogs.put(key, mapping);
return this;
}
"""

  return '\n'.join(map(lambda x: x.generate(), methods)) + '\n' + struct_stuff


@dataclass
class Section:
  name: str = ""
  lines: list[str] = field(default_factory=list)


def section_lines(string: str, startchar: str, endchar: str):
  result = []
  stack = [result]
  current = ""

  for line in string:
    if line.strip().startswith(startchar):
      if current:
        stack[-1].append(current)
        current = ""
      new_list = []
      stack[-1].append(new_list)
      stack.append(new_list)
    elif line.strip().startswith(endchar):
      if current:
        stack[-1].append(current)
        current = ""
      stack.pop()
    else:
      current += line + '\n'

  if current:
    stack[-1].append(current)

  return result


def inject(original_text: str, sectcount: int, replacemap: dict[int, tuple[int, str]]):
  START_SYMBOL = "// LGH BEGIN GENERATED 596a96cc7bf9108cd896f33c44aedc8a"
  END_SYMBOL = "// LGH END GENERATED 596a96cc7bf9108cd896f33c44aedc8a"

  sectioned = section_lines(original_text.splitlines(), START_SYMBOL, END_SYMBOL)
  measured_count = len([i for i in sectioned if isinstance(i, list)])
  if measured_count != sectcount:
    raise Exception()
  
  for section, replace in replacemap.items():
    if not isinstance(sectioned[section], list):
      raise Exception()

    if len(sectioned[section]) != replace[0]:
      raise Exception()
    
    sectioned[section] = f"{START_SYMBOL}\n{replace[1]}\n{END_SYMBOL}" 

  return '\n'.join(sectioned)


def run_codegen():
  print("testpoint$$")
  print(Path(__file__).parent)

  root = Path(__file__).parent
  
  table_java_path = root.joinpath("Table.java")
  loggerhead_java_path = root.joinpath("Loggerhead.java")

  table_inject = generate_table_java_inject()
  loggerhead_inject = generate_loggerhead_java_inject()

  with open(table_java_path, 'rt') as f:
    original_table_java = f.read()

  with open(loggerhead_java_path, 'rt') as f:
    original_loggerhead_java = f.read()

  injected_table = inject(original_table_java, 1, {1: (1, table_inject)})
  with open(table_java_path, 'wt') as f:
    f.write(injected_table)

  injected_loggerhead = inject(original_loggerhead_java, 1, {1: (1, loggerhead_inject)})
  with open(loggerhead_java_path, 'wt') as f:
    f.write(injected_loggerhead)

  pass

run_codegen()
