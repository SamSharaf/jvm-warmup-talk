set terminal pngcairo size 1200,800
set log y
set xlabel "Iterations"
set ylabel "Duration (nanoseconds)"
set datafile separator ','
plot "loop_time_numbers.csv" using 1:2 title "Standard", "" using 1:3 title "Compiler Hints"

