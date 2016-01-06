#server client interpreter server64
set terminal pngcairo size 1200,800
set log x
set log y
set xlabel "Loop count"
set ylabel "Execution time (ns)"

plot "warmup.data" using 1:4 title "Interpreted", "" using 1:3 title "C1 Compiler", "" using 1:2 title "C2 Compiler", "" using 1:5 title "C2 Compiler 64-bit JDK"

