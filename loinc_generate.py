import argparse

parser = argparse.ArgumentParser()
parser.add_argument("--start", type=int)
parser.add_argument("--num", type=int)
parser.add_argument("--noX", action="count")

args = parser.parse_args()
start = args.start
num = args.num
noX = args.noX

def nextTen(n):
    if (n % 10):
        n = n + (10 - n % 10)
    return n


def addChecksum(code):
    codeStr = str(code)

    # 1. convert code to character array and reverse to assign positions
    codeArr = list(codeStr)[::-1]

    # 2. get the odd numbered values and convert to integer
    odd = int("".join(codeArr[0::2]))

    # 3. multiply by 2
    mult = odd*2

    # 4. Take the even digit positions
    even = int("".join(codeArr[1::2]))

    # 5. Append the even value to the front of the value in #3
    app = str(even) + str(mult)

    # 6. Add the digits together
    appArr = list(str(app))
    sum = 0
    for x in appArr:
        sum += int(x)

    # 7. Find next multiple of 10
    multTen = nextTen(sum)

    cksum = multTen - sum
    return str(code) + "-" + str(cksum)

# main program
codes = []
for i in range(start, start+num):
    code = addChecksum(i)
    if noX == None:
        code = "X" + code

    codes.append(code)

for c in codes:
    print(c)
