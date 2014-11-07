__author__ = 'freak'
import happybase

connection = happybase.Connection(host='127.0.0.1', port=9090)
domains_table = connection.table('verifieddomains')
rows = domains_table.scan()
urlstr = 'http://www.runningshoesguru.com/2014/03/brooks-pureconnect-3-review/'
articletype = 'article'
verifiedas = 'proper'
paths = "['/html/body/div[3]/div[1]/div[2]/div[2]/div/div[1]/div/div[1]/div[1]/div[1]/div/div[8]', '/html/body/div[3]/div[1]/div[2]/div[1]/div/div[7]/div/ul/li[5]']"
row_count = sum(1 for _ in rows)
rowkey = 'r' + str(row_count + 1)
#domains_table.put(rowkey, {'url:urlttested': urlstr, 'article:type': articletype, 'article:paths': paths, 'verifiedas:v':verifiedas})

rows = domains_table.scan()
for r in rows:
    print(r[1]['domain:domaintested'])
