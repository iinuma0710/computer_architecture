`include "fulladder.sv"

module suber
    #(parameter nbit = 32)
    (
        input  logic [(nbit - 1):0] a, b,
        output logic [(nbit - 1):0] s
    );
    // 各全加算機の出力信号
    logic [(nbit - 1):0] fa_cout;

    // nbit 個の加算器を接続
    genvar i;
    generate
        for (i = 0; i < nbit; i++) begin : genAdder
            fulladder fa(
                .a(a[i]), .b(~b[i]), .cin(i==0?1'b1:fa_cout[i-1]), 
                .s(s[i]), .cout(fa_cout[i])
            );
        end
    endgenerate
endmodule