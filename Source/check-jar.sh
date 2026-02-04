#! /bin/bash
ONLY_FIRST_FILE=true
MAX_MAJOR=0
WORK_DIR=$(mktemp -d)
#WORK_DIR="/tmp/tmp.nbCuo3Tzv2"

# check if tmp dir was created
if [[ ! "$WORK_DIR" || ! -d "$WORK_DIR" ]]; then
  echo "Could not create temp dir"
  exit 1
fi

# deletes the temp directory
function cleanup {
  rm -rf "$WORK_DIR"
  #echo "Deleted temp working directory $WORK_DIR"
}

# check one class file
function check {
  found=false
  for file in *.class;
  do
  	[ -f "$file" ] || continue
    msg=$(javap -v $file | grep major)
    ver=${msg:17:2}
    if [ "$ver" -gt "$MAX_MAJOR" ]; then
      echo "$file $ver"
      MAX_MAJOR="$ver"
    fi
    found=true
    break
  done
  if [ $ONLY_FIRST_FILE = true ] && [ $found = false ];
  then
    for d in *;
    do
      if [ -d "$d" ]; then
        pushd $d > /dev/null
        pwd=$(pwd)
        classname=${pwd#"$WORK_DIR"/}
        echo -en "\r\e[K$classname"
        check
        popd > /dev/null
      fi
    done
  fi
}

# register the cleanup function to be called on the EXIT signal
trap cleanup EXIT

filename=$(basename "$1")
cp $1 $WORK_DIR
pushd $WORK_DIR > /dev/null
unzip -n -q $filename "*.class"
for file in *.class;
do
  [ -f "$file" ] || continue
  msg=$(javap -v $file | grep major)
  ver=${msg:17:2}
  if [ "$ver" -gt "$MAX_MAJOR" ]; then
    echo "$file $ver"
    MAX_MAJOR="$ver"
  fi
  break
done
for d in *;
do
  if [ -d "$d" ]; then
    pushd $d > /dev/null
    check
    popd > /dev/null
  fi
done
echo -e "\r\e[K"
popd > /dev/null
case $MAX_MAJOR in
  46)
    echo "Java 1.2"
    ;;
  47)
    echo "Java 1.3"
    ;;
  48)
    echo "Java 1.4"
    ;;
  49)
    echo "Java 1.5"
    ;;
  50)
    echo "Java 6"
    ;;
  51)
    echo "Java 7"
    ;;
  52)
    echo "Java 8"
    ;;
  53)
    echo "Java 9"
    ;;
  54)
    echo "Java 10"
    ;;
  55)
    echo "Java 11"
    ;;
  56)
    echo "Java 12"
    ;;
  57)
    echo "Java 13"
    ;;
  58)
    echo "Java 14"
    ;;
  59)
    echo "Java 15"
    ;;
  60)
    echo "Java 16"
    ;;
  61)
    echo "Java 17"
    ;;
  62)
    echo "Java 18"
    ;;
  63)
    echo "Java 19"
    ;;
  64)
    echo "Java 20"
    ;;
  65)
    echo "Java 21 (LTS)"
    ;;
  66)
    echo "Java 22"
    ;;
  67)
    echo "Java 23"
    ;;
  *)
    echo "Other Java version"
    ;;
esac

