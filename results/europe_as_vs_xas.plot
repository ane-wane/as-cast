set terminal pdf enhanced color font "Helvetica, 14"

set output "europe_as_vs_xas.pdf"

set pointsize 0.6


set multiplot layout 2,1

set lmargin at screen 0.13
set bmargin 0.75

set yrange [0:3*1174/1000]
set format y "%.1fk"
set xrange[0:7]
set xtics format ""

set grid xtics

set ylabel "number of messages\nper operation" font "Helvetica, 13"
# set xlabel "time (s)"

set key right center font "Helvetica, 13"

START = 1000
LISBON = (0 + START)/1000
ANDORRA = (1000 + START)/1000
PARIS = (2000 + START)/1000
REYKJAVIK = (3000 + START)/1000
ANDORRA_DEL = (4000 + START)/1000

HEAD = 0.003

# set logscale y 10
# set yrange [0.01:10]

HEIGHT = -0.1

set label 11 at 1.5, 2.65 center "updated\n 1157 nodes" tc rgb "web-blue" font "Helvetica, 11"
set label 10 at 3.5, 2.55 center "updated\n1102 nodes" tc rgb "web-blue" font "Helvetica, 11"

set label 1 at LISBON, HEIGHT center "Portugal" font "Helvetica, 11"
set label 2 at ANDORRA, HEIGHT center "Spain" font "Helvetica, 11"
set label 3 at PARIS, HEIGHT center "France" font "Helvetica, 11"
set label 4 at REYKJAVIK, HEIGHT center "Iceland" font "Helvetica, 11"
set label 5 at ANDORRA_DEL, HEIGHT center "Spain" font "Helvetica, 11" tc rgb "light-red"

set arrow from ANDORRA_DEL - 0.3, HEIGHT to ANDORRA_DEL + 0.3 , HEIGHT nohead lc rgb "light-red"

f(x) = (x <= 2) ? 0. : (x <= 3) ? 2.5324 : (x <= 4) ? 2.5575 : (x <= 5) ? 4.9853 : (x <= 6) ? 5.0156 : 5.0605+0.0182

g(x) = (x <= 2) ? 0. : (x <= 3) ? 0.0432+0.0017 : (x <= 4) ? 0.0985+0.0415 : (x <= 5) ? 0.1850+0.0467 : (x <= 6) ? 0.2109+0.0484 : 0.2109+0.0432+0.0501

plot "as_cast_europe_traffic.dat" every 50 u ($2)/1000:($5+$8-f($2/1000))*1157/1000 w linespoints t "ascast" lt rgb "web-blue", \
     "xas_cast_europe_traffic.dat" every 100 u ($2)/1000:($5+$8+$14-g($2/1000))*1157/1000 w linespoints t "xascast" lt rgb "purple";

# plot "xas_cast_europe_traffic_per_sec.dat" every 25 u ($1)/1000:($2) w linespoints t "avg {/Symbol a} per node" lt rgb "forest-green", \
#      "xas_cast_europe_traffic_per_sec.dat" every 25 u ($1)/1000:($3) w linespoints t "avg {/Symbol d} per node" lt rgb "orange", \
#      "as_cast_europe_traffic_per_sec.dat" every 25 u ($1)/1000:($2) w linespoints t "avg {/Symbol a} per node" lt rgb "forest-green", \
#      "as_cast_europe_traffic_per_sec.dat" every 25 u ($1)/1000:($3) w linespoints t "avg {/Symbol d} per node" lt rgb "orange";


# ## set ylabel "time (ms)"
set xlabel "time (s)"
set xtics format

set tmargin 0.0
set bmargin 2.9

set yrange [0:105]
unset format y 
set ylabel "number of nodes\nindexing" 

unset label 1
unset label 2
unset label 3
unset label 4
unset label 5
unset label 10
unset label 11

set label at 1, 90 offset 0, 0.5  center "+1157" font "Helvetica, 11" tc rgb "web-blue"
set label at 1, 23 offset 0, 0.5  center "+23" font "Helvetica, 11" tc rgb "purple"
set label at 2, 43 offset 0, 0.5  center "+20" font "Helvetica, 11" tc rgb "purple"
set label at 3, 82 offset 0, 0.5  center "+39" font "Helvetica, 11" tc rgb "purple"
set label at 4, 96 offset 0, 0.5  center "+14" font "Helvetica, 11" tc rgb "purple"
set label at 5, 96-20 offset 0, -0.5  center "-20" font "Helvetica, 11" tc rgb "purple"

set label at 2.25, 11 offset 0, 0.6 center "+11" font "Helvetica, 11" tc rgb "purple"
set label at 5.25, 0 offset 0, 0.6 center "-11" font "Helvetica, 11" tc rgb "purple"


plot "as_cast_europe_local.dat" every 100 u ($2)/1000:($8*1157/100) w linespoints t "ascast indexes" lt rgb "web-blue", \
     "xas_cast_europe_local.dat" every 100 u ($2)/1000:($8*1157/100) w linespoints t "xascast local indexes" lt rgb "purple", \
     "xas_cast_europe_local.dat" every 100 u ($2)/1000:($11*1157/100) w linespoints t "xascast global indexes" lt rgb "purple";