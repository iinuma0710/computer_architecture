package rv32cpu

import chisel3._
import chisel3.util._
import common.Consts._
import common.RV32I._


class Core extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPortIo())
        val dmem = Flipped(new DmemPortIo())
        val exit = Output(Bool())
    })

    val regfile = Mem(32, UInt(WORD_LEN.W))

    //********************************************
    // 命令フェッチ (IF) ステージ 
    //********************************************

    // 初期値 START_ADDR でプログラムカウンタを生成する
    val pc_reg = RegInit(START_ADDR)
    // 1サイクルで4Byteずつカウントアップ
    pc_reg := pc_reg + 4.U(WORD_LEN.W)

    // 出力ポート addr に pc_reg を接続
    io.imem.addr := pc_reg
    // 入力ポート inst の値を、inst で受ける
    val inst = io.imem.inst

    //********************************************
    // 命令デコード (ID) ステージ 
    //********************************************

    // レジスタアドレスの取得
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    val wb_addr  = inst(11, 7)

    // 常に0番レジスタでゼロを保持するためにマルチプレクサを通してデータを取得
    val rs1_data = Mux((rs1_addr =/= 0.U(WORD_LEN.W)), regfile(rs1_addr), 0.U(WORD_LEN.W))
    val rs2_data = Mux((rs2_addr =/= 0.U(WORD_LEN.W)), regfile(rs2_addr), 0.U(WORD_LEN.W))

    // 即値を符号拡張で32ビットに変換
    val imm_i = inst(31, 20)
    val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i)
    val imm_s = Cat(inst(31, 25), inst(11, 7))
    val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s)

    //********************************************
    // 実行 (EX) ステージ 
    //********************************************
    
    // MuxCase で ALU を実装
    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (inst === LW || inst === ADDI) -> (rs1_data + imm_i_sext),  // LW, ADDI
        (inst === SW)                  -> (rs1_data + imm_s_sext),  // SW
        (inst === ADD)                 -> (rs1_data + rs2_data),    // ADD
        (inst === SUB)                 -> (rs1_data - rs2_data)     // SUB
        (inst === AND)                 -> (rs1_data & rs2_data)     // AND
        (inst === OR)                  -> (rs1_data | rs2_data)     // OR
        (inst === XOR)                 -> (rs1_data ^ rs2_data)     // XOR
        (inst === ANDI)                -> (rs1_data & imm_i_sext)   // ANDI
        (inst === ORI)                 -> (rs1_data | imm_i_sext)   // ORI
        (inst === XORI)                -> (rs1_data ^ imm_i_sext)   // XORI
    ))

    //********************************************
    // メモリアクセス (MEM) ステージ 
    //********************************************

    // io.dmem を通してメモリにアドレスを渡す
    io.dmem.addr := alu_out

    // 書き込み可否信号と書き込むデータをメモリのポートに渡す
    io.dmem.wen   := (inst === SW)
    io.dmem.wdata := rs2_data

    //********************************************
    // ライトバック (WB) ステージ 
    //********************************************

    val wb_data = MuxCase(alu_out, Seq(
        (inst === LW) -> io.dmem.rdata
    ))
    when(
        inst === LW || inst === ADD || inst === ADDI || inst === SUB ||
        inst === AND || inst === OR || inst === XOR || inst === ANDI ||
        inst === ORI || inst ===XORI
    ) {
        regfile(wb_addr) := wb_data
    }

    // デバッグ用の出力
    printf(p"pc_reg    : 0x${Hexadecimal(pc_reg)}\n")
    printf(p"inst      : 0x${Hexadecimal(inst)}\n")
    printf(p"rs1_addr  : $rs1_addr\n")
    printf(p"rs2_addr  : $rs2_addr\n")
    printf(p"wb_addr   : $wb_addr\n")
    printf(p"rs1_data  : 0x${Hexadecimal(rs1_data)}\n")
    printf(p"rs2_data  : 0x${Hexadecimal(rs2_data)}\n")
    printf(p"wb_data   : 0x${Hexadecimal(wb_data)}\n")
    printf(p"dmem.addr : ${io.dmem.addr}\n")
    printf(p"dmem.wen  : ${io.dmem.wen}\n")
    printf(p"dmem.wdata: 0x${Hexadecimal(io.dmem.wdata)}\n")
    printf("----------\n")

    // プログラムを終了判定
    io.exit := (inst === 0x00602823.U(WORD_LEN.W))
}