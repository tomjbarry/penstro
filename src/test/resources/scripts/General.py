from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest

class General(BaseTest):
    
    def runTests(self):
        print("Running general tests")
        self.testIndex()
        self.testInvalid()
        self.testFailure()
    
    def testIndex(self):
        expected = {PyRequest.CODE:Codes.SUCCESS}
        PyRequest().expectResponse(Paths.INDEX, PyRequest.GET, None, expected)
        PyRequest(self.token).expectResponse(Paths.INDEX, PyRequest.GET, None, expected)
        
    def testInvalid(self):
        expected = {PyRequest.CODE:Codes.INVALID}
        PyRequest().expectResponse(Paths.INVALID, PyRequest.GET, None, expected)
        PyRequest(self.token).expectResponse(Paths.INVALID, PyRequest.GET, None, expected)
        
    def testFailure(self):
        expected = {PyRequest.CODE:Codes.FAILURE}
        PyRequest().expectResponse(Paths.FAILURE, PyRequest.GET, None, expected)
        PyRequest(self.token).expectResponse(Paths.FAILURE, PyRequest.GET, None, expected)
