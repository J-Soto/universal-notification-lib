#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
#
# Optional ENV vars
#   JAVA_HOME - location of a JDK home dir
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != true ] || set -x

# OS specific support.
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

# set JAVACMD and JAVACCMD
set_java_home() {
  # Even though the subroutine name is set_java_home, we actually
  # temporary change HOME for the duration of this subroutine.
  if [ -n "${JAVA_HOME-}" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
      JAVACCMD="$JAVA_HOME/jre/sh/javac"
    else
      JAVACMD="$JAVA_HOME/bin/java"
      JAVACCMD="$JAVA_HOME/bin/javac"
    fi
    if [ ! -x "$JAVACMD" ] || [ ! -x "$JAVACCMD" ]; then
      echo "The JAVA_HOME environment variable is not defined correctly, so Apache Maven cannot be started." >&2
      echo "JAVA_HOME is set to \"$JAVA_HOME\", but \"\$JAVA_HOME/bin/java\" or \"\$JAVA_HOME/bin/javac\" does not exist." >&2
      return 1
    fi
  else
    JAVACMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v java
    )" || :
    JAVACCMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v javac
    )" || :
    if [ ! -x "${JAVACMD-}" ] || [ ! -x "${JAVACCMD-}" ]; then
      echo "The java/javac command does not exist in PATH nor is JAVA_HOME set, so Apache Maven cannot be started." >&2
      return 1
    fi
  fi
}

# hash string like Java String::hashCode
hash_string() {
  str="${1:-}" h=0
  while [ -n "$str" ]; do
    char="${str%"${str#?}"}"
    h=$(((h * 31 + $(LC_CTYPE=C printf %d "'$char")) % 4294967296))
    str="${str#?}"
  done
  printf %x\\n $h
}

verbose() { :; }
[ "${MVNW_VERBOSE-}" != true ] || verbose() { printf %s\\n "${1-}"; }

die() {
  printf %s\\n "$1" >&2
  exit 1
}

trim() {
  # MWRAPPER-139:
  #   Trims trailing carriage returns and whitespace
  printf "%s" "${1}" | tr -d '[\r\n]' | xargs
}

# parse distributionUrl and optional distributionSha256Sum, requires .mvn/wrapper/maven-wrapper.properties
while IFS="=" read -r key value; do
  case "${key-}" in
  distributionUrl) distributionUrl=$(trim "${value-}") ;;
  distributionSha256Sum) distributionSha256Sum=$(trim "${value-}") ;;
  esac
done <"${0%/*}/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || die "cannot read distributionUrl property in ${0%/*}/.mvn/wrapper/maven-wrapper.properties"

case "${distributionUrl##*/}" in
maven-mvnd-*bin.*)
  MVN_CMD=mvnd.sh _MVNW_REPO_PATTERN=/maven/mvnd/
  case "${PROCESSOR_ARCHITECTURE-}${PROCESSOR_ARCHITEW6432-}:$(uname -a)" in
  *AMD64:CYGWIN* | *AMD64:MINGW*) distributionPlatform=windows-amd64 ;;
  :Darwin*x86_64) distributionPlatform=darwin-amd64 ;;
  :Darwin*arm64) distributionPlatform=darwin-aarch64 ;;
  :Linux*aarch64*) distributionPlatform=linux-aarch64 ;;
  :*) distributionPlatform=linux-amd64 ;;
  esac
  distributionUrl="${distributionUrl%-bin.*}-${distributionPlatform}.zip"
  ;;
maven-mvnd-*) nakedUrl="${distributionUrl##*/}" && distributionUrl="${distributionUrl%%/*}/maven-mvnd-${nakedUrl#maven-mvnd-}" ;;
*) MVN_CMD="mvn${0##*/mvnw}" _MVNW_REPO_PATTERN=/org/apache/maven/ ;;
esac

# apply MVNW_REPOURL and calculate MAVEN_HOME
# maven home pattern: ~/.m2/wrapper/dists/{apache-maven-<version>,bindist}/<checksum>
[ -z "${MVNW_REPOURL-}" ] || distributionUrl="$MVNW_REPOURL$_MVNW_REPO_PATTERN${distributionUrl#*"$_MVNW_REPO_PATTERN"}"
distributionUrlName="${distributionUrl##*/}"
distributionUrlNameMain="${distributionUrlName%%.*}"
distributionUrlNameMain="${distributionUrlNameMain%-bin}"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
MAVEN_HOME="${MAVEN_USER_HOME}/wrapper/dists/${distributionUrlNameMain-}/$(hash_string "$distributionUrl")"

exec_maven() {
  unset MVNW_VERBOSE MVNW_USERNAME MVNW_PASSWORD MVNW_REPOURL || :
  exec "$MAVEN_HOME/bin/$MVN_CMD" "$@" || die "cannot exec $MAVEN_HOME/bin/$MVN_CMD"
}

