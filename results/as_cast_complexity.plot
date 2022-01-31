set terminal pdf enhanced color font "Helvetica, 14"

set output "as_cast_complexity.pdf"

set multiplot layout 2,1

set lmargin at screen 0.13

set bmargin 0.75

set xrange [0:60]
set xtics format ""

set grid xtics

set ylabel "messages per second"

set key top left font "Helvetica, 13" 

set arrow 1 from 21.254, 0 to 21.254, 12 nohead dt 2 lw 2



plot "as_cast_messages_per_second.dat" every 500 u ($1)/1000:($2) w linespoints t "avg {/Symbol a} per node" lt rgb "forest-green", \
     "as_cast_messages_per_second.dat" every 500 u ($1)/1000:($3) w linespoints t "avg {/Symbol d} per node" lt rgb "orange";

set ylabel "time (ms)"
set xlabel "time (s)"
set xtics format

set yrange [0:450]

set tmargin 0.0
set bmargin 2.9

set arrow 1 from 21.254 to 21.254, 450
set label 1 at 21.254, 473 "start deleting partitions" font "Helvetica, 11" center
# set label 1 at 21.254, 370

set key bottom right

set pointsize 0.4

plot "as_cast_duration_2.dat" u ($1)/1000:($2) w linespoints t "consistent partitioning reached" lt rgb "web-blue";