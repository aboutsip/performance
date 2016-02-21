#!python

import requests
import xml.etree.ElementTree as ET
import json
import re
import sys
import random
import string
import inspect
import time
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from multiprocessing import Process

__author__ = 'jonas'

url = 'http://localhost:8080'

def assert_not_equal(expected, actual, msg = None):
    """Make sure that the two values are NOT equal
    """
    if actual == expected:
        if msg is not None:
            print "[ERROR] %s" % msg

        raise Exception('Expected "%s"  to be different from "%s" but they were equal' % (expected, actual))

def assert_value(expected, actual, msg = None):
    if actual != expected:
        if msg is not None:
            print "[ERROR] %s" % msg

        raise Exception('Expected "%s" but got "%s"' % (expected, actual))

def get(path, test, expected_response = 200):
    """Helper method for issuing a request and optionally
    printing out all the requests and responses so we can
    create our documentation easy as well as testing

    This method will also verify the basics of the request
    which should be true for all requests
    """

    final_url = '%s%s' % (url, path)
    print ""
    print "Test: %s" % test
    print "GET %s " % path
    print "URL %s " % final_url
    resp = requests.get(final_url)
    assert_value(expected_response, resp.status_code)
    if (expected_response >= 300): 
        return expected_response 
    result = None
    assert_value('application/json',  resp.headers['content-type'])
    payload = resp.json()
    print json.dumps(payload, indent=True)
    return payload

def post(path, payload = {}, test = "", expected_response = 201):

    final_url = '%s%s' % (url, path)
    print ""
    print ""
    print "Test: %s " % test
    print "POST %s " % path
    print "URL %s " % final_url
    print "    %s" % payload
    headers = {'Content-Type' : 'application/x-www-form-urlencoded'}
    resp = requests.post(final_url, data = payload, headers = headers)
    assert_value(expected_response, resp.status_code)
    if (expected_response >= 300): 
        payload = resp.json()
        print payload['message']
        return expected_response 
    assert_value('application/json',  resp.headers['content-type'])
    payload = resp.json()
    print json.dumps(payload, indent=True)
    return payload

def put(path, payload = {}, test = "", expected_response = 201):
    final_url = '%s%s' % (url, path)
    print ""
    print ""
    print "Test: %s " % test
    print "PUT %s " % path
    print "URL %s " % final_url
    print "    %s" % payload
    resp = requests.put(final_url, data = payload)
    assert_value(expected_response, resp.status_code)
    if (expected_response >= 300): 
        payload = resp.json()
        print payload['message']
        return expected_response 
    assert_value('application/json',  resp.headers['content-type'])
    payload = resp.json()
    print json.dumps(payload, indent=True)
    return payload

def print_test_name():
    print
    print 
    print "________________________________________________________________________________"
    print "|                                                                               "    
    print "|    " + inspect.stack()[1][3] 
    print "|_______________________________________________________________________________"
    #               1         2         3         4         5         6         7         8 
    #      12345678901234567890123456789012345678901234567890123456789012345678901234567890

def create_new_sipp_instance(payload = {}):
    result = post('/sipp/instances', 'Create new SIPp instance')
    return result

def fetch_sipp_instance(uuid):
    return get('/sipp/instances/%s' % uuid, 'Fetching SIPp instance')

def set_target_rate(uuid, rate):
    return post('/sipp/instances/%s/rate' % uuid, 'Setting the target rate to %d' % rate, expected_response = 200)
    
def test_create_sipp_instance():
    print_test_name()
    sipp = create_new_sipp_instance()
    sipp2 = fetch_sipp_instance(sipp['uuid'])
    assert_value(sipp['uuid'], sipp2['uuid'])
    
    
def test_set_target_rate():
    print_test_name()
    sipp = create_new_sipp_instance()
    set_target_rate(sipp['uuid'], 10)
    
def test_set_bad_target_rate():
    """ Test to set the target rate without passing along
        an actual target. We expect 400 back.
        Note: we have to make sure we set the Content-Type
        header for this to work. If no data is passed along
        there will be no Content-Type header in the POST
        request
    """ 
    print_test_name()
    sipp = create_new_sipp_instance()
    post('/sipp/instances/%s/rate' % sipp['uuid'], 
         'Setting bad target rate', 
         expected_response = 400)

test_create_sipp_instance()
# test_set_bad_target_rate()
#test_set_target_rate()
