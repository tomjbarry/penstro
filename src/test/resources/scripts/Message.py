from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest

class Message(BaseTest):
    def runTests(self):
        print("Running message tests")
        self.inbox = 0
        self.outbox = 0
        self.conversations = 0
        self.testConversations()
        self.testConversationMessages()
        self.testSend()
        self.testConversations()
        self.testConversationMessages()
        self.testFlagConversation()
        self.testConversationMessages()
        self.testConversations()
    
    def testConversations(self):
        if self.inbox > 1 or self.outbox > 1:
            self.conversations = 1
        expectedEmpty = self.unauthed.getPageResponse({"totalElements": 0})
        if self.conversations == 0:
            self.authed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.GET, None, self.expectedNotFound, self.target)
        expectedConversations = self.unauthed.getPageResponse({"totalElements": self.conversations})
        self.unauthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)
        self.targetAuthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)

        self.unauthed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.DELETE, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.DELETE, None, self.expectedSuccess, self.target)
	self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedEmpty)
        self.targetAuthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)
        
        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.DELETE, None, self.expectedSuccess, self.username)
	self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedEmpty)
        self.targetAuthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedEmpty)
        
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.POST, None, self.expectedSuccess, self.target)
	self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)
        self.targetAuthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedEmpty)

        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.POST, None, self.expectedSuccess, self.username)
	self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)
        self.targetAuthed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)

        if self.conversations > 0:
            self.authed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.GET, None, self.unauthed.getDTOResponse({}), self.target)
            self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.DELETE, None, self.expectedSuccess, self.target)
            self.authed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.GET, None, self.expectedNotAllowed, self.target)
	    self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_SHOW, PyRequest.POST, None, self.expectedSuccess, self.target)
	
        self.authed.expectResponse(Paths.MESSAGES, PyRequest.GET, None, expectedConversations)
        
    def testConversationMessages(self):
        page = {'totalElements':self.inbox + self.outbox}
        expected = self.unauthed.getPageResponse(page)
        
        self.unauthed.expectResponse(Paths.MESSAGES_CONVERSATION_MESSAGES, PyRequest.GET, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_MESSAGES, PyRequest.GET, None, expected, self.target)
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_MESSAGES, PyRequest.GET, None, expected, self.target.upper())
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_MESSAGES, PyRequest.GET, None, expected, self.target.lower())
        
    def testSend(self):
        body = {'message':'This is a test!'}
        message = {"message":"This is a test!", "author":self.createUsername(self.username)}
        
        self.unauthed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.POST, body, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.POST, body, self.expectedSuccess, self.target)
        self.outbox = self.outbox + 1
        
        
        targetPage = {"totalElements": self.outbox, "content": message}
        expectedTargetResponse = self.unauthed.getPageResponse(targetPage)
        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION_MESSAGES, PyRequest.GET, None, expectedTargetResponse, self.username)
        
        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.POST, body, self.expectedSuccess, self.username)
        
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedSuccess, self.target)
        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.POST, body, self.expectedNotAllowed, self.username)
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedSuccess, self.target)
        self.targetAuthed.expectResponse(Paths.MESSAGES_CONVERSATION, PyRequest.POST, body, self.expectedSuccess, self.username)
        self.inbox = self.inbox + 2
        
    def testFlagConversation(self):
        self.unauthed.expectResponse(Paths.MESSAGES_CONVERSATION_FLAG, PyRequest.POST, None, self.expectedDenied, self.target)
        self.authed.expectResponse(Paths.MESSAGES_CONVERSATION_FLAG, PyRequest.POST, None, self.expectedSuccess, self.target)
        # ensure user is not suspended after a single flagging
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        
