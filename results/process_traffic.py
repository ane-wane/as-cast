import sys

## averaging window 1000ms values <=> 1 second
## (TODO) not hardcode the number of columns
secondA = [0] * 1000
secondD = [0] * 1000

aA = 0.0
aD = 0.0

i = 0

## eg. "as_cast_messages.dat"
with open(sys.argv[1], "r") as f:
    for line in f.readlines():
        words  = line.split(" ")
        
        bA = float(words[4].replace(",", "."))
        bD = float(words[7].replace(",", "."))

        secondA.append( bA - aA )
        secondD.append( bD - aD )
        del secondA[0]
        del secondD[0]
        
        aA = bA
        aD = bD

        A = sum(secondA)
        D = sum(secondD)
        
        print (f"{i} {A} {D}")

        i = i + 1



