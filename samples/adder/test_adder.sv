`include "adder.sv"

module test_halfadder();
    logic [1:0] a, b;
    logic [1:0] s;
    logic       cout;

    // 2ビット加算器のインスタンスを作成
    adder #(.nbit(2)) _adder (.a(a), .b(b), .s(s), .cout(cout));

    initial begin
        $display("a[1:0] b[1:0] || cout s[1:0]");
        $display("--------------++------------");

        // テストパターンを試す
        a = 2'b00; b = 2'b00; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b00; b = 2'b01; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b00; b = 2'b10; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b00; b = 2'b11; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b01; b = 2'b00; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b01; b = 2'b01; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b01; b = 2'b10; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b01; b = 2'b11; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b10; b = 2'b00; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b10; b = 2'b01; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b10; b = 2'b10; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b10; b = 2'b11; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b11; b = 2'b00; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b11; b = 2'b01; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b11; b = 2'b10; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
        a = 2'b11; b = 2'b11; #10; $display("  %b     %b   ||  %b     %b", a, b, cout, s);
    end
endmodule