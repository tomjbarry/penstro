from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest

class Notification(BaseTest):
    
    def runTests(self):
        print("Running notification tests")
        self.testNotifications()
    
    def testNotifications(self):
        expected = self.unauthed.getPageResponse()
        
        expectedFailure = self.unauthed.getOnlyCodeResponse(Codes.DENIED)
        
        self.unauthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expectedFailure)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=any')
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=posting')
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=comment')
        
