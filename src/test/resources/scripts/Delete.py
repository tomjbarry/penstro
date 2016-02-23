from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
import time

class Delete(BaseTest):
    
    def runTests(self, secondaryToken, secondaryTargetToken):
        
        self.secondaryAuthed = PyRequest(secondaryToken)
        self.secondaryTargetAuthed = PyRequest(secondaryTargetToken)
        
        sleepTime = 20
        self.testToken = 'testingtoken'
        
        print("Running delete tests")
        
        self.testSendDelete()
        time.sleep(sleepTime)
        self.testDelete()
        self.testUndelete()
        
        self.testSendDelete()
        time.sleep(sleepTime)
        self.testDelete()
        
        
    def testSendDelete(self):
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        
        self.authed.expectResponse(Paths.USERS_DELETE_SEND, PyRequest.POST, None, self.expectedSuccess)
        self.targetAuthed.expectResponse(Paths.USERS_DELETE_SEND, PyRequest.POST, None, self.expectedSuccess)
        self.secondaryAuthed.expectResponse(Paths.USERS_DELETE_SEND, PyRequest.POST, None, self.expectedSuccess)
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_DELETE_SEND, PyRequest.POST, None, self.expectedSuccess)
        
    def testDelete(self):
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        
        self.authed.expectResponse(Paths.USERS_DELETE, PyRequest.DELETE, None, self.expectedSuccess, None, 'emailToken=' + self.testToken)
        self.targetAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.DELETE, None, self.expectedSuccess, None, 'emailToken=' + self.testToken)
        self.secondaryAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.DELETE, None, self.expectedSuccess, None, 'emailToken=' + self.testToken)
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.DELETE, None, self.expectedSuccess, None, 'emailToken=' + self.testToken)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        
    def testUndelete(self):
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        
        self.authed.expectResponse(Paths.USERS_DELETE, PyRequest.POST, None, self.expectedSuccess)
        self.targetAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.POST, None, self.expectedSuccess)
        self.secondaryAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.POST, None, self.expectedSuccess)
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_DELETE, PyRequest.POST, None, self.expectedSuccess)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        