package fetch

import java.io.File
import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

import common.Consts._


class ImemPortIo extends Bundle {
    val addr = Input(UInt(WORD_LEN.W))
    val inst = Output(UInt(WORD_LEN.W))
}


class Memory extends Module {
    val io = IO(new Bundle {
        val imem = new ImemPortIo()
    })

    // 16KiB のメモリを生成
    val mem = Mem(16384, UInt(8.W))

    // データをファイルからメモリにロード
    val relativePath = "src/hex/fetch.hex"
    val absolutePath = new File(relativePath).getAbsolutePath
    loadMemoryFromFile(mem, absolutePath)

    // 各アドレスの 8bit データをつなげて 32bit に整形
    io.imem.inst := Cat(
        mem(io.imem.addr + 3.U(WORD_LEN.W)),
        mem(io.imem.addr + 2.U(WORD_LEN.W)),
        mem(io.imem.addr + 1.U(WORD_LEN.W)),
        mem(io.imem.addr)
    )
}