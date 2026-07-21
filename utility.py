#!/usr/bin/env python3
from dataclasses import dataclass
from pathlib import Path
import shutil, os
import sys
import os
import re
import hashlib
import datetime
from xml.etree import ElementTree
from xml.dom import minidom
from typing import Callable


operations: dict[str, Callable] = {}
shortkeys = {}

def operation(func: Callable):
    operations[func.__name__] = func
    return func


MAVEN_METADATA_STRING = """<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>{group_id}</groupId>
  <artifactId>{artifact_id}</artifactId>
  <versioning>
    <latest>{latest1}</latest>
    <release>{latest2}</release>
    <versions>
{versions_xml}
    </versions>
    <lastUpdated>{now}</lastUpdated>
  </versioning>
</metadata>
"""


def checksum(path: str, algo: str) -> str:
    h = hashlib.new(algo)
    with open(path, "rb") as f:
        h.update(f.read())
    return h.hexdigest()


versions_valid = [
    "0.0.1",
    "0.0.2",
    "0.0.2-dev-Whee!",
    "0.0.3",
    "0.2.0",
    "0.2.0-devB",
    "0.3.0",
    "0.3.1",
    "0.3.1-dev-A",
    "3.2.0",
    "1.12.2",
    "3.12.2-dev-unstable",
    "3.14.2-dev",
    "3.13.2-unstable",
    "3.12.2",
    "3.14.2",
    "3.13.2",
    "1.7.10",
]


# General utils

VersionTag = object # Dummy object, because WSL's python got mad at the static methods

@dataclass
class VersionTag:
    x: int
    y: int
    z: int

    isDev: bool

    isUnstable: bool

    parts: list[str]

    def __str__(self):
        base = f"{self.x}.{self.y}.{self.z}"
        if len(self.parts) == 0:
            rest = ''
        else:
            rest = '-'+('-'.join(self.parts))
        return base+rest

    @staticmethod
    def sort_version(versions: list[VersionTag]):
        return sorted(versions, key= lambda x: (x.x, x.y, x.z))

    @staticmethod
    def is_valid_version(v: str):
        try:
            VersionTag.validate_version(v)
            return True
        except Exception as e:
            return False

    @staticmethod
    def validate_version(version: str):
        parts = version.split('-')
        assert len(parts) >= 1, "Empty numerical version"
        assert len(parts[0].split('.')) == 3, "Version should be x.y.z in inital portion"
        try:
            [int(i) for i in parts[0].split('.')]
        except Exception as e:
            print('Version numbers have to be numbers')
            exit(-1)

    @staticmethod
    def validate_many_versions(versions: list[str]):
        assert len(set(versions)) == len(versions), "Non unique value"
        for version in versions:
            VersionTag.validate_version(version)

    @staticmethod
    def from_string(version: str, validate=True):
        if validate:
            VersionTag.validate_version(version)
        parts = version.split('-')
        if True in ['dev' in p for p in parts]:
            dev = True
        else:
            dev = False

        if True in ['unstable' in p for p in parts]:
            unstable = True
        else:
            unstable = False

        vparts = parts[0].split('.')
        x = int(vparts[0])
        y = int(vparts[1])
        z = int(vparts[2])
        return VersionTag(x, y, z, dev, unstable, parts[1:])

    @staticmethod
    def from_string_array(versions: list[str]):
        VersionTag.validate_many_versions(versions)
        return [VersionTag.from_string(v, False) for v in versions]


def no_dev(versions: list[VersionTag]):
    return list(filter(lambda x: not x.isDev, versions))


def no_unstable(versions: list[VersionTag]):
    return list(filter(lambda x: not x.isUnstable, versions))


def get_max_major_version(versions: list[VersionTag]):
    # Determine major
    major = -1
    for v in versions:
        if v.x > major:
            major = v.x

    if major == None:
        print('Empty major version')
        quit(-1)

    return major


# Javadoc

def determine_javadoc_kill_keep_list(versions: list[VersionTag]):
    stable = no_unstable(no_dev(versions))

    majors = set([v.x for v in stable])
    max_maj = sorted(tuple(majors), reverse=True)[0]

    stable_by_major = {n: [] for n in majors}
    for v in stable:
        stable_by_major[v.x].append(v)

    kill_list = []
    keep_list = []

    keep_list += stable_by_major.pop(max_maj)

    for maj, vlist in stable_by_major.items():
        vsorted = sorted(vlist, key=lambda x: x.y)
        vlist.clear()
        if len(vsorted) < 3:
            keep_list += vsorted
        else: # Its longer
            keep_list.append(vsorted.pop(0))
            keep_list.append(vsorted.pop(-1))
            kill_list += vsorted
        pass

    assert len(kill_list)+len(keep_list)==len(stable)

    return kill_list, keep_list

