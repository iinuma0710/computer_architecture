package rv32cpu

import java.io.File
import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

import common.Consts._


// 命令用の IO ポート
class ImemPortIo extends Bundle {
    val addr = Input(UInt(WORD_LEN.W))
    val inst = Output(UInt(WORD_LEN.W))
}


// データ用の IO ポート
class DmemPortIo extends Bundle {
    val addr  = Input(UInt(WORD_LEN.W))
    val rdata = Output(UInt(WORD_LEN.W))
    val wen   = Input(Bool())
    val wdata = Input(UInt(WORD_LEN.W))
}


// メモリ本体
class Memory extends Module {
    val io = IO(new Bundle {
        val imem = new ImemPortIo()
        val dmem = new DmemPortIo()
    })

    // 16KiB のメモリを生成
    val mem = Mem(16384, UInt(8.W))

    // データをファイルからメモリにロード
    val relativePath = "src/hex/sw.hex"
    val absolutePath = new File(relativePath).getAbsolutePath
    loadMemoryFromFile(mem, absolutePath)

    // 命令の読み込み：各アドレスの 8bit データをつなげて 32bit に整形
    io.imem.inst := Cat(
        mem(io.imem.addr + 3.U(WORD_LEN.W)),
        mem(io.imem.addr + 2.U(WORD_LEN.W)),
        mem(io.imem.addr + 1.U(WORD_LEN.W)),
        mem(io.imem.addr)
    )

    // データの読み込み：各アドレスの 8bit データをつなげて 32bit に整形
    io.dmem.rdata := Cat(
        mem(io.dmem.addr + 3.U(WORD_LEN.W)),
        mem(io.dmem.addr + 2.U(WORD_LEN.W)),
        mem(io.dmem.addr + 1.U(WORD_LEN.W)),
        mem(io.dmem.addr)
    )

    // データの書き込み：wdata ポートに渡されたデータを 8bit ごとに分割して格納
    when(io.dmem.wen) {
        mem(io.dmem.addr)       := io.dmem.wdata( 7,  0)
        mem(io.dmem.addr + 1.U) := io.dmem.wdata(15,  8)
        mem(io.dmem.addr + 2.U) := io.dmem.wdata(23, 16)
        mem(io.dmem.addr + 3.U) := io.dmem.wdata(31, 24)
    }
}