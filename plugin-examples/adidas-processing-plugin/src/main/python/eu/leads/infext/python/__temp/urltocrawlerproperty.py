'''
Created on Sep 14, 2014

@author: nonlinear
'''

if __name__ == '__main__':
    with open("/data/workspace/leadsdm/eu/leads/infext/python/__temp/files/a.txt") as f:
        lines = f.readlines()
        properties = []
        for line in lines:
            line = line[:-1]
            #
            number = "500"
            #
            key = line[line.index(".")+1:] if "www" in line else line[line.index("//")+2:]
            key = key[:-1] if key[-1]=="/" else key
            #
            pattern = line
            pattern = pattern.replace(".", "\\\\.")
            pattern = pattern.replace("http", "http.?")
            #
            url = line
            
            print key+"="+pattern+";"+url+";"+number
            
            