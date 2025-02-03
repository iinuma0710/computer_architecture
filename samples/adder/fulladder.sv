`include "halfadder.sv"

module fulladder(
    input  logic a, b, cin,
    output logic s, cout
);
    // 内部信号の宣言
    logic s0, c0, c1;
    // 1段目の半加算器のインスタンスを生成
    halfadder ha0(.a(a), .b(b), .s(s0), .c(c0));
    // 2段目の半加算器のインスタンスを生成
    halfadder ha1(.a(s0), .b(cin), .s(s), .c(c1));

    always_comb begin
        cout = c0 | c1;
    end
endmodule