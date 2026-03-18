#!/bin/sh -ex

EFI_FILE=$1
DEVENV_DIR=$HOME/computer_architecture/mikanos/devenv
MOUNT_POINT=$DEVENV_DIR/mnt
DISK_IMG=$DEVENV_DIR/disk.img

if [ ! -f $EFI_FILE ]
then
    echo "No such file: $EFI_FILE"
    exit 1
fi

# ディスクイメージの作成
qemu-img create -f raw $DISK_IMG 200M
mkfs.fat -n 'MIKAN OS' -s 2 -f 2 -R 32 -F 32 $DISK_IMG
mkdir -p $MOUNT_POINT
sudo mount -o loop $DISK_IMG $MOUNT_POINT
sudo mkdir -p $MOUNT_POINT/EFI/BOOT
sudo cp $EFI_FILE $MOUNT_POINT/EFI/BOOT/BOOTX64.EFI
sudo umount $MOUNT_POINT
rm -rf $MOUNT_POINT

# QEMU で実行
qemu-system-x86_64 \
    -drive if=pflash,file=$DEVENV_DIR/OVMF_CODE.fd \
    -drive if=pflash,file=$DEVENV_DIR/OVMF_VARS.fd \
    -hda $DISK_IMG