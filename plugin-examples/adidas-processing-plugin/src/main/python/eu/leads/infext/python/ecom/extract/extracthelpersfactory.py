'''
Created on Jul 20, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.extract.verify.defaultverifier import DefaultVerifier,\
    DefaultEcomNameVerifier, DefaultEcomPriceVerifier
from eu.leads.infext.python.ecom.extract.mine.priceminer import DefaultPriceMiner
from eu.leads.infext.python.ecom.extract.mine.stringminer import DefaultProductNameMiner,\
    DefaultStringMiner

def verifierFactoryMethod(objectofextraction):
    verifier = None
    if objectofextraction == "ecom_product_name":
        verifier = DefaultEcomNameVerifier()
    elif objectofextraction == "ecom_product_price":
        verifier = DefaultEcomPriceVerifier()
    else:
        verifier = DefaultVerifier()
    
    return verifier

def minerFactoryMethod(objectofextraction):
    miner = None
    if objectofextraction == "ecom_product_name":
        miner = DefaultProductNameMiner()
    elif objectofextraction == "ecom_product_price":
        miner = DefaultPriceMiner()
    else:
        miner = DefaultStringMiner()
    
    return miner