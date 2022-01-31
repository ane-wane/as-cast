import sys

GROUPBY = 1

with open(sys.argv[1], "r") as f:
    isFirst = True

    i=0
    sumDuration = 0
    tA = 0
    for line in f.readlines():
        words  = line.split(" ")

        i = i + 1
        
        if not isFirst:
            tB = int(words[1][:-1])
            sumDuration = sumDuration + tB - tA
            tA = tB

        if i % GROUPBY == 0 and not isFirst:
            print (f"{tA-sumDuration/GROUPBY/2} {sumDuration/GROUPBY}")
            sumDuration = 0
            
        if isFirst:
            isFirst = False
            tA = int(words[1][:-1])
            



