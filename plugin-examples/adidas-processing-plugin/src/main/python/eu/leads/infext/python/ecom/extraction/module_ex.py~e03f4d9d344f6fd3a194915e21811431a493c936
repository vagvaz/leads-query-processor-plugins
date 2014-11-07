'''
Created on Dec 9, 2013

@author: nonlinear
'''

from lxml import html
import requests

if __name__ == '__main__':
    print('Hello World!')
else:
    print('Hello Madafaka!')
    
page = requests.get('http://www.adidas.com/us/product/mens-soccer-11-pro-trx-fg-cleats/ACR09')
atree = html.fromstring(page.text)

product = [ x.strip() for x in atree.xpath('//div[@id="productName"]/text()') ]
price = atree.xpath('//div[@id="productPrice"]/text()')
print(product, price)



try:
    from lxml import etree
    print("running with lxml.etree")
except ImportError:
    try:
        # Python 2.5
        import xml.etree.cElementTree as etree
        print("running with cElementTree on Python 2.5+")
    except ImportError:
        try:
            # Python 2.5
            import xml.etree.ElementTree as etree
            print("running with ElementTree on Python 2.5+")
        except ImportError:
            try:
                # normal cElementTree install
                import cElementTree as etree
                print("running with cElementTree")
            except ImportError:
                try:
                    # normal ElementTree install
                    import elementtree.ElementTree as etree
                    print("running with ElementTree")
                except ImportError:
                        print("Failed to import ElementTree from any known place")