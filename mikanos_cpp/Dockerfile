FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y software-properties-common locales && locale-gen en_US.UTF-8 && \
    apt-get update && apt-get install -y okteta kde-plasma-desktop dbus-x11 --no-install-recommends && \
    apt-get install -y clang lldb lld llvm && \
    apt-get install -y qemu-system-x86 qemu-user qemu-user-static qemu-utils build-essential git wget curl \
    unzip ca-certificates libglib2.0-dev libfdt-dev libpixman-1-dev zlib1g-dev dosfstools && \
    apt-get clean && rm -rf /var/lib/apt/lists/*