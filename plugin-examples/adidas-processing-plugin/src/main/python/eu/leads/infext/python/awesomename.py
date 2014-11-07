'''
Created on Jan 30, 2014

@author: nonlinear
'''
from eu.leads.infext.python.comm.hbasebigtable import HBaseBigTable

hostname = '141.76.44.141'
tablename = 'webpage'
rowkey = 'de.adidas.www:http/'
columns = ['f:cnt']

bigtable = HBaseBigTable()
bigtable.connect(hostname, 9090, tablename)

pages = bigtable.get(rowkey,columns)
pages2 = bigtable.scan(rowkey,columns)
for page in pages2:
    print page