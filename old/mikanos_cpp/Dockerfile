FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# 必要なライブラリと、QEMU やバイナリエディタのインストール
RUN apt-get update && \
    # ロケールの設定
    apt-get install -y software-properties-common locales && locale-gen en_US.UTF-8 && \
    apt-get install -y --no-install-recommends\
    build-essential \
    git \
    dosfstools \
    nasm \
    acpica-tools \
    uuid-dev \
    xauth \
    unzip \
    dbus-x11 \
    # バイナリエディタ
    okteta \
    # QEMU
    qemu-system-x86 \
    qemu-utils \
    qemu-user \
    qemu-user-static \
    qemu-system-gui \
    # ビルドに必要なツール
    llvm \
    lld \
    lldb \
    clang \
    libc6-dev-amd64-cross \
    binutils-x86-64-linux-gnu && \
    apt-get clean -y && rm -rf /var/lib/apt/lists/*

# EDK II のインストール
WORKDIR /home/ubuntu

RUN git clone https://github.com/tianocore/edk2.git && \
    cd edk2 && \
    git submodule update --init --recursive && \
    make -C BaseTools/Source/C