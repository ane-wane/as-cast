from pathlib import Path
from pygnuplot import gnuplot



TRACE_FILE = Path('peersim_ack_cost_speed_results.dat');

results = {}

with TRACE_FILE.open('r') as f:
    for line in f.readlines():
        values = line.split()                
        if "CTraffic" in line:
            results[values[1]] = []
            results[values[1]].append(float(values[4])-float(values[7])-float(values[10]))
            results[values[1]].append(values[7])
            results[values[1]].append(values[10])

with TRACE_FILE.open('r') as f:
    for line in f.readlines():
        values = line.split()
        if "CMiss" in line:
            results[values[1]].append(values[4])
            results[values[1]].append(values[7])
            results[values[1]].append(values[10])

with Path(__file__+'.dat').open('w') as w:
    for key, value in results.items():
        w.write("{} {} {} {} {} {} {}\n".format(key,
                                       value[0], value[1], value[2],
                                       value[3], value[4], value[5]))
    


g = gnuplot.Gnuplot(log = True,
                    output = f'"{__file__}.eps"',
                    term = 'postscript eps color blacktext "Helvetica" 20',
                    multiplot = 'layout 2, 1 spacing 0.5,0.5')

g.cmd('set ylabel "avg msg per process"',
      'set format y "%.1f"')
# g.c('set yrange [0:1]')

g.cmd('set key left top')

g.cmd('set xrange [0:100]',
      'set xtics format ""',
      'set grid xtics')

#g.cmd('set bmargin 0.75')

# g.cmd('set arrow 1 from 550,0 to 550,5.5 nohead dt "." lc "black"',
#       'set arrow 2 from 650,0 to 650,5.5 nohead dt "." lc "black"')

g.plot(f'"{__file__}.dat" u ($1):($2) t "ack" w linespoints lt rgb "orange", \
"{__file__}.dat" u ($1):($3) t "add" w linespoints lt rgb "blue", \
"{__file__}.dat" u ($1):($4) t "del" w linespoints lt rgb "red"')

g.cmd('set tmargin 0.0',
      'set bmargin')

g.cmd('set xtics format',
      'set xlabel "time"')

g.cmd('set ylabel "nb processes"')

g.cmd('set key right top')

# g.cmd('set arrow 1 from 550,0 to 550,1',
#       'set arrow 2 from 650,0 to 650,1')
# g.cmd('set arrow from 550,0.35 to 650,0.35 heads',
#       'set label at 600,0.42 center "-1 service"')


g.plot(f'"{__file__}.dat" u ($1):($5) t "no" w linespoints, \
"{__file__}.dat" u ($1):($6) t "wrong" w linespoints, \
"{__file__}.dat" u ($1):($7) t "cumulative error" w linespoints')

print (f"Plotted into file {__file__}.eps")
