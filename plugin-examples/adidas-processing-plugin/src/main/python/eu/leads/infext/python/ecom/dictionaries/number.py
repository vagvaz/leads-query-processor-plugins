'''
Created on Jan 13, 2014

@author: nonlinear
'''

from eu.leads.infext.python.ecom.dictionaries.generalreg import anyspace_regpart, B, E, __OM, __ZM, __ZO
import re

integer_regpart = "\d"
real_regpart = "(((\.|\,)(\d*|\-))|)"
fraction_regpart = "("+anyspace_regpart+r"\d+(/|\)\d+)"

numberr_regpart = "\d+((\.|\,)(\d*|\-))?"
numberf_regpart = r"\d*([ \t]*\d+(/|\\)\d+)?"

number_regex = B + "(("  +  integer_regpart+__OM  +  real_regpart+__ZO  +  ")|("  +  integer_regpart+__ZM  +  fraction_regpart+__ZO  + "))" + E

numberr_regex = re.compile(B+numberr_regpart+E)
numberf_regex = re.compile(B+numberf_regpart+E)

numberr_any_regex = re.compile(numberr_regpart)