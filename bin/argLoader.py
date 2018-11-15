import sys

argStr = " ".join(sys.argv)

with open("args.txt", "w") as f:
    f.write(argStr)