from PyConstants import Paths
from PyConstants import Codes
from PyConstants import CacheTimes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
import time

class Authentication(BaseTest):
    
    password = "testPassword123"
    invalidPassword = "incorrectincorrect"
    
    def runTests(self):
        print("Running authentication tests")
        self.testRegister(self.username, self.email)
        token = self.testLogin(self.username)
        self.testRegister(self.target, self.targetEmail)
        self.testLogout(token)
        
        time.sleep(CacheTimes.USER_USERNAME)
        token = self.testLogin(self.username)
        targetToken = self.testLogin(self.target)
        time.sleep(CacheTimes.USER_USERNAME)
        return targetToken, token
    
    def testRegister(self, username, email):
        invalidBody = {"username":username, "email":email}
        body = {"username":username, "email":email, "password":self.password, "confirmNewPassword":self.password, "ageMinimum":True, "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)
        
        invalidBody = {"email":email, "password":self.password}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)
        
        invalidBody = {"username":username, "password":self.password}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)

        invalidBody = {"username":username, "email":email, "password":self.password}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)

        invalidBody = {"username":username, "email":email, "password":self.password, "confirmNewPassword":self.password + "s", "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)

        invalidBody = {"username":username, "email":email, "password":self.password, "confirmNewPassword":self.password, "ageMinimum":False, "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, invalidBody, self.expectedInvalid)
        
        restrictedBody = {"username":username, "password":"password1234567", "email":email, "confirmNewPassword":"password1234567", "ageMinimum":True, "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, restrictedBody, self.expectedRestrictedPassword)
        
        restrictedBody = {"username":"penstro", "password":self.password, "email":email, "confirmNewPassword":self.password, "ageMinimum":True, "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, restrictedBody, self.expectedRestrictedUsername)
        
        restrictedBody = {"username":username, "password":self.password, "email":"dmca@penstro.com", "confirmNewPassword":self.password, "ageMinimum":True, "recaptchaResponse":"test"}
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, restrictedBody, self.expectedRestrictedEmail)
        
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, body, self.expectedResultCreated)
        PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, body, self.expectedExistsUsernameEmail)
        
    def testLogin(self, username):
        body = {"username":username, "password":self.invalidPassword}
        PyRequest().expectResponse(Paths.LOGIN, PyRequest.POST, None, self.expectedInvalid)
        PyRequest().expectResponse(Paths.LOGIN, PyRequest.POST, body, self.expectedDenied)
        body = {"username":username, "password":self.password}
        data = PyRequest().expectResponse(Paths.LOGIN, PyRequest.POST, body, self.expectedResultSuccess)
        if 'dto' in data:
            if 'result' in data['dto']:
                print("TOKEN: " + str(data['dto']['result']))
                return str(data['dto']['result'])
        return None
        
    def testLogout(self, token):
        PyRequest().expectResponse(Paths.LOGOUT, PyRequest.POST, None, self.expectedDenied)
        PyRequest(token).expectResponse(Paths.LOGOUT, PyRequest.POST, None, self.expectedSuccess)
        
