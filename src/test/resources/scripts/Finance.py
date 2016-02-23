from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
from Admin import Admin

class Finance(BaseTest):
    
    purchase = 1000
    
    def runTests(self):
        print("Running finance tests")
        data = self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, 
                                          {PyRequest.CODE:Codes.SUCCESS})
        self.balance = data['dto']['balance']
        self.testBalance()
        self.testAdd()
        self.testBalance()
    
    def testBalance(self):
        if not hasattr(self, 'balance'):
            self.balance = 0
        dto = {
               'balance':self.balance
               }
        expected = self.unauthed.getDTOResponse(dto)
        
        self.unauthed.expectResponse(Paths.FINANCES, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expected)
        
    def testAdd(self):
        expected = self.unauthed.getOnlyCodeResponse(Codes.SUCCESS)
        expectedFailure = self.unauthed.getOnlyCodeResponse(Codes.DENIED)
        
        self.purchase = 1000
        
        Admin().addCurrency(self.username, self.purchase)
        # temporary until payment is allowed
        self.balance = self.balance + self.purchase
        
