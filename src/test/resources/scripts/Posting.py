from PyConstants import Paths
from PyConstants import Codes
from PyConstants import CacheTimes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
from Admin import Admin
from random import randrange
import time

class Posting(BaseTest):
    
    def runTests(self, notifications, secondaryToken, secondaryName, lockedUserToken):
        self.notifications = notifications
        self.notifications['posting']['backer'] = 0
        self.notifications['posting']['promotion'] = 0
        self.notifications['posting']['backerPromotion'] = 0

        self.postingTags = {}
        
        self.secondaryAuthed = PyRequest(secondaryToken)
        self.lockedAuthed = PyRequest(lockedUserToken)
        print("Running posting tests")
        expectedFinances = {'code':Codes.SUCCESS}
        expectedPostingsCount = {'code':Codes.SUCCESS}
        
        admin = Admin()
        admin.addCurrency(self.username, 2000)
        admin.addCurrency(self.target, 2000)
        admin.addCurrency(secondaryName, 2000)
        
        #backingBody = {'username':self.username, 'amount':1000}
        #self.targetAuthed.expectResponse(Paths.OFFERS, PyRequest.POST, backingBody, self.expectedSuccess)
        #self.authed.expectResponse(Paths.OFFERS_ACCEPT, PyRequest.POST, None, self.expectedSuccess, self.target)
        self.backer = None
        #self.backer = self.target
        #self.notifications['backing']['offer'] += 1
        #self.notifications['backing']['accept'] += 1
        
        data = self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedFinances)
        self.balance = data['dto']['balance']
        data = self.targetAuthed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expectedFinances)
        self.targetBalance = data['dto']['balance']
        
        self.postings = {}
        self.otherPostingsCount = 0
        
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPostingsCount, None, ['time=alltime','warning=true'])
        self.otherPostingsCount = data['page']['totalElements']
        
        self.testAddPostings()
        self.testPromotePostings()
        self.testSort()
        self.testPromotePostings()
        self.testAddPostings()
        self.testSort()
        self.testAddPostings()
        self.testAddPostings()
        self.testPromotePostings()
        self.testSort()
        self.testPostings()
        #self.testNotifications()
        
        balance = {'balance':self.balance}
        expected = self.unauthed.getDTOResponse(balance)
        self.authed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expected)
        balance = {'balance':self.targetBalance}
        expected = self.unauthed.getDTOResponse(balance)
        self.targetAuthed.expectResponse(Paths.FINANCES, PyRequest.GET, None, expected)
        
        #self.testFlag()
        return self.postings.keys()
        
    def testPostings(self):
        time.sleep(CacheTimes.POSTING)
        for k in self.postings.keys():
            posting = self.postings[k]
            tags = None
            if 'tags' in posting:
                tags = posting['tags'][:]
                posting['tags'] = None
            expected = self.unauthed.getDTOResponse(posting)
            self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expected, k)
            self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expected, k)
            if tags != None:
                posting['tags'] = tags
       
    def testPostingsPaged(self):
        for k in self.postings.keys():
            posting = self.postings[k]
            expected = self.unauthed.getDTOResponse(posting)
            #checking again fails. The tags have changed since last checking, and this test is no longer valid
            #self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expected, k)
            #self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expected, k)
            
        page = {'totalElements':self.otherPostingsCount + len(self.postings.keys())}
        expectedPage = self.unauthed.getPageResponse(page)
        
        self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, ['time=alltime','warning=true'])
        self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, ['time=alltime','warning=true'])
        
        author = {'totalElements':len(self.postings.keys())}
        expectedPage = self.unauthed.getPageResponse(author)
        self.unauthed.expectResponse(Paths.POSTINGS_AUTHOR, PyRequest.GET, None, expectedPage, self.username,['warning=true'])
        
        self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, self.expectedInvalid, None,['warning=true','user=invalid#%@'])
        self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, self.expectedNotFound, None,['warning=true','user=notFoundName838c'])
        self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None,['warning=true','user='+self.username])
        
        #beneficiary = {'totalElements':len(self.postings.keys()) / 2}
        #expectedPage = self.unauthed.getPageResponse(beneficiary)
        #self.unauthed.expectResponse(Paths.POSTINGS_BENEFICIARY, PyRequest.GET, None, expectedPage, self.backer,['warning=true'])
        
        self.testTags()

    def testAddPostings(self):
        cost = randrange(1,15)
        title = self.username + ' Title ' + str(cost)
        numTags = randrange(0,5)
        tags = {}
        tagsList = range(numTags)
        for i in range(numTags):
            tag = str('tag' + str(i))
            tags[tag] = cost
            tagsList[i] = tag
        content = 'This is a test content!'
        
        submission = {'title':title, 
                      'cost':9000,
                      'tags':tagsList,
                      'content':'',
                      'backer':None,
                      'warning':False
                      }
        
        resultSuccess = {'result': self.unauthed.insertExists()}
        createSuccess = self.unauthed.getCustomResponse(Codes.CREATED, resultSuccess, self.unauthed.insertNotExists())
        self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, submission, self.expectedInvalid)
        submission['content'] = content
        self.unauthed.expectResponse(Paths.POSTINGS, PyRequest.POST, submission, self.expectedDenied)
        self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, submission, self.expectedBalance)
        submission['cost'] = cost
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, submission, createSuccess)
        
        self.balance -= cost
        id = data['dto']['result']
        
        self.testRemovePosting(id)
        
        tally = {'cost':cost,'appreciation':0,'promotion':0,'value':cost}
        posting = {'id':id,
                   'title':title,
                   'author':self.createUsername(self.username),
                   'tally':tally,
                   'tags':tagsList,
                   'content':content,
                   'promotionCount':0,
                   'appreciationCount':0
                   }
        self.postings[id] = posting
        self.postingTags[id] = tags        

        if self.backer != None and isinstance(self.backer, str):
            backedSubmission = {'title':title, 
                      'cost':cost,
                      'tags':tagsList,
                      'content':content,
                      'backer':self.backer,
                      'warning':False
                      }
            data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.POST, backedSubmission, createSuccess)
            backedId = data['dto']['result']
            backedPosting = {'id':backedId,
                        'title':title,
                        'author':self.createUsername(self.username),
                        'tally':tally.copy(),
                        'tags':tagsList[:],
                        'content':content,
                        'promotionCount':0,
                        'appreciationCount':0,
                        'beneficiary':self.createUsername(self.backer)
                   }
            self.notifications['posting']['backer'] += 1
            self.postings[backedId] = backedPosting
            self.postingTags[backedId] = tags.copy()

        self.testPostings()

        submission = {'content':'', 'title':title}
        self.authed.expectResponse(Paths.POSTINGS_ID_EDIT, PyRequest.POST, submission, self.expectedInvalid, id)
        submission = {'content':content, 'title':''}
        self.authed.expectResponse(Paths.POSTINGS_ID_EDIT, PyRequest.POST, submission, self.expectedInvalid, id)

        time.sleep(CacheTimes.POSTING)

        edit = 'This is a test edit!'
        title = 'this is a Changed title!'
        submission = {'content':edit, 'title':title}
        self.unauthed.expectResponse(Paths.POSTINGS_ID_EDIT, PyRequest.POST, submission, self.expectedDenied, id) 
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_ID_EDIT, PyRequest.POST, submission, self.expectedNotAllowed, id)
        self.authed.expectResponse(Paths.POSTINGS_ID_EDIT, PyRequest.POST, submission, self.expectedSuccess, id)

        time.sleep(CacheTimes.POSTING)

        expectedEdit = self.unauthed.getDTOResponse(submission)
        self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedEdit, id)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedEdit, id)
        self.postings[id]['title'] = title
        self.postings[id]['content'] = edit

        
    def testPromotePostings(self):
        promotion = randrange(25,55)
        keyNum = randrange(len(self.postings.keys()))
        id = self.postings.keys()[keyNum]
        posting = self.postings[id].copy()
        invalidid = "123"
        numTags = randrange(1,2)
        tagsList = []
        for i in range(numTags):
            tag = 'tag' + str(i)
            tagsList.append(tag)
        for i in range(numTags,5):
            tag = 'promotag' + str(i)
            tagsList.append(tag)
        
        
        submission = {'promotion':9000,'warning':True}
        
        self.unauthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedDenied,id)
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedInvalid,invalidid)
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedBalance,id)
        submission = {'promotion':promotion,'warning':True}
        #self.authed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedNotAllowed,id)
        if 'beneficiary' in posting:
            #self.targetAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedNotAllowed,id)
            self.notifications['posting']['backerPromotion'] += 2
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedSuccess,id)
        submission = {'promotion':promotion, 'tags':tagsList, 'warning':True}
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_PROMOTE, PyRequest.POST, submission, self.expectedSuccess,id)

        self.notifications['posting']['promotion'] += 2
        
        pT = self.postingTags[id]
        for k in tagsList:
            if k not in pT.keys():
                pT[k] = 0
            pT[k] += promotion
        self.postingTags[id] = pT
        
        posting['tally']['promotion'] += promotion * 2
        posting['tally']['value'] += promotion * 2
        posting['warning'] = True
        if 'content' in posting:
            del posting['content']
        posting['promotionCount'] += 2
        self.postings[id] = posting
        
    def testRemovePosting(self, pid):
        # testing remove
        expectedNotRemoved = self.unauthed.getDTOResponse({'removed': False})
        expectedRemoved = self.unauthed.getDTOResponse({'removed': True})
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotRemoved, pid)
        self.authed.expectResponse(Paths.POSTINGS_ID_DISABLE, PyRequest.POST, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedRemoved, pid)
        self.authed.expectResponse(Paths.POSTINGS_ID_ENABLE, PyRequest.POST, None, self.expectedSuccess, pid)
        time.sleep(CacheTimes.POSTING)
        self.authed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, expectedNotRemoved, pid)
        
    def testSort(self):
        time.sleep(CacheTimes.POSTING)
        expectedPage = self.unauthed.getPageResponse()
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, 
                                          ['size=50','page=0', 'sort=value', 'time=alltime'])
        valueList = data['page']['content']
        sortedValueList = sorted(valueList, 
                                 key=lambda x: x['tally']['value'], reverse=True)
        valueList = map(lambda x: {x['id']:x['tally']['value']}, valueList)
        sortedValueList = map(lambda x: {x['id']:x['tally']['value']}, sortedValueList)
        
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, 
                                          ['size=50','page=0', 'sort=value', 'time=hour'])
        hourList = data['page']['content']
        sortedHourList = sorted(hourList, 
                                 key=lambda x: x['tally']['value'], reverse=True)
        hourList = filter(lambda x: x['id'] in self.postings.keys(), hourList)
        sortedHourList = filter(lambda x: x['id'] in self.postings.keys(), sortedHourList)
        hourList = map(lambda x: {x['id']:x['tally']['value']}, hourList)
        sortedHourList = map(lambda x: {x['id']:x['tally']['value']}, sortedHourList)
        
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, 
                                          ['size=50','page=0', 'sort=promotion', 'time=alltime'])
        promotionList = data['page']['content']
        sortedPromotionList = sorted(promotionList, 
                                        key=lambda x: x['tally']['promotion'], reverse=True)
        promotionList = map(lambda x: {x['id']:x['tally']['promotion']}, promotionList)
        sortedPromotionList = map(lambda x: {x['id']:x['tally']['promotion']}, sortedPromotionList)
        
        data = self.authed.expectResponse(Paths.POSTINGS, PyRequest.GET, None, expectedPage, None, 
                                          ['size=50','page=0', 'sort=cost', 'time=alltime'])
        costList = data['page']['content']
        sortedCostList = sorted(costList, key=lambda x: x['tally']['cost'], reverse=True)
        costList = map(lambda x: {x['id']:x['tally']['cost']}, costList)
        sortedCostList = map(lambda x: {x['id']:x['tally']['cost']}, sortedCostList)
        
        valueError = False
        for position, id in enumerate(valueList):
            if id != sortedValueList[position]:
                valueError = True
        hourError = False
        for position, id in enumerate(hourList):
            if id != sortedHourList[position]:
                hourError = True
        
        promotionError = False
        for position, id in enumerate(promotionList):
            if id != sortedPromotionList[position]:
                promotionError = True
        
        costError = False
        for position, id in enumerate(costList):
            if id != sortedCostList[position]:
                costError = True
        
        if valueError:
            self.error(valueList, sortedValueList, PyRequest.GET, Paths.POSTINGS, None, None, 'sort=value&time=alltime')
        if hourError:
            self.error(hourList, sortedHourList, PyRequest.GET, Paths.POSTINGS, None, None, 'sort=value&time=hour')
        if promotionError:
            self.error(promotionList, sortedPromotionList, PyRequest.GET, Paths.POSTINGS, None, None, 'sort=promotion&time=alltime')
        if costError:
            self.error(costList, sortedCostList, PyRequest.GET, Paths.POSTINGS, None, None, 'sort=cost&time=alltime')
        
    def testFlag(self):
        data = self.lockedAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.unauthed.getDTOResponse())
        Admin().addCurrency(data['dto']['username']['username'], 1000)
        
        cost = 5
        title = 'Flagged title'
        content = 'Flagged content'
        submission = {'title':title, 
                      'cost':cost,
                      'tags':[],
                      'content':content,
                      'backer':None,
                      'warning':False
                      }
        data = self.lockedAuthed.expectResponse(Paths.POSTINGS, PyRequest.POST, submission, self.expectedResultCreated)
        
        id = data['dto']['result']
        
        tally = {'cost':cost,'appreciation':0,'promotion':0,'value':cost}
        posting = {'id':id,
                   'title':title,
                   'tally':tally,
                   'content':content,
                   'promotionCount':0,
                   'appreciationCount':0,
                   'removed':False
                   }

        time.sleep(CacheTimes.POSTING)
        
        self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(posting), id)
        posting['removed'] = True
        
        self.authed.expectResponse(Paths.POSTINGS_ID_FLAG, PyRequest.POST, None, self.expectedSuccess, id)
        self.targetAuthed.expectResponse(Paths.POSTINGS_ID_FLAG, PyRequest.POST, None, self.expectedSuccess, id)
        self.secondaryAuthed.expectResponse(Paths.POSTINGS_ID_FLAG, PyRequest.POST, None, self.expectedSuccess, id)
        
        #self.unauthed.expectResponse(Paths.POSTINGS_ID, PyRequest.GET, None, self.unauthed.getDTOResponse(posting), id)
        #self.lockedAuthed.expectResponse(Paths.USERS_CURRENT, PyRequest.GET, None, self.expectedLocked)
        
    def testTags(self):
        i = randrange(0,2)
        tag = 'tag0'
        time.sleep(CacheTimes.POSTING)
 
        tagCount = reduce(lambda x,y: (self.postings[y]['author']['username'] == self.username and tag in self.postings[y]['tags']) + x, self.postings.keys(), 0)

        expected = self.unauthed.getPageResponse({'totalElements':tagCount})
        self.unauthed.expectResponse(Paths.POSTINGS_AUTHOR, PyRequest.GET, None, expected, self.username, [str("tags=" + tag), 'time=alltime', 'warning=true'])
        self.authed.expectResponse(Paths.POSTINGS_AUTHOR, PyRequest.GET, None, expected, self.username, [str("tags=" + tag), 'time=alltime', 'warning=true'])
    
    def testNotifications(self):
        page = {'totalElements':self.notifications['posting']['promotion']}
        expected = self.unauthed.getPageResponse(page)
        self.authed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=promotion_posting')
        
        page = {'totalElements':self.notifications['posting']['backer']}
        expected = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=posting')
        
        page = {'totalElements':self.notifications['posting']['backerPromotion']}
        expected = self.unauthed.getPageResponse(page)
        self.targetAuthed.expectResponse(Paths.NOTIFICATIONS, PyRequest.GET, None, expected, None, 'event=promotion_posting')
        
        
