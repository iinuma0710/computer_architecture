`include "fulladder.sv"

module adder
    #(parameter nbit = 32;)
    (
        input  logic [(nbit - 1):0] a, b,
        output logic [(nbit - 1):0] s,
        output logic cout
    );
    // 各全加算機の出力信号
    logic [(nbit - 1):0] fa_s;
    logic [(nbit - 1):0] fa_cout;

    // nbit 個の加算器を接続
    generate
        for (genvar i; i < nbit; i++) begin : genAdder
            
        end
    endgenerate

endmodule