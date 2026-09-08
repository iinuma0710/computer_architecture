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

    // 命令の種類ごとにビットパターンを ListLookup オブジェクトで列挙
    val csignals = ListLookup(
        inst,
        List(ALU_X, OP1_RS1, OP2_RS2, MEM_X, REN_X, WB_X),
        Array(
            LW   -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_MEM),
            SW   -> List(ALU_ADD, OP1_RS1, OP2_IMS, MEM_S, REN_X, WB_X  ),
            ADD  -> List(ALU_ADD, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            ADDI -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            SUB  -> List(ALU_SUB, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            AND  -> List(ALU_AND, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            OR   -> List(ALU_OR,  OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            XOR  -> List(ALU_XOR, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            ANDI -> List(ALU_AND, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            ORI  -> List(ALU_OR,  OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            XORI -> List(ALU_XOR, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            SLL  -> List(ALU_SLL, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            SRL  -> List(ALU_SRL, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            SRA  -> List(ALU_SRA, OP1_RS1, OP2_RS2, MEM_X, REN_S, WB_ALU),
            SLLI -> List(ALU_SLL, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            SRLI -> List(ALU_SRL, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
            SRAI -> List(ALU_SRA, OP1_RS1, OP2_IMI, MEM_X, REN_S, WB_ALU),
        )
    )
    val exe_fun :: op1_sel :: op2_sel :: mem_wen ::rf_wen :: wb_sel :: Nil = csignals

    val op1_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op1_sel == OP1_RS1) -> rs1_data
    ))

    val op2_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op2_sel == OP2_RS2) -> rs2_data,
        (op2_sel == OP2_IMI) -> imm_i_sext,
        (op2_sel == OP2_IMS) -> imm_s_sext,
    ))

    //********************************************
    // 実行 (EX) ステージ 
    //********************************************

    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (exe_fun == ALU_ADD) -> (op1_data + op2_data),
        (exe_fun == ALU_SUB) -> (op1_data - op2_data),
        (exe_fun == ALU_AND) -> (op1_data & op2_data),
        (exe_fun == ALU_OR)  -> (op1_data | op2_data),
        (exe_fun == ALU_XOR) -> (op1_data ^ op2_data),
        (exe_fun == ALU_SLL) -> (op1_data << op2_data(4, 0))(31, 0),
        (exe_fun == ALU_SRL) -> (op1_data >> op2_data(4, 0)).asUInt(),
        (exe_fun == ALU_SRA) -> (op1_data.asSInt() >> op2_data(4, 0)).asUInt(),
    ))  
    
    //********************************************
    // メモリアクセス (MEM) ステージ 
    //********************************************

    // io.dmem を通してメモリにアドレスを渡す
    io.dmem.addr := alu_out

    // 書き込み可否信号と書き込むデータをメモリのポートに渡す
    io.dmem.wen   := mem_wen
    io.dmem.wdata := rs2_data

    //********************************************
    // ライトバック (WB) ステージ 
    //********************************************

    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> io.dmem.rdata
    ))
    
    when(rf_wen === REN_S) {
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