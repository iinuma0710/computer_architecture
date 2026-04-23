FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive

# ビルド設定
ARG RISCV_PREFIX=/opt/riscv
ARG RISCV_ARCH=rv32ima
ARG RISCV_ABI=ilp32
ARG MAKEFLAGS=-j8

# 各パッケージのバージョン指定
ARG IVERILOG_VERSION=v13_0
ARG SBT_VERSION=1.12.9
ARG RISCV_GNU_COMPILER_VERSION=2026.04.05
ARG RISCV_TESTS_COMMIT=933a897d8631773f385d45938facc466dddc7514
ARG VERILATOR_VERSION=v5.046
ARG SPIKE_COMMIT=770ce31f7543f57472b35e66600085ea81184bb2

ENV TZ=Etc/UTC
ENV RISCV=${RISCV_PREFIX}
ENV PATH=${RISCV_PREFIX}/bin:/usr/local/bin:/usr/share/sbt/bin:${PATH}

WORKDIR ${RISCV}

# 基本ツールのインストール
RUN apt update && \
	apt install -y autoconf automake autotools-dev cmake curl openjdk-17-jdk libmpc-dev libmpfr-dev libgmp-dev gawk libglib2.0-dev libslirp-dev libncurses-dev \
	               build-essential bison flex texinfo gperf libtool patchutils bc zlib1g-dev libexpat-dev ninja-build pkg-config gcc g++ \
	               git libusb-1.0-0-dev device-tree-compiler default-jdk gnupg vim help2man python3 python3-pip python3-tomli gtkwave && \
	rm -rf /var/lib/apt/lists/*

# Icarus Verilog のインストール
RUN git clone https://github.com/steveicarus/iverilog.git && \
    cd iverilog && \
    git checkout ${IVERILOG_VERSION} && \
    sh autoconf.sh && \
    ./configure && \
    make ${MAKEFLAGS} && \
    make install && \
    cd ${RISCV} && rm -rf iverilog

# sbt のインストール
RUN curl -L https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz -o /tmp/sbt.tgz && \
    tar -xzf /tmp/sbt.tgz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt && \
    rm /tmp/sbt.tgz

# riscv-gnu-toolchain のビルド
RUN git clone https://github.com/riscv/riscv-gnu-toolchain && \
    cd riscv-gnu-toolchain && \
    git checkout ${RISCV_GNU_COMPILER_VERSION} && \
    git config -f .gitmodules submodule.dejagnu.url https://gnu.googlesource.com/dejagnu && \
    git submodule sync dejagnu && \
    git submodule update --init --recursive && \
    unset CC CXX CPP CFLAGS CXXFLAGS CPPFLAGS LDFLAGS LD_LIBRARY_PATH && \
    ./configure --prefix=${RISCV_PREFIX} --with-arch=${RISCV_ARCH} --with-abi=${RISCV_ABI} && \
    make ${MAKEFLAGS} && \
    cd ${RISCV} && rm -rf riscv-gnu-toolchain

# riscv-tests のダウンロード
RUN git clone https://github.com/riscv/riscv-tests && \
	cd riscv-tests && \
    git checkout ${RISCV_TESTS_COMMIT} && \
	git submodule update --init --recursive && \
	cd ${RISCV}

# Verilator のインストール
RUN git clone https://github.com/verilator/verilator && \
    cd verilator && \
	git checkout ${VERILATOR_VERSION} && \
    autoconf && \
    ./configure --prefix=${RISCV_PREFIX} && \
    make ${MAKEFLAGS} && \
    make install && \
	cd ${RISCV} && rm -rf verilator

## Spike のインストール
RUN git clone https://github.com/riscv-software-src/riscv-isa-sim.git && \
    cd riscv-isa-sim && \
    git checkout ${SPIKE_COMMIT} && \
    mkdir build && cd build && \
    ../configure --prefix=${RISCV_DIR} && \
    make ${MAKEFLAGS} && \
    make install && \
    cd ${RISCV} && rm -rf riscv-isa-sim

# 起動時の挙動
WORKDIR /work/riscv
CMD ["/bin/bash"]