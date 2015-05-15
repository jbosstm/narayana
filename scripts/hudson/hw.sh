
if [ -x /usr/sbin/system_profiler ]; then
  sw_vers
  /usr/sbin/system_profiler
else
  set -o xtrace

  uname -a
  cat /etc/redhat-release
  java -version
  free -m
  cat /proc/cpuinfo
  cat /proc/meminfo
  cat /proc/devices
  cat /proc/scsi/scsi
  cat /proc/partitions
#sudo dmidecode --type 17

  lspci
  lsusb
  lsblk
  df
  mount | column -t | grep ext
fi
