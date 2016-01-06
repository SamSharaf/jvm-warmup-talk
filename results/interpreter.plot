set terminal pngcairo size 1200,800
set log x
set log y
set xlabel "Loop count"
set ylabel "Execution time (ns)"

plot "warmup.data" using 1:4 title "Interpreted code execution time"