@operation
def clean_javadoc_main(repo: Path):
    javadoc_dir = repo.joinpath('javadoc')
    versions = [VersionTag.from_string(j) for j in [i for i in os.listdir(javadoc_dir) if VersionTag.is_valid_version(i)]]
    print("Github workflows really can see new commits!")
    if len(versions) != 0:
        kill_list, keep_list = determine_javadoc_kill_keep_list(versions)
        for i in kill_list:
            shutil.rmtree(javadoc_dir.joinpath(str(i)))
    pass

@operation
def install_new_javadoc(new_javadoc: Path, repo: Path, version: str | VersionTag):
    if isinstance(version, str):
        version = VersionTag.from_string(version)

    try:
        shutil.copytree(new_javadoc, repo.joinpath('javadoc').joinpath(str(version)))
    except Exception as e:
        print(e)
        print("Version likely already exists")
        quit(-1)

@operation
def generate_javadoc_index(repo: Path) -> None:
    javadoc_root = repo.joinpath('javadoc')

    versions_all = VersionTag.sort_version(
        VersionTag.from_string_array(
            [
                d
                for d in os.listdir(javadoc_root)
                if d != "index.html"
                and os.path.isdir(os.path.join(javadoc_root, d))
            ]
        )
    )

    stable_versions = [v for v in versions_all if not v.isDev and not v.isUnstable]
    latest_stable_version = stable_versions.pop(-1)
    dev_versions = [v for v in versions_all if v.isDev and not v.isUnstable]
    unstable_versions = [v for v in versions_all if not v.isDev and v.isUnstable]
    other_versions = [v for v in versions_all if v.isDev and v.isUnstable]

    root = ElementTree.Element('html', {'lang': 'en'})
    head = ElementTree.SubElement(root, 'head')
    ElementTree.SubElement(head, 'meta', {'charset': 'utf-8'})
    title = ElementTree.SubElement(head, 'title')
    title.text = 'Loggerhead Javadoc Portal'
    body = ElementTree.SubElement(root, 'body')
    heading1 = ElementTree.SubElement(body, 'h1')
    heading1.text = 'Loggerhead Javadoc Portal'

    heading2 = ElementTree.SubElement(body, 'h2')
    heading2.text = 'Stable'
    stable_list = ElementTree.SubElement(body, 'ul')

    heading3 = ElementTree.SubElement(body, 'h2')
    heading3.text = 'Unstable'
    unstable_list = ElementTree.SubElement(body, 'ul')

    heading4 = ElementTree.SubElement(body, 'h2')
    heading4.text = 'Dev'
    dev_list = ElementTree.SubElement(body, 'ul')

    heading5 = ElementTree.SubElement(body, 'h2')
    heading5.text = 'Others'
    other_list = ElementTree.SubElement(body, 'ul')

    latest_entry = ElementTree.SubElement(stable_list, 'li')
    latest_a = ElementTree.SubElement(latest_entry, 'a', {'href': f"{str(latest_stable_version)}/index.html"})
    latest_a.text = f'latest ({latest_stable_version})'

    for v in stable_versions:
        entry = ElementTree.SubElement(stable_list, 'li')
        a = ElementTree.SubElement(entry, 'a', {'href': f"{str(v)}/index.html"})
        a.text = str(v)

    for v in dev_versions:
        entry = ElementTree.SubElement(dev_list, 'li')
        a = ElementTree.SubElement(entry, 'a', {'href': f"{str(v)}/index.html"})
        a.text = str(v)

    for v in unstable_versions:
        entry = ElementTree.SubElement(unstable_list, 'li')
        a = ElementTree.SubElement(entry, 'a', {'href': f"{str(v)}/index.html"})
        a.text = str(v)

    for v in other_versions:
        entry = ElementTree.SubElement(other_list, 'li')
        a = ElementTree.SubElement(entry, 'a', {'href': f"{str(v)}/index.html"})
        a.text = str(v)

    strung: bytes = ElementTree.tostring(root, 'utf-8', short_empty_elements=False)

    with open(os.path.join(javadoc_root, "index.html"), "w") as f:
        f.write(strung.decode('utf-8'))

    print(f"Indexed some javadoc versions")


# Maven

