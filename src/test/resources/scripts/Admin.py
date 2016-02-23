from PyConstants import Paths
from PyConstants import AdminPaths
from PyConstants import Codes
from PyConstants import CacheTimes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
import copy
import time
import datetime
import random
import re
import math

class Admin(BaseTest):
    
    adminName = 'admin'
    adminPassword = 'asx1c2toodlesc2c2'
    badPassword = 'incorrectincorrect'
    
    def runTests(self, secondaryToken, secondaryUsername, secondaryTargetToken, secondaryTargetUsername):
        print("Running admin tests")
        
        result = self.adminLogin()
        if result == None:
            return
        self.admin = PyRequest(result, True)
        
        self.secondaryToken = secondaryToken
        self.secondaryAuthed = PyRequest(secondaryToken)
        self.secondary = secondaryUsername
        
        self.secondaryTargetToken = secondaryTargetToken
        self.secondaryTargetAuthed = PyRequest(secondaryTargetToken)
        self.secondaryTarget = secondaryTargetUsername
        
        self.testUsersLockAdmin()
        self.testUsersLoginAttemptsAdmin()
        self.testUsersRolesAdmin()
        self.testUsersFinancesAdmin()
        
        self.testTagsPostingsCommentsLockAdmin()
        self.testChangeReferenceTallyAdmin()
        
        # due to caching for extended times, restricted cannot easily be tested unless cache is disabled
        #self.testRestrictedAdmin()
        self.testFeedback()
        #self.testRename()
        
    def adminLogin(self):
        body = {"username":self.adminName, "password":self.adminPassword}
        data = PyRequest().expectResponse(Paths.LOGIN, PyRequest.POST, body, self.expectedResultSuccess)
        if 'dto' in data:
            if 'result' in data['dto']:
                return data['dto']['result']
        return None
    
    def addCurrency(self, username, amount):
        if not hasattr(self, 'admin'):
            self.admin = PyRequest(self.adminLogin(), True)
        body = {"amount": amount}
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES_ADD, PyRequest.POST, body, self.expectedSuccess, username)
	
        
        #data = self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, self.unauthed.getDTOResponse(), username)
        #if 'unpaid' in data['dto']['overrideRoles']:
        #    overrideRoles = data['dto']['overrideRoles']
        #    overrideRoles.remove('unpaid')
        #    body = {'overrideRoles':overrideRoles}
        #    self.admin.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, body, self.expectedSuccess, username)
    
    def testUsersLockAdmin(self):
        # locking and unlocking user
        # should prevent user from accessing basic features, like currentUser, but not admin as user
        expected = self.unauthed.getDTOResponse()
        lockBody = {"lockReason":'SECURITY', "lockedUntil":int(time.time() + 60*5) * 1000}
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_CURRENT, PyRequest.GET, None, expected, self.username)
        
        self.unauthed.expectResponse(AdminPaths.ADMIN_USERS_LOCK, PyRequest.POST, lockBody, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_USERS_LOCK, PyRequest.POST, lockBody, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_LOCK, PyRequest.POST, lockBody, self.expectedSuccess, self.username)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedLocked)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_CURRENT, PyRequest.GET, None, expected, self.username)
        
        self.unauthed.expectResponse(AdminPaths.ADMIN_USERS_UNLOCK, PyRequest.POST, None, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_USERS_UNLOCK, PyRequest.POST, None, self.expectedLocked, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_UNLOCK, PyRequest.POST, None, self.expectedSuccess, self.username)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_CURRENT, PyRequest.GET, None, expected, self.username)
        
    def testUsersLoginAttemptsAdmin(self):
        # reset login attempts
        expected = self.unauthed.getDTOResponse()
        currentUser = self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        loginFailures = currentUser['dto']['loginFailureCount']
        body = {"username":self.username, "password":self.badPassword}
        data = PyRequest().expectResponse(Paths.LOGIN, PyRequest.POST, body, self.expectedDenied)
        loginFailures = loginFailures + 1
        expectedCurrentUser = self.unauthed.getDTOResponse({'loginFailureCount': loginFailures})
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expectedCurrentUser)
        
        self.unauthed.expectResponse(AdminPaths.ADMIN_USERS_LOGIN_ATTEMPTS, PyRequest.DELETE, None, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_USERS_LOGIN_ATTEMPTS, PyRequest.DELETE, None, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_LOGIN_ATTEMPTS, PyRequest.DELETE, None, self.expectedSuccess, self.username)
        
        loginFailures = 0
        expectedCurrentUser = self.unauthed.getDTOResponse({'loginFailureCount': loginFailures})
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expectedCurrentUser)
        
    def testUsersRolesAdmin(self):
        # roles
        expected = self.unauthed.getDTOResponse()
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        
        roleSet = {'roles':['profile']}
        expectedRoleSet = self.unauthed.getDTOResponse(roleSet)
        self.unauthed.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, self.expectedDenied, self.username)
        self.authed.expectResponse(Paths.ROLES, PyRequest.GET, None, expectedRoleSet)
        roles = self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleSet, self.username)
        
        roleChange = copy.deepcopy(roles['dto'])
        roleChange['roles'].remove('profile')
        self.unauthed.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedSuccess, self.username)
        
        expectedRoleChange = self.unauthed.getDTOResponse(roleChange)
        self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleChange, self.username)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        roleChange['roles'].append('profile')
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedSuccess, self.username)
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        
        expectedRoleChange = self.unauthed.getDTOResponse(roleChange)
        self.authed.expectResponse(Paths.ROLES, PyRequest.GET, None, expectedRoleChange)
        self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleChange, self.username)
        
        roleChange['overrideRoles'].append('unaccepted')
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedSuccess, self.username)
	# no longer is current user disabled if unaccepted        
	#self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedDenied)
        
        expectedRoleChange = self.unauthed.getDTOResponse(roleChange)
        self.authed.expectResponse(Paths.ROLES, PyRequest.GET, None, expectedRoleChange)
        self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleChange, self.username)
        
        roleChange['overrideRoles'].remove('unaccepted')
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_ROLES, PyRequest.POST, roleChange, self.expectedSuccess, self.username)
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, expected)
        
        expectedRoleChange = self.unauthed.getDTOResponse(roleChange)
        self.authed.expectResponse(Paths.ROLES, PyRequest.GET, None, expectedRoleChange)
        self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleChange, self.username)
        
        self.authed.expectResponse(Paths.ROLES, PyRequest.GET, None, expectedRoleSet)
        self.admin.expectResponse(AdminPaths.ADMIN_ROLES, PyRequest.GET, None, expectedRoleSet, self.username)
        
    def testUsersFinancesAdmin(self):
        # users finance
        expectedBalance = self.unauthed.getDTOResponse({'balance':self.unauthed.insertExists()})
        balance = self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedBalance)
        expectedBalance = self.unauthed.getDTOResponse(balance['dto'])
        self.unauthed.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, expectedBalance, self.username)
        
        changeBody = {'amount':193}
        changedBalance = balance['dto']
        changedBalance['balance'] = changedBalance['balance'] + 193
        expectedChangedBalance = self.unauthed.getDTOResponse(changedBalance)
        self.unauthed.expectResponse(AdminPaths.ADMIN_FINANCES_ADD, PyRequest.POST, changeBody, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_FINANCES_ADD, PyRequest.POST, changeBody, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES_ADD, PyRequest.POST, changeBody, self.expectedSuccess, self.username)
        
        self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedChangedBalance)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, expectedChangedBalance, self.username)
        
        changeBody = {'amount':193}
        changedBalance['balance'] = changedBalance['balance'] - 193
        expectedChangedBalance = self.unauthed.getDTOResponse(changedBalance)
        self.unauthed.expectResponse(AdminPaths.ADMIN_FINANCES_REMOVE, PyRequest.POST, changeBody, self.expectedDenied, self.username)
        self.authed.expectResponse(AdminPaths.ADMIN_FINANCES_REMOVE, PyRequest.POST, changeBody, self.expectedDenied, self.username)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES_REMOVE, PyRequest.POST, changeBody, self.expectedSuccess, self.username)
        
        self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedChangedBalance)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, expectedChangedBalance, self.username)
        
        self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedBalance)
        self.admin.expectResponse(AdminPaths.ADMIN_FINANCES, PyRequest.GET, None, expectedBalance, self.username)
        
        
    def testTagsPostingsCommentsLockAdmin(self):
        self.addCurrency(self.username, 1000)
        self.addCurrency(self.target, 1000)
        
        content = 'testing123'
        tag = 'testingtag'
        posting = {'title':'testing123',  
                      'cost':10,
                      'tags':[tag],
                      'content':content,
                      'backer':None,
                      'warning':False
                      }
        comment = {'cost':10,
                      'content':content,
                      'backer':None,
                      'warning':False
                      }
        papp1 = {'warning':False,
                 'promotion':10}
        papp2 = {'warning':False,
                 'promotion':10,
                 'tags':[tag]}
        capp = {'warning':False,
                'promotion':10}
        response = self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, posting, self.expectedResultCreated)
        pid = response['dto']['result']
        response = self.authed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, pid)
        cid = response['dto']['result']
        time.sleep(CacheTimes.POSTING)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_PROMOTE, PyRequest.POST, capp, self.expectedSuccess, cid)
        
        self.targetAuthed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, pid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, cid)
        self.targetAuthed.expectResponse(Paths.TAGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, tag)
        
        # tag
        self.admin.expectResponse(AdminPaths.ADMIN_TAGS_ID_LOCK, PyRequest.POST, None, self.expectedSuccess, tag)
        time.sleep(CacheTimes.TAG)
        self.targetAuthed.expectResponse(Paths.TAGS_COMMENTS, PyRequest.POST, comment, self.expectedNotAllowed, tag)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedSuccess, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_TAGS_ID_UNLOCK, PyRequest.POST, None, self.expectedSuccess, tag)
        time.sleep(CacheTimes.TAG)
        self.targetAuthed.expectResponse(Paths.TAGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, tag)
        self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, posting, self.expectedResultCreated)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedSuccess, pid)
        
        # posting
        # remove
        expectedRemoved = self.unauthed.getDTOResponse({'removed':True})
        expectedNotRemoved = self.unauthed.getDTOResponse({'removed':False})
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotRemoved, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_REMOVE, PyRequest.DELETE, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedRemoved, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedNotAllowed, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedNotAllowed, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedNotAllowed, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_REMOVE, PyRequest.POST, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotRemoved, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedSuccess, pid)
        
        # flag
        expectedFlagged = self.unauthed.getDTOResponse({'flagged':True,'removed':True})
        expectedNotFlagged = self.unauthed.getDTOResponse({'flagged':False})
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotFlagged, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID, PyRequest.GET, None, expectedNotFlagged, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_FLAG, PyRequest.DELETE, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedFlagged, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID, PyRequest.GET, None, expectedFlagged, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedNotAllowed, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedNotAllowed, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedNotAllowed, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_FLAG, PyRequest.POST, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotFlagged, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID, PyRequest.GET, None, expectedNotFlagged, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp1, self.expectedSuccess, pid)
        self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, papp2, self.expectedSuccess, pid)
        
        # warning
        expectedWarning = self.unauthed.getDTOResponse({'warning':True})
        expectedNoWarning = self.unauthed.getDTOResponse({'warning':False,'content':content})
        self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNoWarning, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_WARNING, PyRequest.DELETE, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedWarning, pid)
        self.admin.expectResponse(AdminPaths.ADMIN_POSTINGS_ID_WARNING, PyRequest.POST, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNoWarning, pid)
        
        # comment
        # remove
        expectedRemoved = self.unauthed.getDTOResponse({'removed':True})
        expectedNotRemoved = self.unauthed.getDTOResponse({'removed':False})
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNotRemoved, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_REMOVE, PyRequest.DELETE, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedRemoved, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_COMMENTS, PyRequest.POST, comment, self.expectedNotAllowed, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_PROMOTE, PyRequest.POST, capp, self.expectedNotAllowed, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_REMOVE, PyRequest.POST, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNotRemoved, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_PROMOTE, PyRequest.POST, capp, self.expectedSuccess, cid)
        
        # flag
        expectedFlagged = self.unauthed.getDTOResponse({'flagged':True,'removed':True})
        expectedNotFlagged = self.unauthed.getDTOResponse({'flagged':False})
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNotFlagged, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID, PyRequest.GET, None, expectedNotFlagged, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_FLAG, PyRequest.DELETE, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID, PyRequest.GET, None, expectedFlagged, cid)
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedFlagged, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_COMMENTS, PyRequest.POST, comment, self.expectedNotAllowed, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_PROMOTE, PyRequest.POST, capp, self.expectedNotAllowed, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_FLAG, PyRequest.POST, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.authed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNotFlagged, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID, PyRequest.GET, None, expectedNotFlagged, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_COMMENTS, PyRequest.POST, comment, self.expectedResultCreated, cid)
        self.targetAuthed.expectResponse(Paths.COMMENTS_PROMOTE, PyRequest.POST, capp, self.expectedSuccess, cid)
        
        # warning
        expectedWarning = self.unauthed.getDTOResponse({'warning':True})
        expectedNoWarning = self.unauthed.getDTOResponse({'warning':False,'content':content})
        self.unauthed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNoWarning, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_WARNING, PyRequest.DELETE, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.unauthed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedWarning, cid)
        self.admin.expectResponse(AdminPaths.ADMIN_COMMENTS_ID_WARNING, PyRequest.POST, None, self.expectedSuccess, cid)
        time.sleep(CacheTimes.COMMENT)
        self.unauthed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, expectedNoWarning, cid)

    def changeReference(self, pathChange, pathCheck, id, tally, cost, appreciation, promotion):
        changeTally = {'appreciation':appreciation, 'cost':cost, 'promotion':promotion}
        tally['cost'] += cost
        tally['value'] += cost
        tally['appreciation'] += appreciation
        tally['promotion'] += promotion
        tally['value'] += promotion
        self.admin.expectResponse(pathChange, PyRequest.POST, changeTally, self.expectedSuccess, id)
        time.sleep(CacheTimes.POSTING)
        resultTally = {'value':tally['value'], 'cost':tally['cost'], 'promotion':tally['promotion'], 'appreciation':math.floor(tally['appreciation'])}
        expected = self.unauthed.getDTOResponse({'id':id, 'tally':tally})
        self.unauthed.expectResponse(pathCheck, PyRequest.GET, None, expected, id)
        
    def testChangeReferenceTallyAdmin(self):
        data = self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, self.unauthed.getPageResponse())
        postingId = data['page']['content'][0]['id']
        data = self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), postingId)
        postingTally = data['dto']['tally']
        
        data = self.unauthed.expectResponse(Paths.COMMENTS, PyRequest.GET, None, self.unauthed.getPageResponse())
        commentId = data['page']['content'][0]['id']
        data = self.unauthed.expectResponse(Paths.COMMENTS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), commentId)
        commentTally = data['dto']['tally']
        
        costChange = 150
        appreciationChange = 93
        promotionChange = 33
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, 0, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, 0 - appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0 - costChange, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0 - costChange, 0 - appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, 0, promotionChange)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0, appreciationChange, 0 - promotionChange)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, appreciationChange, promotionChange)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, costChange, 0 - appreciationChange, 0 - promotionChange)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0 - costChange, appreciationChange, promotionChange)
        self.changeReference(AdminPaths.ADMIN_POSTINGS_ID_TALLY_CHANGE, Paths.POSTINGS_ID, postingId, postingTally, 0 - costChange, 0 - appreciationChange, 0 - promotionChange)
        
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, 0, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, 0 - appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0 - costChange, appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0 - costChange, 0 - appreciationChange, 0)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, 0, promotionChange)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0, appreciationChange, 0 - promotionChange)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, appreciationChange, promotionChange)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, costChange, 0 - appreciationChange, 0 - promotionChange)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0 - costChange, appreciationChange, promotionChange)
        self.changeReference(AdminPaths.ADMIN_COMMENTS_ID_TALLY_CHANGE, Paths.COMMENTS_ID, commentId, commentTally, 0 - costChange, 0 - appreciationChange, 0 - promotionChange)
        
    def testRestrictedAdmin(self):
        def randomUsername():
            return ''.join(random.choice('0123456789abcdefghijklmnopqrstuvwxyz') for i in range(16))
        def createUser(result, name = None, email = None, password = None):
            if name == None:
                name = randomUsername()
            if email == None:
                email = (name + "@test.com")
            if password == None:
                password = name
            body = {"username":name, "email":email, "password":password, "confirmNewPassword":password}
            PyRequest().expectResponse(Paths.REGISTER, PyRequest.POST, body, result)
        
        createUser(self.expectedResultCreated, randomUsername())
        
        restrictedName = randomUsername()
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedDenied, restrictedName, "rtype=USERNAME")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedName, "rtype=USERNAME")
        rBody = {'word':restrictedName, 'type':'USERNAME'}
        expected = self.unauthed.getDTOResponse(rBody)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedCreated)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, expected, restrictedName, "rtype=USERNAME")
        createUser(self.expectedRestrictedUsername, restrictedName)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.DELETE, None, self.expectedDeleted, restrictedName, "rtype=USERNAME")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedName, "rtype=USERNAME")
        createUser(self.expectedResultCreated, restrictedName)
        
        restrictedEmail = randomUsername() + "@restricted.com"
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedDenied, restrictedEmail, "rtype=EMAIL")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedEmail, "rtype=EMAIL")
        rBody = {'word':restrictedEmail, 'type':'EMAIL'}
        expected = self.unauthed.getDTOResponse(rBody)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedCreated)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, expected, restrictedEmail, "rtype=EMAIL")
        createUser(self.expectedRestrictedEmail, randomUsername(), restrictedEmail)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.DELETE, None, self.expectedDeleted, restrictedEmail, "rtype=EMAIL")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedEmail, "rtype=EMAIL")
        createUser(self.expectedResultCreated, randomUsername(), restrictedEmail)
        
        restrictedPassword = randomUsername()
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedDenied, restrictedPassword, "rtype=PASSWORD")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedPassword, "rtype=PASSWORD")
        rBody = {'word':restrictedPassword, 'type':'PASSWORD'}
        expected = self.unauthed.getDTOResponse(rBody)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedCreated)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, expected, restrictedPassword, "rtype=PASSWORD")
        createUser(self.expectedRestrictedPassword, randomUsername(), randomUsername() + "@test.com", restrictedPassword)
        self.authed.expectResponse(AdminPaths.ADMIN_RESTRICTEDS, PyRequest.POST, rBody, self.expectedDenied)
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.DELETE, rBody, self.expectedDeleted, restrictedPassword, "rtype=PASSWORD")
        self.admin.expectResponse(AdminPaths.ADMIN_RESTRICTEDS_ID, PyRequest.GET, None, self.expectedNotFound, restrictedPassword, "rtype=PASSWORD")
        createUser(self.expectedResultCreated, randomUsername(), randomUsername() + "@test.com", restrictedPassword)
        
    def testFeedback(self):
        self.unauthed.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.expectedDenied)
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse(), None, "feedbackType=bug")
        count = {}
        count['bug'] = data['page']['totalElements']
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse(), None, "feedbackType=synopsis")
        count['synopsis'] = data['page']['totalElements']
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse(), None, "feedbackType=suggestion")
        count['suggestion'] = data['page']['totalElements']
        expected = self.unauthed.getPageResponse({'totalElements':count['bug'] + count['synopsis'] + count['suggestion']})
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, expected)
        
        submission = {'summary':'Shit website!','context':'GENERAL','type':'BUG'}
        self.unauthed.expectResponse(Paths.FEEDBACKS, PyRequest.POST, submission, self.expectedDenied)
        self.authed.expectResponse(Paths.FEEDBACKS, PyRequest.POST, submission, self.expectedCreated)
        submission['type'] = 'SYNOPSIS'
        self.authed.expectResponse(Paths.FEEDBACKS, PyRequest.POST, submission, self.expectedCreated)
        submission['type'] = 'SUGGESTION'
        self.authed.expectResponse(Paths.FEEDBACKS, PyRequest.POST, submission, self.expectedCreated)
        count['bug'] += 1
        count['synopsis'] += 1
        count['suggestion'] += 1
        
        expected = self.unauthed.getPageResponse({'totalElements':count['bug'] + count['synopsis'] + count['suggestion']})
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, expected)
        
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['bug']}), None, ['feedbackType=bug','state=INITIAL','context=GENERaL'])
        bug = data['page']['content'][0]
        self.unauthed.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.expectedDenied, bug['id'])
        self.authed.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.expectedDenied, bug['id'])
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(bug), bug['id'])
        
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['synopsis']}), None, ['feedbackType=SYNOPSIS','state=initial','context=GENERAL'])
        synopsis = data['page']['content'][0]
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(synopsis), synopsis['id'])
        
        data = self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['suggestion']}), None, ['feedbackType=sugGestion','state=INiTIAL','context=general'])
        suggestion = data['page']['content'][0]
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(suggestion), suggestion['id'])
        
        change = {'ids':[bug['id']], 
                  'summary':'I take it back it is amazing!', 
                  'type':'SUGGESTION', 
                  'state':'COMPLETE', 
                  'context':'POSTING'}
        bug['summary'] = str(change['summary'])
        bug['type'] = str(change['type'])
        bug['state'] = str(change['state'])
        bug['context'] = str(change['context'])
        del bug['lastModified']
        count['bug'] -= 1
        count['suggestion'] += 1
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_CHANGE, PyRequest.POST, change, self.expectedSuccess)
        
        change = {'ids':[synopsis['id'], suggestion['id']], 
                  'summary':'I take it back it is amazing!', 
                  'state':'READ', 
                  'context':'TAG'}
        synopsis['summary'] = str(change['summary'])
        synopsis['state'] = str(change['state'])
        synopsis['context'] = str(change['context'])
        del synopsis['lastModified']
        suggestion['summary'] = str(change['summary'])
        suggestion['state'] = str(change['state'])
        suggestion['context'] = str(change['context'])
        del suggestion['lastModified']
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_CHANGE, PyRequest.POST, change, self.expectedSuccess)
        
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['bug']}), None, ['feedbackType=bug','state=INITIAL','context=GENERaL'])
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(bug), bug['id'])
        
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['synopsis']}), None, ['feedbackType=SYNOPSIS','state=initial','context=GENERAL'])
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(synopsis), synopsis['id'])
        
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, self.unauthed.getPageResponse({'totalElements':count['suggestion']}), None, ['feedbackType=sugGestion','state=INiTIAL','context=general'])
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(suggestion), suggestion['id'])
        
        expected = self.unauthed.getPageResponse({'totalElements':count['bug'] + count['synopsis'] + count['suggestion']})
        self.admin.expectResponse(AdminPaths.ADMIN_FEEDBACKS, PyRequest.GET, None, expected)
        
    def testRename(self):
        """split = re.match(r"([a-z]+)([0-9]+)",self.username, re.I)
        if not split:
            num = str(randrange(1,999999))
        else:
            num = str(split.groups()[1])
        """
        num = str(random.randrange(1,999999))
        renameUsername = 'r2n' + str(num)
        renameTarget = 't2n' + str(num)
        renameSecondary = 's2n' + str(num)
        renameSecondaryTarget = 'st2n' + str(num)
        
        primary = self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), self.username)['dto']
        target = self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), self.target)['dto']
        secondary = self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), self.secondary)['dto']
        secondaryTarget = self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(), self.secondaryTarget)['dto']
        
        primary['username']['username'] = renameUsername
        target['username']['username'] = renameTarget
        secondary['username']['username'] = renameSecondary
        secondaryTarget['username']['username'] = renameSecondaryTarget
        
        primaryCurrent = self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse({'username':{'username':self.username}}))['dto']
        targetCurrent = self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse({'username':{'username':self.target}}))['dto']
        secondaryCurrent = self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse({'username':{'username':self.secondary}}))['dto']
        secondaryTargetCurrent = self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse({'username':{'username':self.secondaryTarget}}))['dto']
        
        primaryCurrent['username']['username'] = renameUsername
        targetCurrent['username']['username'] = renameTarget
        secondaryCurrent['username']['username'] = renameSecondary
        secondaryTargetCurrent['username']['username'] = renameSecondaryTarget
        
        change = {'username':renameUsername}
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_RENAME, PyRequest.POST, change, self.expectedSuccess, self.username)
        change = {'username':renameTarget}
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_RENAME, PyRequest.POST, change, self.expectedSuccess, self.target)
        change = {'username':renameSecondary}
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_RENAME, PyRequest.POST, change, self.expectedSuccess, self.secondary)
        change = {'username':renameSecondaryTarget}
        self.admin.expectResponse(AdminPaths.ADMIN_USERS_RENAME, PyRequest.POST, change, self.expectedSuccess, self.secondaryTarget)
        
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.expectedNotFound, self.username)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.expectedNotFound, self.target)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.expectedNotFound, self.secondary)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.expectedNotFound, self.secondaryTarget)
        
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(primary), renameUsername)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(target), renameTarget)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(secondary), renameSecondary)
        self.unauthed.expectResponse(Paths.USERS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(secondaryTarget), renameSecondaryTarget)
        
        self.authed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse(primaryCurrent))
        self.targetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse(targetCurrent))
        self.secondaryAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse(secondaryCurrent))
        self.secondaryTargetAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse(secondaryTargetCurrent))
        
        
