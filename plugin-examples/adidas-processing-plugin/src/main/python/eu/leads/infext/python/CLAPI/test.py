'''
Created on Jan 29, 2015

@author: nonlinear
'''
from dateutil.parser import _timelex, parser

a = "Saudis were more interested in the King's prayer schedule than the first lady's uncovered head."
a = "IE=8"

p = parser()
info = p.info

def timetoken(token):
    try:
        float(token)
        return True
    except ValueError:
        pass
    return any(f(token) for f in (info.jump,info.weekday,info.month,info.hms,info.ampm,info.pertain,info.utczone,info.tzoffset))

def timesplit(input_string):
    batch = []
    for token in _timelex(input_string):
        if timetoken(token):
            if info.jump(token):
                continue
            batch.append(token)
        else:
            break
            if batch:
                yield " ".join(batch)
                batch = []
    if batch:
        yield " ".join(batch) 


if __name__ == '__main__':
    for item in timesplit(a):
        print "Found:", item
        print "Parsed:", p.parse(item)