module halfadder(
    input  logic a, b,
    output logic s, c
);
    always_comb begin
       s = a ^ b;
       c = a & b;
    end
endmodule