if [ -d "$MAVEN_HOME" ]; then
  verbose "found existing MAVEN_HOME at $MAVEN_HOME"
  exec_maven "$@"
fi

case "${distributionUrl-}" in
*?-bin.zip | *?maven-mvnd-?*-?*.zip) ;;
*) die "distributionUrl is not valid, must end with *-bin.zip, but found '${distributionUrl-}'" ;;
esac

# prepare tmp dir
if TMP_DOWNLOAD_DIR="$(mktemp -d)" && [ -d "$TMP_DOWNLOAD_DIR" ]; then
  clean() { rm -rf -- "$TMP_DOWNLOAD_DIR"; }
  trap clean HUP INT TERM EXIT
else
  die "cannot create temp dir"
fi

mkdir -p -- "${MAVEN_HOME%/*}"

# Download and Install Apache Maven
verbose "Couldn't find MAVEN_HOME, downloading and installing it ..."
verbose "Downloading from: $distributionUrl"
verbose "Downloading to: $TMP_DOWNLOAD_DIR/$distributionUrlName"

# Use .mvn/wrapper/maven-wrapper.jar if it exists
if [ -f "${0%/*}/.mvn/wrapper/maven-wrapper.jar" ]; then
  verbose "Found .mvn/wrapper/maven-wrapper.jar"
  set_java_home || die "failed to set JAVA_HOME"

  if [ -n "${MVNW_USERNAME-}" ] && [ -n "${MVNW_PASSWORD-}" ]; then
    MVNW_AUTH="-Dhttp.proxyUser=$MVNW_USERNAME -Dhttp.proxyPassword=$MVNW_PASSWORD -Dhttps.proxyUser=$MVNW_USERNAME -Dhttps.proxyPassword=$MVNW_PASSWORD"
  else
    MVNW_AUTH=""
  fi

  "$JAVACMD" \
    $MVNW_AUTH \
    -classpath "${0%/*}/.mvn/wrapper/maven-wrapper.jar" \
    "-Dmaven.multiModuleProjectDirectory=${0%/*}" \
    org.apache.maven.wrapper.MavenWrapperMain \
    "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl" || die "failed to download with maven-wrapper.jar"

# Use wget or curl
elif set_java_home; then
  verbose "Falling back to wget/curl ..."
  if command -v wget > /dev/null; then
    verbose "Found wget ... using wget"
    wget ${MVNW_VERBOSE:+"--verbose"} --auth-no-challenge \
      ${MVNW_USERNAME:+"--user=$MVNW_USERNAME"} ${MVNW_PASSWORD:+"--password=$MVNW_PASSWORD"} \
      -O "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl" || die "wget: Failed to fetch $distributionUrl"
  elif command -v curl > /dev/null; then
    verbose "Found curl ... using curl"
    curl ${MVNW_VERBOSE:+"--verbose"} --fail --location \
      ${MVNW_USERNAME:+"--user $MVNW_USERNAME:$MVNW_PASSWORD"} \
      -o "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl" || die "curl: Failed to fetch $distributionUrl"
  else
    die "wget and curl not available, cannot download"
  fi
else
  die "cannot download, JAVA_HOME not set and java/javac not in PATH"
fi

# If specified, validate the SHA-256 sum of the Maven distribution zip file
if [ -n "${distributionSha256Sum-}" ]; then
  distributionSha256Result=false
  if [ "$distributionSha256Sum" = "$(sha256sum "$TMP_DOWNLOAD_DIR/$distributionUrlName" | cut -d ' ' -f 1)" ]; then
    distributionSha256Result=true
  elif [ "$distributionSha256Sum" = "$(shasum -a 256 "$TMP_DOWNLOAD_DIR/$distributionUrlName" | cut -d ' ' -f 1)" ]; then
    distributionSha256Result=true
  fi
  if [ $distributionSha256Result = false ]; then
    die "Failed to validate Maven distribution SHA-256, your Maven distribution might be compromised."
  fi
fi

# unzip and move
if command -v unzip > /dev/null; then
  unzip ${MVNW_VERBOSE:+"-v"} "$TMP_DOWNLOAD_DIR/$distributionUrlName" -d "$TMP_DOWNLOAD_DIR" || die "failed to unzip"
else
  tar xzf${MVNW_VERBOSE:+"v"} "$TMP_DOWNLOAD_DIR/$distributionUrlName" -C "$TMP_DOWNLOAD_DIR" || die "failed to untar"
fi
printf %s\\n "$distributionUrl" >"$TMP_DOWNLOAD_DIR/$distributionUrlNameMain/mvnw.url"
mv -- "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" "$MAVEN_HOME" || [ -d "$MAVEN_HOME" ] || die "fail to move MAVEN_HOME"

clean || :
exec_maven "$@"
