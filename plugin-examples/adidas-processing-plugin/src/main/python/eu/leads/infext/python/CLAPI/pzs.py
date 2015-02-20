#
# Freelance server - Model 1
# Trivial echo service
#
# Author: Daniel Lundin <dln(at)eintr(dot)org>
#

import sys
import zmq
from eu.leads.infext.python.CLAPI import helloworld_clinterface,\
    addtobasketbuttonclassifier_clinterface, clusteringtest_clinterface,\
    determineschemaforthesite_clinterface, ecomnewpagetypeclassifier_clinterface,\
    ecomvaluesextraction_clapi, googlenewsfeedschecker_clinterface,\
    leadsecomschemaextractor_clinterface,\
    productofferingpageclassifier_clinterface, valuesextraction_clinterface, getpublishdate_AP
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters
import logging
import traceback

def factory(str):
    if str == 'eu.leads.infext.python.CLAPI.helloworld_clinterface':
        return helloworld_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.addtobasketbuttonclassifier_clinterface':
        return addtobasketbuttonclassifier_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.clusteringtest_clinterface':
        return clusteringtest_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.determineschemaforthesite_clinterface':
        return determineschemaforthesite_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.ecomnewpagetypeclassifier_clinterface':
        return ecomnewpagetypeclassifier_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.ecomvaluesextraction_clapi':
        return ecomvaluesextraction_clapi.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.googlenewsfeedschecker_clinterface':
        return googlenewsfeedschecker_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.leadsecomschemaextractor_clinterface':
        return leadsecomschemaextractor_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.productofferingpageclassifier_clinterface':
        return productofferingpageclassifier_clinterface.AccessPoint()
    elif str == 'eu.leads.infext.python.CLAPI.valuesextraction_clinterface':
        return valuesextraction_clinterface.AccessPoint()
    elif str == "eu.leads.infext.python.CLAPI.getpublishdate_AP":
        return getpublishdate_AP.AccessPoint()
    # and so on
    
    
def setup_custom_logger(name,fhsuffix):
    formatter = logging.Formatter(fmt='%(asctime)s - %(levelname)s - %(module)s - %(message)s')

    handler = logging.FileHandler('/home/nonlinear/leads-pzs-'+fhsuffix+'.log')
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(handler)
    return logger


if __name__ == '__main__':
    setup_custom_logger("leads", sys.argv[1][16:20])
    logger = logging.getLogger("leads")
    if len(sys.argv) < 2:
        logging.debug("I: Syntax: %s <endpoint>" % sys.argv[0])
        sys.exit(0)
        
    
    endpoint = sys.argv[1]
    context = zmq.Context()
    server = context.socket(zmq.REP)
    logging.debug(endpoint)
    server.bind(endpoint)
    
    logger.debug("I: Echo service is ready at %s" % endpoint)
    while True:
        logger.debug("In the loop")
        msg = server.recv_multipart()
        logging.debug("msg: %s" % str(msg))
        if not msg:
            break  # Interrupted
        
        # read message
        # pt.1 module name
        logger.debug("module name: %s" % msg[0])
        ap = None
        try:
            ap = factory(msg[0])
            # pt.2 all of the params
            paramstr = str([text for text in msg[1:]])
            logger.debug("input params: %s" % paramstr)
            params = translateInputParameters(msg)
        except:
            logger.debug("UNEXPECTED EXCEPTION in the main module: %s" % str(traceback.format_exc()))    
            break        
        # call a class with all of the params as list 
        retval = ""
        try:
            retval = ap.execute(params)
            logger.debug(retval)
        except:
            logger.debug("EXCEPTION during module %s execution: %s" % (msg[0], str(traceback.format_exc())))
        
        server.send_json(retval)
    
    logger.debug("finishing")
    server.setsockopt(zmq.LINGER, 0) # Terminate immediately
    logger.debug("finishing")



