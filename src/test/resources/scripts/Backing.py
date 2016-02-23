from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
from Admin import Admin

class Backing(BaseTest):
    
    def runTests(self, notifications):
        print("Running backing and offer tests")
        self.offers = 0
        self.emailOffers = 0
        self.backings = 0
        self.offersAmount = 0
        self.emailOffersAmount = 0
        self.backingAmount = 0
        self.notifications = notifications
        self.notifications['backing']['offer'] = 0
        self.notifications['backing']['withdraw'] = 0
        self.notifications['backing']['cancel'] = 0
        self.notifications['backing']['deny'] = 0
        self.notifications['backing']['accept'] = 0
        
        admin = Admin()
        admin.addCurrency(self.username, 2000)
        admin.addCurrency(self.target, 2000)
        
        self.testAddOffers()
        self.testDenyOffer()
        self.testAddOffers()
        self.testWithdrawOffer()
        self.testAddOffers()
        #self.testNotifications()
        self.testWithdrawEmailOffer()
        self.testAddOffers()
        self.testAcceptOffer()
        self.testAddOffers()
        #self.testNotifications()
        self.testRemoveBacking()
        self.testAddOffers()
        self.testAcceptOffer()
        self.testAddOffers()
        self.testWithdrawOffer()
        self.testAddOffers()
        #self.testNotifications()
        
    def testOffers(self):
        offer = {'source':self.createUsername(self.target), 
                 'target':self.createUsername(self.username), 
                 'value':self.offersAmount + self.emailOffersAmount}
        outstandingOffer = {'source':self.createUsername(self.target), 
                            'target':self.createUsername(self.username), 
                            'value':self.offersAmount}
        outstandingEmailOffer = {'source':self.createUsername(self.target), 
                                 'target':self.createEmail(self.email.lower()), 
                                 'value':self.emailOffersAmount}
        page = {'totalElements':self.offers + self.emailOffers}
        expectedPage = self.unauthed.getPageResponse(page)
        
        self.unauthed.expectResponse(Paths.OFFERS, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.OFFERS, PyRequest.GET, None, expectedPage)
        
        self.unauthed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        if self.offers > 0:
            expected = self.unauthed.getDTOResponse(offer)
            self.unauthed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, self.expectedDenied, self.target)
            self.authed.expectResponse(Paths.OFFERS_ID, PyRequest.GET, None, expected, self.target)
       
        page = {'totalElements':self.offers}
        expectedPage = self.unauthed.getPageResponse(page)
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING, PyRequest.GET, None, expectedPage)
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "failnottaken")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "-_-_")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        if self.offers > 0:
            expected = self.unauthed.getDTOResponse(outstandingOffer)
            self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, self.username)
            self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_ID, PyRequest.GET, None, expected, self.username)
        
        page = {'totalElements':self.emailOffers}
        expectedPage = self.unauthed.getPageResponse(page)
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL, PyRequest.GET, None, expectedPage)
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedDenied, "failnottaken@fail.com")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedNotFound, "failnottaken@fail.com")
        
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedDenied, "-_-_")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedDenied, 
                                     "waytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.com")
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedInvalid, 
                                         "waytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.com")
        
        if self.emailOffers > 0:
            expected = self.unauthed.getDTOResponse(outstandingEmailOffer)
            self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, self.expectedDenied, self.email.lower())
            self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL_ID, PyRequest.GET, None, expected, self.email.lower())
        
    def testBackings(self):
        backing = {'source':self.createUsername(self.target), 
                   'target':self.createUsername(self.username), 
                   'value':self.backingAmount}
        outstandingBacking = {'source':self.createUsername(self.target), 
                              'target':self.createUsername(self.username), 
                              'value':self.backingAmount}
        
        page = {'totalElements':self.backings}
        expectedPage = self.unauthed.getPageResponse(page)
        self.unauthed.expectResponse(Paths.BACKINGS, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.BACKINGS, PyRequest.GET, None, expectedPage)
        
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        if self.backings > 0:
            expected = self.unauthed.getDTOResponse(backing)
            self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedDenied, self.target)
            self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, expected, self.target)

        self.unauthed.expectResponse(Paths.BACKINGS_OUTSTANDING, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.BACKINGS_OUTSTANDING, PyRequest.GET, None, expectedPage)
        
        self.unauthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "failnottaken")
        self.targetAuthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "-_-_")
        self.targetAuthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.targetAuthed.expectResponse(Paths.BACKINGS_ID, PyRequest.GET, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        if self.backings > 0:
            expected = self.unauthed.getDTOResponse(outstandingBacking)
            self.unauthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, self.expectedDenied, self.username)
            self.targetAuthed.expectResponse(Paths.BACKINGS_OUTSTANDING_ID, PyRequest.GET, None, expected, self.username)
        
    def testAddOffers(self):
        self.testOffers()
        self.testBackings()
        highBody = {'username':self.username, 'amount':100000000}
        highCostBody = {'username':self.username, 'amount':9999}
        body = {'username':self.username, 'amount':10}
	noUserBody = {'amount':10}
	notFoundBody = {'username':'failnottaken', 'amount':10}
        self.unauthed.expectResponse(Paths.OFFERS, PyRequest.POST, body, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, highBody, self.expectedInvalid)
        self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, highCostBody, self.expectedBalance)
        self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, noUserBody, self.expectedInvalid)
        self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, notFoundBody, self.expectedNotFound)
        self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, body, self.expectedSuccess)
        self.offersAmount += 10
        self.offers = 1
        self.notifications['backing']['offer'] += 1
        
        dto = {'source':self.createUsername(self.target), 
               'target':self.createUsername(self.username), 
               'value':self.offersAmount}
        page = {'totalElements':self.offers, 'content':dto}
        expectedPage = self.unauthed.getPageResponse(page)
        self.unauthed.expectResponse(Paths.OFFERS_OUTSTANDING, PyRequest.GET, None, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING, PyRequest.GET, None, expectedPage)
        
        
        highBody = {'email':self.email, 'amount':100000000}
        highCostBody = {'email':self.email, 'amount':9999}
        body = {'email':self.email, 'amount':10}
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL, PyRequest.POST, body, self.expectedDenied)
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL, PyRequest.POST, highBody, self.expectedInvalid)
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL, PyRequest.POST, highCostBody, self.expectedBalance)
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL, PyRequest.POST, body, self.expectedSuccess)
        self.emailOffersAmount += 10
        self.emailOffers = 1
        self.notifications['backing']['offer'] += 1
        
        dto = {'source':self.createUsername(self.target), 
               'target':self.createEmail(self.email.lower()), 
               'value':self.emailOffersAmount}
        page = {'totalElements':self.emailOffers, 'content':dto}
        expectedPage = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.OFFERS_OUTSTANDING_EMAIL, PyRequest.GET, None, expectedPage)
        
        self.testOffers()
        self.testBackings()
        
    def testAcceptOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedSuccess, self.target)
        self.offers = 0
        self.backings = 1
        self.backingAmount += self.offersAmount
        self.offersAmount = 0
        self.notifications['backing']['accept'] += 1
        self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedNotFound, self.target)
        
    def testAcceptEmailOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedSuccess, self.target)
        self.emailOffers = 0
        self.backings = 1
        self.backingAmount += self.emailOffersAmount
        self.emailOffersAmount = 0
        self.notifications['backing']['accept'] += 1
        self.authed.expectResponse(Paths.OFFERS_EMAIL_ACCEPT, PyRequest.POST, None, self.expectedNotFound, self.target)
    
    def testDenyOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedSuccess, self.target)
        self.offers = 0
        self.offersAmount = 0
        self.notifications['backing']['deny'] += 1
        self.authed.expectResponse(Paths.OFFERS_DENY, PyRequest.DELETE, None, self.expectedNotFound, self.target)
    
    def testDenyEmailOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedSuccess, self.target)
        self.emailOffers = 0
        self.emailOffersAmount = 0
        self.notifications['backing']['deny'] += 1
        self.authed.expectResponse(Paths.OFFERS_EMAIL_DENY, PyRequest.DELETE, None, self.expectedNotFound, self.target)
        
    def testWithdrawOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, "failnottaken")
        self.targetAuthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, "-_-_")
        self.targetAuthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.targetAuthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, self.username)
        self.targetAuthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedSuccess, self.username)
        self.offers = 0
        self.offersAmount = 0
        self.notifications['backing']['withdraw'] += 1
        self.targetAuthed.expectResponse(Paths.OFFERS_WITHDRAW, PyRequest.DELETE, None, self.expectedNotFound, self.username)
        
    def testWithdrawEmailOffer(self):
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, "failnottaken@fail.com")
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedNotFound, "failnottaken@fail.com")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, "-_-_")
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied,
                                     "waytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.com")
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedInvalid,
                                     "waytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.comwaytoolongandsuchforanyusername12345678901234567890waytoolongandsuchforanyusername12345678901234567890@fail.com")
        
        self.unauthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedDenied, self.email.lower())
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedSuccess, self.email.lower())
        self.emailOffers = 0
        self.emailOffersAmount = 0
        self.notifications['backing']['withdraw'] += 1
        self.targetAuthed.expectResponse(Paths.OFFERS_EMAIL_WITHDRAW, PyRequest.DELETE, None, self.expectedNotFound, self.email.lower())
        
    def testRemoveBacking(self):
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedDenied, "failnottaken")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedNotFound, "failnottaken")
        
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedDenied, "-_-_")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedInvalid, "-_-_")
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedDenied, "waytoolongandsuchforanyusername12345678901234567890")
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedInvalid, "waytoolongandsuchforanyusername12345678901234567890")
        
        self.unauthed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedSuccess, self.target)
        self.backings = 0
        self.backingAmount = 0
        self.notifications['backing']['cancel'] += 1
        
        self.authed.expectResponse(Paths.BACKINGS_ID, PyRequest.DELETE, None, self.expectedNotFound, self.target)
        
    def testNotifications(self):
        page = {'totalElements':self.notifications['backing']['offer']}
        expected = self.unauthed.getPageResponse(page)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=offer')
        
        page = {'totalElements':self.notifications['backing']['accept']}
        expected = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=offer_accept')
        
        page = {'totalElements':self.notifications['backing']['deny']}
        expected = self.unauthed.getPageResponse(page)
        #self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=offer_deny')
        
        page = {'totalElements':self.notifications['backing']['withdraw']}
        expected = self.unauthed.getPageResponse(page)
        #self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=offer_withdraw')
        
        page = {'totalElements':self.notifications['backing']['cancel']}
        expected = self.unauthed.getPageResponse(page)
        #self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=backing_cancel')
        
