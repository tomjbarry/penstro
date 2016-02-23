from PyRequest import PyRequest
from PyConstants import Codes
import json


class BaseTest(object):
    
    def __init__(self, token=None, username=None, email=None, targetToken=None,target=None, targetEmail=None):
        print("")
        self.token = str(token)
        self.targetToken = str(targetToken)
        self.authed = PyRequest(self.token)
        self.unauthed = PyRequest()
        self.targetAuthed = PyRequest(self.targetToken)
        self.username = str(username)
        self.email = str(email)
        self.target = str(target)
        self.targetEmail = str(targetEmail)
        result = {'result': self.unauthed.insertExists()}
        self.expectedResultSuccess = self.unauthed.getDTOResponse(result)
        self.expectedResultCreated = self.unauthed.getCustomResponse(Codes.CREATED, result)
        
        # Default expecteds
        self.expectedSuccess = self.unauthed.getOnlyCodeResponse(Codes.SUCCESS)
        self.expectedCreated = self.unauthed.getOnlyCodeResponse(Codes.CREATED)
        self.expectedDeleted = self.unauthed.getOnlyCodeResponse(Codes.DELETED)
        
        self.expectedFailure = self.unauthed.getOnlyCodeResponse(Codes.FAILURE)
        self.expectedDenied = self.unauthed.getOnlyCodeResponse(Codes.DENIED)
        self.expectedNotAllowed = self.unauthed.getOnlyCodeResponse(Codes.NOT_ALLOWED)
        self.expectedInvalid = self.unauthed.getCustomResponse(Codes.INVALID, None, self.unauthed.insertNotExists())
        self.expectedLocked = self.unauthed.getOnlyCodeResponse(Codes.LOCKED)
        self.expectedExpired = self.unauthed.getOnlyCodeResponse(Codes.EXPIRED)
        self.expectedExists = self.unauthed.getOnlyCodeResponse(Codes.EXISTS)
        self.expectedExistsUsernameEmail = self.unauthed.getOnlyCodeResponse([Codes.EXISTS, Codes.EXISTS_USERNAME, Codes.EXISTS_EMAIL])
        self.expectedNotFound = self.unauthed.getOnlyCodeResponse(Codes.NOT_FOUND)
        self.expectedBalance = self.unauthed.getOnlyCodeResponse(Codes.BALANCE)
        self.expectedPageable = self.unauthed.getOnlyCodeResponse(Codes.PAGEABLE)
        self.expectedParameter = self.unauthed.getOnlyCodeResponse(Codes.PARAMETER)
        
        self.expectedError = self.unauthed.getOnlyCodeResponse(Codes.ERROR)
        self.expectedFinance = self.unauthed.getOnlyCodeResponse(Codes.FINANCE)
        
        self.expectedRestricted = self.unauthed.getOnlyCodeResponse(Codes.RESTRICTED)
        self.expectedRestrictedUsername = self.unauthed.getOnlyCodeResponse(Codes.RESTRICTED_USERNAME)
        self.expectedRestrictedPassword = self.unauthed.getOnlyCodeResponse(Codes.RESTRICTED_PASSWORD)
        self.expectedRestrictedEmail = self.unauthed.getOnlyCodeResponse(Codes.RESTRICTED_EMAIL)
        self.expectedThrottledAddress = self.unauthed.getOnlyCodeResponse(Codes.THROTTLED_ADDRESS)
        
    def error(self, response, expected, method, path, body=None, pathVariables=None, params=None, information=None):
        print("Errors found for path: " + method + " " + path)
        if pathVariables != None:
            print("Path variables: " + str(pathVariables))
        if params != None:
            print("Params: " + str(params))
        if body != None:
            print("Body: " + str(body))
        print("JSON Response: " + json.dumps(response))
        print("Expected: " + json.dumps(expected))
        
    def createUsername(self, username, exists = True):
        return {"username":username, "exists":exists}
    
    def createEmail(self, email):
        return self.createUsername(email, False)
