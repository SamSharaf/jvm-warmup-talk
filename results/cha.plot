set terminal pngcairo size 1000,600
set log y
set xlabel "Iterations"
set ylabel "Duration (nanoseconds)"
set title "Effect of CHA uncommon trap"

plot "cha.data" using 1:2 with lines title "Time taken to complete 1000 iterations"
#pause -1