@operation
def generate_maven_metadata(root: Path):
    assert root.exists(), "Path doesnt exist"
    # repo_root = sys.argv[1] if len(sys.argv) > 1 else "build/repos/releases"
    # repo_root = os.path.normpath(repo_root)
    repo_root = root.joinpath('maven')
    updated = 0

    for dirpath, dirnames, _filenames in os.walk(repo_root):
        # An "artifact directory" is one whose immediate subdirectories each
        # contain a .pom file -- i.e. version directories.
        version_dirs = []
        for d in dirnames:
            sub = os.path.join(dirpath, d)
            if any(f.endswith(".pom") for f in os.listdir(sub)):
                version_dirs.append(d)

        if not version_dirs:
            continue

        artifact_id = os.path.basename(dirpath)
        rel = os.path.relpath(dirpath, repo_root)
        group_id = rel.replace(os.sep, ".").rsplit("." + artifact_id, 1)[0]

        # versions = sorted(version_dirs, key=version_sort_key)
        versions = version_dirs
        latest = versions[-1]
        versions_xml = "\n".join(f"      <version>{v}</version>" for v in versions)
        now = datetime.datetime.utcnow().strftime("%Y%m%d%H%M%S")

        metadata = MAVEN_METADATA_STRING.format(group_id=group_id, artifact_id=artifact_id, latest1=latest, latest2=latest, versions_xml=versions_xml, now=now)

        meta_path = os.path.join(dirpath, "maven-metadata.xml")
        with open(meta_path, "w") as f:
            f.write(metadata)
        for algo in ("md5", "sha1"):
            with open(f"{meta_path}.{algo}", "w") as f:
                f.write(checksum(meta_path, algo))

        updated += 1
        print(f"Updated metadata: {group_id}:{artifact_id} -> versions {versions}")

    if updated == 0:
        print(f"::warning::No artifact directories found under {repo_root}")

@operation
def install_new_maven_repo(repo: Path, new_maven: Path):
    shutil.copytree(new_maven, repo.joinpath('maven'), dirs_exist_ok=True)



# MkDocs

@operation
def move_mkdocs(repo: Path, new_docs_root: Path, version: str | VersionTag):
    if isinstance(version, str):
        version = VersionTag.from_string(version)
    
    devdir = repo.joinpath('devdocs')
    proddir = repo.joinpath('docs')

    if version.isDev or version.isUnstable:
        try:
            shutil.copytree(new_docs_root, devdir.joinpath(str(version)))
        except FileExistsError as e:
            print("Gang, this version already exists")
            quit(-1)
    else:
        shutil.rmtree(proddir)
        try:
            shutil.copytree(new_docs_root, proddir)
        except FileExistsError as e:
            print("Gang, this version already exists")
            quit(-1)

    pass


# Vendordep

@operation
def install_new_vendordep(repo: Path, new_vendordep: Path, version: str | VersionTag):
    if isinstance(version, str):
        version = VersionTag.from_string(version)
    
    try:
        os.remove(repo.joinpath('vendordeps/Loggerhead.json'))
    except FileNotFoundError:
        pass

    shutil.copyfile(new_vendordep, repo.joinpath(f'vendordeps/Loggerhead-{str(version)}.json'))
    shutil.copyfile(repo.joinpath(f'vendordeps/Loggerhead-{str(version)}.json'), repo.joinpath(f'vendordeps/Loggerhead.json'))



# Clears

@operation
def clear_devdocs(repo: Path):
    shutil.rmtree(repo.joinpath('devdocs'))
    os.mkdir(repo.joinpath('devdocs'))

@operation
def clear_dev_unstable_maven(repo: Path, cleardev=True, clearunstable=False):
    mavenpath = repo.joinpath("maven")
    e = list(os.walk(mavenpath))

    for directory,dirs,files in e:
        for dir in dirs:
            try:
                v = VersionTag.from_string(dir) # Its a valid version if this passes
                if (v.isDev and cleardev) or (v.isUnstable and clearunstable):
                    shutil.rmtree(Path(directory).joinpath(dir))
            except AssertionError as e:
                pass

    pass

@operation
def clear_dev_unstable_vendordep(repo: Path, cleardev=True, clearunstable=False):
    vendordeppath = repo.joinpath("vendordeps")

    for file in os.listdir(vendordeppath):
        try:
            v = VersionTag.from_string(file[len('Loggerhead-'):-len('.json')]) # Its a valid version if this passes
            if (v.isDev and cleardev) or (v.isUnstable and clearunstable):
                os.remove(Path(vendordeppath).joinpath(file))
        except AssertionError as e:
            pass

    pass


def make_shortkeys():
    for long, func in operations.items():
        parts = long.split('_')
        letterlist = [i[0] for i in parts]
        shortkeys[''.join(letterlist)] = func
    assert len(operations)==len(shortkeys), "Bad operation names"


def main():
    print(f"Working with: {sys.argv}")
    make_shortkeys()

    args = sys.argv[1:]
    assert len(args) >- 1, "Bad args"
    op = args[0]
    if (op not in operations) and (op not in shortkeys):
        print("Bad args")
        quit(-1)
    
    opargs = args[1:]
    true_args = []
    for arg in opargs:
        argparts = arg.split(':')
        assert len(argparts) > 1, "Bad sub arg"
        argtype = argparts[0]
        raw_arg = ''.join(argparts[1:])
        match argtype:
            case 'bool':
                true_args.append(raw_arg == 'True')
            case 'path':
                true_args.append(Path(raw_arg))
            case 'str':
                true_args.append(raw_arg)
            case _:
                print("Really bad sub arg")
                quit(-1)

    if op in operations:
        operations[op](*true_args)
    else:
        shortkeys[op](*true_args)



if __name__ == "__main__":
    main()
