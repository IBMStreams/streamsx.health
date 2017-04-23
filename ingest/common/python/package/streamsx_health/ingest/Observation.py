
def isHR(obx):
    readingCode = getReadingCode(obx)
    return readingCode == '8867-4'

def getReadingCode(obx):
    reading = obx.get('reading')
    readingType = reading.get('readingType')
    readingCode = readingType.get('code')
    return readingCode

def getReadingValue(obx):
    reading = obx.get('reading')
    return reading.get('value')

def getUom(obx):
    reading = obx.get('reading')
    return reading.get('uom')

def getReadingTs(obx):
    reading = obx.get('reading')
    return reading.get('ts')

class Avg:
   def __init__(self, n):
      self.n = n
      self.last_n = []
   def __call__(self, tuple):
      self.last_n.append(getReadingValue(tuple))
      if (len(self.last_n) > self.n):
          self.last_n.pop(0)
      return sum(self.last_n)/len(self.last_n)

