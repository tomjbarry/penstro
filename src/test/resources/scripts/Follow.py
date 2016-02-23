from PyConstants import Paths
from PyConstants import Codes
from PyConstants import CacheTimes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
from random import randrange
import time

class Follow(BaseTest):
    
    def runTests(self, secondaryToken, secondaryName):
        
        self.secondary = secondaryName
        self.secondaryAuthed = PyRequest(secondaryToken)
        self.notifications = {'follow' + self.username:0,
                              'followRemove' + self.username:0,
                              'follow' + self.target:0,
                              'followRemove' + self.target:0,
                              'follow' + self.secondary:0,
                              'followRemove' + self.secondary:0}
        data = self.unauthed.expectResponse(Paths.USERS, PyRequest.GET, None, self.unauthed.getPageResponse(), None, 'time=alltime')
        users = data['page']['content']
        self.userList = map(lambda u: u['username'], users)
        self.userAddedList = ['Tom']
        self.userFollowerList = []
	self.userBlockedList = []
        
        print("Running follow tests")
        self.testAddFollowee()
        self.testAddFollowee()
        self.testTarget()
        self.testAddFollowee()
        self.testSecondary()
        
    def testFollow(self):
        
        for k in self.userList:
            if k in self.userAddedList:
                expected = self.unauthed.getDTOResponse({'added':self.unauthed.insertExists(),
                                                         'username':self.createUsername(k)})
                self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, expected, k)
            else:
                self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedNotFound, k)
    
        for k in self.userList:
            if k in self.userFollowerList:
                expected = self.unauthed.getDTOResponse({'added':self.unauthed.insertExists(),
                                                         'username':self.createUsername(self.username)})
                self.authed.expectResponse(Paths.FOLLOWERS_ID, PyRequest.GET, None, expected, k)
            else:
                self.authed.expectResponse(Paths.FOLLOWERS_ID, PyRequest.GET, None, self.expectedNotFound, k)
    
        time.sleep(CacheTimes.SUBSCRIPTION)
    
        expected = self.unauthed.getPageResponse({'totalElements': len(self.userAddedList)})
        self.unauthed.expectResponse(Paths.FOLLOWEES, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.FOLLOWEES, PyRequest.GET, None, expected)
        
        self.unauthed.expectResponse(Paths.USER_FOLLOWEES, PyRequest.GET, None, expected, self.username)
        self.authed.expectResponse(Paths.USER_FOLLOWEES, PyRequest.GET, None, expected, self.username)
        
        
    def testTarget(self):
        if self.target not in self.userFollowerList:
            self.targetAuthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedSuccess, self.username)
            self.userFollowerList.append(self.target)
            self.notifications['follow' + self.username] += 1
        else:
            self.targetAuthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedSuccess, self.username)
            self.userFollowerList.remove(self.target)
            self.notifications['followRemove' + self.username] += 1
            
    def testSecondary(self):
        if self.secondary not in self.userFollowerList:
            self.secondaryAuthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedSuccess, self.username)
            self.userFollowerList.append(self.secondary)
            self.notifications['follow' + self.username] += 1
        else:
            self.secondaryAuthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedSuccess, self.username)
            self.userFollowerList.remove(self.secondary)
            self.notifications['followRemove' + self.username] += 1
        
    
    def testAddFollowee(self):
        self.testFollow()
        numKey = randrange(len(self.userList))
        while self.username == self.userList[numKey]['username']:
            numKey = randrange(len(self.userList))
        
        name = self.userList[numKey]['username']
        
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedNotAllowed, self.username)
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedNotFound, 'failnottaken')
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedInvalid, '-_-_')
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedInvalid, 'waytoolongandsuchforanyusername12345678901234567890')
        
        self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedDenied, name)
        
        if name not in self.userAddedList:
            self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedSuccess, name)
            self.userAddedList.append(name)
            if name == self.target or name == self.secondary:
                self.notifications['follow' + name] += 1
        else:
            expected = self.unauthed.getDTOResponse({'added':self.unauthed.insertExists(),
                                                     'username':self.createUsername(name)})
            self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, expected, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedExists, name)
        
        self.testFollow()
        
    def testRemoveFollowee(self):
        self.testFollow()
        if len(self.userAddedList) <= 0:
            return
        numKey = randrange(len(self.userAddedList))
        while self.username == self.userAddedList[numKey]:
            numKey = randrange(len(self.userAddedList))
        
        name = self.userAddedList[numKey]
        
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedNotFound, self.username)
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedNotFound, 'failnottaken')
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedInvalid, '-_-_')
        self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedInvalid, 'waytoolongandsuchforanyusername12345678901234567890')
        
        self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedDenied, name)
        
        if name not in self.userAddedList:
            self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedNotFound, name)
        else:
            self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.DELETE, None, self.expectedSuccess, name)
            self.userAddedList.remove(name)
            if name == self.target or name == self.secondary:
                self.notifications['followRemove' + name] += 1
        
        self.testFollow()
        
    def testBlocked(self):
        
        for k in self.userList:
            if k in self.userBlockedList:
                expected = self.unauthed.getDTOResponse({'added':self.unauthed.insertExists(),
                                                         'username':self.createUsername(k)})
                self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, expected, k)
            else:
                self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedNotFound, k)
    
        time.sleep(CacheTimes.SUBSCRIPTION)

        expected = self.unauthed.getPageResponse({'totalElements': len(self.userBlockedList)})
        self.unauthed.expectResponse(Paths.BLOCKED, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.BLOCKED, PyRequest.GET, None, expected)
        
    def testAddBlock(self):
        self.testBlocked()
        numKey = randrange(len(self.userList))
        while self.username == self.userList[numKey]['username']:
            numKey = randrange(len(self.userList))
        
        name = self.userList[numKey]['username']
        
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedNotAllowed, self.username)
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedNotFound, 'failnottaken')
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedInvalid, '-_-_')
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedInvalid, 'waytoolongandsuchforanyusername12345678901234567890')
        
        self.unauthed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedDenied, name)
        
        if name not in self.userBlockedList:
            self.unauthed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.FOLLOWEES_ID, PyRequest.POST, None, self.expectedSuccess, name)
            self.userBlockedList.append(name)
        else:
            expected = self.unauthed.getDTOResponse({'added':self.unauthed.insertExists(),
                                                     'username':self.createUsername(name)})
            self.unauthed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, expected, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.POST, None, self.expectedExists, name)
        
        self.testBlocked()
        
    def testRemoveBlock(self):
        self.testBlocked()
        if len(self.userBlockedList) <= 0:
            return
        numKey = randrange(len(self.userBlockedList))
        while self.username == self.userBlockedList[numKey]:
            numKey = randrange(len(self.userBlockedList))
        
        name = self.userBlockedList[numKey]
        
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedNotFound, self.username)
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedNotFound, 'failnottaken')
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedInvalid, '-_-_')
        self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedInvalid, 'waytoolongandsuchforanyusername12345678901234567890')
        
        self.unauthed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedDenied, name)
        
        if name not in self.userBlockedList:
            self.unauthed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedNotFound, name)
        else:
            self.unauthed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedDenied, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.GET, None, self.expectedNotFound, name)
            self.authed.expectResponse(Paths.BLOCKED_ID, PyRequest.DELETE, None, self.expectedSuccess, name)
            self.userBlockedList.remove(name)
        
        self.testBlocked()
        
    def testNotifications(self):
        page = {'totalElements':self.notifications['follow' + self.username]}
        expected = self.unauthed.getPageResponse(page)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_add')
        
        page = {'totalElements':self.notifications['followRemove' + self.username]}
        expected = self.unauthed.getPageResponse(page)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_remove')
        
        page = {'totalElements':self.notifications['follow' + self.target]}
        expected = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_add')
        
        page = {'totalElements':self.notifications['followRemove' + self.target]}
        expected = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_remove')
        
        page = {'totalElements':self.notifications['follow' + self.secondary]}
        expected = self.unauthed.getPageResponse(page)
        self.secondaryAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_add')
        
        page = {'totalElements':self.notifications['followRemove' + self.secondary]}
        expected = self.unauthed.getPageResponse(page)
        self.secondaryAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=follow_remove')
        
