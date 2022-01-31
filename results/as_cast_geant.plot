set terminal pdf enhanced color font "Helvetica, 14"

set output "as_cast_geant.pdf"

set multiplot layout 2,1

INFINITY = 10

TADD = 0.05
TDELEDGE = 0.85
TADDEDGE = 1.7

set lmargin at screen 0.10
set bmargin 0.75

INFINITY = 5

set xrange [0:2.5]
set yrange [0:INFINITY]
set xtics format ""

# set grid xtics

set ylabel "cumulative\nnumber of messages"

unset key

HEIGHT = -0.15

set arrow from TADD, 0 to TADD, 4
set label 1 at TADD, 4.35 "Add 1 source" font "Helvetica Bold, 11"

set arrow from TDELEDGE, 0 to TDELEDGE, INFINITY nohead dt 2 lw 2

set arrow 3 from TADDEDGE, 0 to TADDEDGE, INFINITY nohead dt 2 lw 2


set arrow 4 from TADDEDGE, 3.5 to TADDEDGE+0.2, 3.5 
set arrow 5 from TADDEDGE+0.2, 3.5 to TADDEDGE, 3.5
set label 4 at TADDEDGE+0.1, 3.8 "latency" font "Helvetica, 12" center
set label 5 at TADDEDGE+0.1, 3.2 "200 ms" font "Helvetica, 10" center


a(x) = (x > 0.051 && x < 0.766) ? 1000: 0
b(x) = (x > 0.859 && x < 1.227) ? 1000: 0
c(x) = (x > 1.700 && x < 2.277) ? 1000: 0

# a(x) = x < 0.05 ? 1000:0
# b(x) = (x > 0.766 && x < TDELEDGE- 0.01)? 1000: 0
# c(x) = (x > 1.227 && x < TADDEDGE - 0.01)? 1000: 0
# d(x) = (x > 2.277) ? 1000: 0

set style fill transparent solid 0.25

plot "as_cast_geant_traffic.dat" every 20 u ($2)/1000:($5) w linespoints t "avg {/Symbol a}/node" lt rgb "forest-green", \
     "as_cast_geant_traffic.dat" every 20 u ($2)/1000:($8) w linespoints t "avg {/Symbol d}/node" lt rgb "orange", \
     a(x) w filledcurves below x1 lc rgb "gray", \
     b(x) w filledcurves below x1 lc rgb "gray", \
     c(x) w filledcurves below x1 lc rgb "gray";


## set ylabel "time (ms)"
set xlabel "time (s)"
set xtics format

set tmargin 0.0
set bmargin 2.9


unset label 2
unset label 3
unset arrow 4
unset arrow 5
unset label 4
unset label 5

set key top left font "Helvetica, 13"
set key bottom right

set label 1 "Add 1 source per cluster"

f(x) = (x > 0.051 && x < 0.411)? 1000 : 0

set label 2 at TDELEDGE, 5.25 "remove link between clusters" font "Helvetica, 11" center
set label 3 at TADDEDGE, 5.25 "add link between clusters" font "Helvetica, 11" center

plot "as_cast_geant_2_traffic.dat" every 20 u ($2)/1000:($5) w linespoints t "avg {/Symbol a} per node" lt rgb "forest-green", \
     "as_cast_geant_2_traffic.dat" every 20 u ($2)/1000:($8) w linespoints t "avg {/Symbol d} per node" lt rgb "orange", \
     f(x) w filledcurves below x1 lc rgb "gray" t "converging toward consistent partitioning";
	  
	  