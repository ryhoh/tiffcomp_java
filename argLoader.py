# windows とか CUI環境 とかで大量の引数をプログラムに簡単に与えるためのスクリプト
import os

pwd = os.getcwd()
ls = os.listdir()
ls.remove("argLoader.py")
ls = [os.path.join(pwd, path) for path in ls]

argStr = " ".join(ls)

with open("args.txt", "w") as f:
    f.write(argStr)